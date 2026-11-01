package com.mycompany.levelup_code.data.oracle;

import com.mycompany.levelup_code.data.connection.DatabaseConnection;
import com.mycompany.levelup_code.data.dao.AttemptRepository;
import com.mycompany.levelup_code.domain.enums.AttemptResult;
import com.mycompany.levelup_code.domain.model.Attempt;
import com.mycompany.levelup_code.domain.model.Challenge;
import com.mycompany.levelup_code.domain.model.User;
import com.mycompany.levelup_code.exception.BusinessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación Oracle JDBC de {@link AttemptRepository} sobre la tabla {@code Intentos}.
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
            throw new BusinessException("Error al consultar historial de intentos en Oracle: " + e.getMessage(), e);
        }
        return list;
    }

    private AttemptResult parseResult(String raw) {
        if (raw == null) {
            return AttemptResult.INCORRECT;
        }
        try {
            return AttemptResult.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return AttemptResult.INCORRECT;
        }
    }
}

