package com.mycompany.levelup_code.data.oracle;

import com.mycompany.levelup_code.data.connection.DatabaseConnection;
import com.mycompany.levelup_code.data.dao.AttemptRepository;
import com.mycompany.levelup_code.domain.enums.AttemptResult;
import com.mycompany.levelup_code.domain.model.Attempt;
import com.mycompany.levelup_code.domain.model.Challenge;
import com.mycompany.levelup_code.domain.model.User;
import com.mycompany.levelup_code.exception.BusinessException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación Oracle JDBC de {@link AttemptRepository} sobre la tabla {@code Intentos}.
 * Soporta ejecución transaccional delegada en PL/SQL ({@code PKG_GAMIFICACION.SP_REGISTRAR_INTENTO_RETO})
 * con fallback a inserción SQL directa.
 */
public class OracleAttemptRepository implements AttemptRepository {

    private final OracleChallengeRepository challengeRepository = new OracleChallengeRepository();
    private final UserDAO userDAO = new UserDAO();

    @Override
    public boolean hasSuccessfulAttempt(int userId, int challengeId) {
        String sql = "SELECT 1 FROM Intentos WHERE id_usuario = ? AND id_reto = ? AND es_correcto = 1 FETCH FIRST 1 ROWS ONLY";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, challengeId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new BusinessException("Error al verificar intento previo en Oracle: " + e.getMessage(), e);
        }
    }

    @Override
    public Attempt save(Attempt attempt) {
        // 1. Intentar registrar mediante el procedimiento almacenado PL/SQL en Oracle
        String plsql = "{call PKG_GAMIFICACION.SP_REGISTRAR_INTENTO_RETO(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(plsql)) {

            cstmt.setInt(1, attempt.getUser().getId());
            cstmt.setInt(2, attempt.getChallenge().getId());
            cstmt.setInt(3, attempt.isCorrect() ? 1 : 0);
            cstmt.setString(4, attempt.getSubmittedContent() != null ? attempt.getSubmittedContent() : "");
            cstmt.setString(5, attempt.getResult() != null ? attempt.getResult().name() : AttemptResult.INCORRECT.name());
            cstmt.setInt(6, (int) Math.max(0, attempt.getExecutionTimeMs()));

            // Parámetros de salida OUT calculados por PL/SQL
            cstmt.registerOutParameter(7, Types.NUMERIC);  // p_nuevo_nivel
            cstmt.registerOutParameter(8, Types.NUMERIC);  // p_nueva_exp
            cstmt.registerOutParameter(9, Types.NUMERIC);  // p_vidas_restantes
            cstmt.registerOutParameter(10, Types.NUMERIC); // p_exp_otorgada
            cstmt.registerOutParameter(11, Types.NUMERIC); // p_id_intento_creado

            cstmt.execute();

            int nuevoNivel = cstmt.getInt(7);
            int nuevaExp = cstmt.getInt(8);
            int vidasRestantes = cstmt.getInt(9);
            int expOtorgada = cstmt.getInt(10);
            int idIntentoCreado = cstmt.getInt(11);

            attempt.setId(idIntentoCreado);
            attempt.setEarnedExperience(expOtorgada);

            // Sincronizar el estado del usuario en memoria con los valores calculados por PL/SQL
            if (attempt.getUser() != null) {
                attempt.getUser().setLevel(nuevoNivel);
                attempt.getUser().setExperience(nuevaExp);
                attempt.getUser().setLives(vidasRestantes);
            }
            return attempt;
        } catch (SQLException ex) {
            // Si el paquete PL/SQL no ha sido compilado en la BD aún, recurrir a inserción directa
            return saveWithDirectInsert(attempt);
        }
    }

    private Attempt saveWithDirectInsert(Attempt attempt) {
        String sql = "INSERT INTO Intentos ("
                + "id_usuario, id_reto, codigo_enviado, salida_obtenida, "
                + "es_correcto, tiempo_ejecucion_ms, exp_obtenida, fecha_intento"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"ID_INTENTO"})) {
            stmt.setInt(1, attempt.getUser().getId());
            stmt.setInt(2, attempt.getChallenge().getId());
            stmt.setString(3, attempt.getSubmittedContent());
            stmt.setString(4, attempt.getResult() != null ? attempt.getResult().name() : AttemptResult.INCORRECT.name());
            stmt.setInt(5, attempt.isCorrect() ? 1 : 0);
            stmt.setLong(6, Math.max(0, attempt.getExecutionTimeMs()));
            stmt.setInt(7, Math.max(0, attempt.getEarnedExperience()));
            LocalDateTime when = attempt.getAttemptDate() != null ? attempt.getAttemptDate() : LocalDateTime.now();
            stmt.setTimestamp(8, Timestamp.valueOf(when));

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    attempt.setId(rs.getInt(1));
                }
            }
            return attempt;
        } catch (SQLException e) {
            throw new BusinessException("Error al guardar intento en Oracle: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Attempt> findByUserAndChallenge(int userId, int challengeId) {
        List<Attempt> list = new ArrayList<>();
        String sql = "SELECT id_intento, id_usuario, id_reto, codigo_enviado, salida_obtenida, "
                + "es_correcto, tiempo_ejecucion_ms, exp_obtenida, fecha_intento "
                + "FROM Intentos WHERE id_usuario = ? AND id_reto = ? ORDER BY fecha_intento ASC";

        User user = userDAO.findById(userId).orElse(null);
        Challenge challenge = challengeRepository.findById(challengeId).orElse(null);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, challengeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    AttemptResult result = rs.getInt("es_correcto") == 1
                            ? AttemptResult.CORRECT
                            : parseResult(rs.getString("salida_obtenida"));
                    Timestamp ts = rs.getTimestamp("fecha_intento");
                    Attempt a = new Attempt(
                            rs.getInt("id_intento"),
                            user,
                            challenge,
                            rs.getString("codigo_enviado"),
                            result,
                            rs.getLong("tiempo_ejecucion_ms"),
                            rs.getInt("exp_obtenida"),
                            ts != null ? ts.toLocalDateTime() : LocalDateTime.now()
                    );
                    list.add(a);
                }
            }
        } catch (SQLException e) {
            throw new BusinessException("Error al listar intentos por usuario y reto en Oracle: " + e.getMessage(), e);
        }
        return list;
    }

    private AttemptResult parseResult(String raw) {
        if (raw == null) {
            return AttemptResult.INCORRECT;
        }
        try {
            return AttemptResult.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return AttemptResult.INCORRECT;
        }
    }
}
