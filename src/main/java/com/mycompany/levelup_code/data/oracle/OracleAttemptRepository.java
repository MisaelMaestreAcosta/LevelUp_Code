package com.mycompany.levelup_code.data.oracle;

import com.mycompany.levelup_code.data.connection.DatabaseConnection;
import com.mycompany.levelup_code.data.dao.AttemptRepository;
import com.mycompany.levelup_code.domain.enums.AttemptResult;
import com.mycompany.levelup_code.domain.enums.ChallengeProgress;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementación Oracle JDBC de {@link AttemptRepository} sobre la tabla {@code Intentos}.
 * Ejecuta como flujo transaccional único el procedimiento PL/SQL
 * {@code PKG_GAMIFICACION.SP_REGISTRAR_INTENTO_RETO} confirmando la transacción desde el cliente.
 */
public class OracleAttemptRepository implements AttemptRepository {

    private static final Logger LOGGER = Logger.getLogger(OracleAttemptRepository.class.getName());

    private final OracleChallengeRepository challengeRepository = new OracleChallengeRepository();
    private final UserDAO userDAO = new UserDAO();

    @Override
    public boolean managesGamificationTransactionally() {
        return true;
    }

    @Override
    public Map<Integer, ChallengeProgress> findProgressByUser(int userId) {
        Map<Integer, ChallengeProgress> progress = new HashMap<>();
        String sql = "SELECT id_reto, MAX(es_correcto) AS aprobado "
                + "FROM Intentos WHERE id_usuario = ? GROUP BY id_reto";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    progress.put(rs.getInt("id_reto"), rs.getInt("aprobado") == 1
                            ? ChallengeProgress.SOLVED : ChallengeProgress.ATTEMPTED);
                }
            }
            return progress;
        } catch (SQLException ex) {
            throw new BusinessException("No se pudo consultar el estado de los retos: " + ex.getMessage(), ex);
        }
    }

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
        String plsql = "{call PKG_GAMIFICACION.SP_REGISTRAR_INTENTO_RETO(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (CallableStatement cstmt = conn.prepareCall(plsql)) {
                try (PreparedStatement lock = conn.prepareStatement("SELECT estado,revision FROM Retos WHERE id_reto=? FOR UPDATE")) {
                    lock.setInt(1, attempt.getChallenge().getId());
                    try (ResultSet current = lock.executeQuery()) {
                        if (!current.next() || !"PUBLICADO".equals(current.getString(1))
                                || current.getInt(2) != attempt.getChallenge().getRevision()) {
                            throw new IllegalStateException("El reto cambió durante la evaluación. Abre su versión actual para enviar.");
                        }
                    }
                }
                cstmt.setInt(1, attempt.getUser().getId());
                cstmt.setInt(2, attempt.getChallenge().getId());
                cstmt.setInt(3, attempt.isCorrect() ? 1 : 0);
                cstmt.setString(4, attempt.getSubmittedContent() != null ? attempt.getSubmittedContent() : "");
                cstmt.setString(5, attempt.getResult() != null ? attempt.getResult().name() : AttemptResult.INCORRECT.name());
                cstmt.setInt(6, (int) Math.max(0, attempt.getExecutionTimeMs()));
                cstmt.setString(7, attempt.getRequestId());

                cstmt.registerOutParameter(8, Types.NUMERIC);  // p_nuevo_nivel
                cstmt.registerOutParameter(9, Types.NUMERIC);  // p_nueva_exp
                cstmt.registerOutParameter(10, Types.NUMERIC); // p_vidas_restantes
                cstmt.registerOutParameter(11, Types.NUMERIC); // p_exp_otorgada
                cstmt.registerOutParameter(12, Types.NUMERIC); // p_id_intento_creado

                cstmt.execute();

                int nuevoNivel = cstmt.getInt(8);
                int nuevaExp = cstmt.getInt(9);
                int vidasRestantes = cstmt.getInt(10);
                int expOtorgada = cstmt.getInt(11);
                int idIntentoCreado = cstmt.getInt(12);

                conn.commit();

                attempt.setId(idIntentoCreado);
                attempt.setEarnedExperience(expOtorgada);

                if (attempt.getUser() != null) {
                    attempt.getUser().setLevel(nuevoNivel);
                    attempt.getUser().setExperience(nuevaExp);
                    attempt.getUser().setLives(vidasRestantes);
                    // The attempt is already committed at this point. A secondary
                    // statistics refresh must never turn that confirmed save into
                    // a failure that invites the student to submit it again.
                    try {
                        userDAO.findById(attempt.getUser().getId()).ifPresent(fresh -> {
                            attempt.getUser().setCurrentStreak(fresh.getCurrentStreak());
                            attempt.getUser().setMaxStreak(fresh.getMaxStreak());
                        });
                    } catch (RuntimeException refreshError) {
                        LOGGER.log(Level.WARNING,
                                "Attempt #{0} was saved, but user streak refresh failed",
                                idIntentoCreado);
                        LOGGER.log(Level.FINE, "User streak refresh details", refreshError);
                    }
                }
                return attempt;
            } catch (SQLException | RuntimeException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new BusinessException(
                    "Error al procesar el intento en PKG_GAMIFICACION (verifica que 03_plsql_gamificacion.sql esté compilado): "
                    + e.getMessage(), e);
        }
    }

    @Override
    public List<Attempt> findByUserAndChallenge(int userId, int challengeId) {
        List<Attempt> recent = findRecentByUserAndChallenge(userId, challengeId, Integer.MAX_VALUE);
        java.util.Collections.reverse(recent);
        return recent;
    }

    @Override
    public List<Attempt> findRecentByUserAndChallenge(int userId, int challengeId, int limit) {
        if (limit <= 0) throw new IllegalArgumentException("El límite debe ser mayor que cero.");
        List<Attempt> list = new ArrayList<>();
        String sql = "SELECT * FROM (SELECT i.*,v.revision AS version_reto,v.enunciado AS enunciado_version "
                + "FROM Intentos i JOIN Version_Reto v ON v.id_version=i.id_version_reto "
                + "WHERE i.id_usuario = ? AND i.id_reto = ? "
                + "ORDER BY i.fecha_intento DESC,i.id_intento DESC) WHERE ROWNUM <= ?";

        User user = userDAO.findById(userId).orElse(null);
        Challenge challenge = challengeRepository.findById(challengeId).orElse(null);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, challengeId);
            stmt.setInt(3, limit);
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
                    a.setChallengeRevision(rs.getInt("version_reto"));
                    a.setChallengeStatement(rs.getString("enunciado_version"));
                    list.add(a);
                }
            }
        } catch (SQLException e) {
            throw new BusinessException("Error al listar intentos por usuario y reto en Oracle: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public Optional<Attempt> findByRequestId(int userId, int challengeId, String requestId) {
        String sql = "SELECT i.*,v.revision AS version_reto,v.enunciado AS enunciado_version "
                + "FROM Intentos i JOIN Version_Reto v ON v.id_version=i.id_version_reto "
                + "WHERE i.id_usuario=? AND i.id_reto=? AND i.id_solicitud=?";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId); statement.setInt(2, challengeId); statement.setString(3, requestId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) return Optional.empty();
                User user = userDAO.findById(userId).orElseThrow();
                com.mycompany.levelup_code.domain.model.CodeChallenge historical = new com.mycompany.levelup_code.domain.model.CodeChallenge();
                historical.setId(challengeId);
                historical.setRevision(result.getInt("version_reto"));
                historical.setDescription(result.getString("enunciado_version"));
                Attempt attempt = new Attempt(result.getInt("id_intento"), user, historical, result.getString("codigo_enviado"),
                        result.getInt("es_correcto") == 1 ? AttemptResult.CORRECT : parseResult(result.getString("salida_obtenida")),
                        result.getLong("tiempo_ejecucion_ms"), result.getInt("exp_obtenida"), result.getTimestamp("fecha_intento").toLocalDateTime());
                attempt.setChallengeRevision(result.getInt("version_reto"));
                attempt.setChallengeStatement(result.getString("enunciado_version"));
                attempt.setRequestId(requestId);
                return Optional.of(attempt);
            }
        } catch (SQLException ex) { throw new BusinessException("No se pudo comprobar el envío anterior.", ex); }
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
