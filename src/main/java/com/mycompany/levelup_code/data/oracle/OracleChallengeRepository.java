package com.mycompany.levelup_code.data.oracle;

import com.mycompany.levelup_code.data.connection.DatabaseConnection;
import com.mycompany.levelup_code.data.dao.ChallengeRepository;
import com.mycompany.levelup_code.domain.enums.ChallengeDifficulty;
import com.mycompany.levelup_code.domain.model.Challenge;
import com.mycompany.levelup_code.domain.model.CodeChallenge;
import com.mycompany.levelup_code.domain.model.CourseModule;
import com.mycompany.levelup_code.domain.model.Language;
import com.mycompany.levelup_code.domain.model.Lesson;
import com.mycompany.levelup_code.domain.model.QuizChallenge;
import com.mycompany.levelup_code.domain.model.TestCase;
import com.mycompany.levelup_code.exception.BusinessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repositorio Oracle JDBC para la consulta polimórfica de retos ({@code Retos},
 * {@code Reto_Codigo}, {@code Casos_Prueba} y {@code Reto_Quiz}).
 */
public class OracleChallengeRepository implements ChallengeRepository {

    private static final String SELECT_CHALLENGE_SQL =
            "SELECT r.id_reto, r.id_leccion, r.titulo_reto, r.enunciado, r.tipo_reto, "
            + "r.dificultad, r.exp_recompensa, r.orden_secuencia, "
            + "rc.codigo_inicial, rc.limite_tiempo_ms, "
            + "rq.opciones, rq.respuesta_correcta, "
            + "l.titulo AS leccion_titulo, l.contenido AS leccion_contenido, "
            + "l.orden_secuencia AS leccion_orden, l.exp_recompensa AS leccion_exp, "
            + "m.id_modulo, m.nombre_modulo, m.descripcion AS modulo_desc, m.orden_secuencia AS modulo_orden, "
            + "lg.id_lenguaje, lg.nombre AS lenguaje_nombre, lg.descripcion AS lenguaje_desc, "
            + "lg.icono_url, lg.activo "
            + "FROM Retos r "
            + "JOIN Lecciones l ON l.id_leccion = r.id_leccion "
            + "JOIN Modulos m ON m.id_modulo = l.id_modulo "
            + "JOIN Lenguajes lg ON lg.id_lenguaje = m.id_lenguaje "
            + "LEFT JOIN Reto_Codigo rc ON rc.id_reto = r.id_reto "
            + "LEFT JOIN Reto_Quiz rq ON rq.id_reto = r.id_reto ";

    @Override
    public Optional<Challenge> findById(int challengeId) {
        String sql = SELECT_CHALLENGE_SQL + "WHERE r.id_reto = ? AND r.estado='PUBLICADO' AND lg.activo=1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, challengeId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapChallenge(conn, rs));
                }
            }
        } catch (SQLException e) {
            throw new BusinessException("Error al consultar reto en Oracle: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public List<Challenge> findByLessonId(Connection conn, Lesson lesson) throws SQLException {
        List<Challenge> challenges = new ArrayList<>();
        String sql = SELECT_CHALLENGE_SQL + "WHERE r.id_leccion = ? AND r.estado='PUBLICADO' ORDER BY r.orden_secuencia ASC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, lesson.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Challenge ch = mapChallengeWithLesson(conn, rs, lesson);
                    challenges.add(ch);
                }
            }
        }
        return challenges;
    }

    /** Loads every challenge and test case for the active catalog without N+1 queries. */
    void loadActiveCatalog(Connection conn, Map<Integer, Lesson> lessonsById) throws SQLException {
        if (lessonsById.isEmpty()) return;

        Map<Integer, CodeChallenge> codeChallengesById = new HashMap<>();
        String sql = SELECT_CHALLENGE_SQL + "WHERE lg.activo = 1 AND r.estado='PUBLICADO' "
                + "ORDER BY lg.id_lenguaje, m.orden_secuencia, l.orden_secuencia, r.orden_secuencia";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Lesson lesson = lessonsById.get(rs.getInt("id_leccion"));
                if (lesson == null) continue;
                Challenge challenge = mapChallengeRow(rs, lesson);
                lesson.addChallenge(challenge);
                if (challenge instanceof CodeChallenge codeChallenge) {
                    codeChallengesById.put(codeChallenge.getId(), codeChallenge);
                }
            }
        }

        if (codeChallengesById.isEmpty()) return;
        String casesSql = "SELECT cp.id_caso, cp.id_reto, cp.entrada, cp.salida_esperada, cp.es_oculto "
                + "FROM Casos_Prueba cp "
                + "JOIN Retos r ON r.id_reto = cp.id_reto "
                + "JOIN Lecciones le ON le.id_leccion = r.id_leccion "
                + "JOIN Modulos m ON m.id_modulo = le.id_modulo "
                + "JOIN Lenguajes lg ON lg.id_lenguaje = m.id_lenguaje "
                + "WHERE lg.activo = 1 AND r.estado='PUBLICADO' ORDER BY cp.id_reto, cp.id_caso";
        try (PreparedStatement stmt = conn.prepareStatement(casesSql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                CodeChallenge challenge = codeChallengesById.get(rs.getInt("id_reto"));
                if (challenge == null) continue;
                challenge.addTestCase(new TestCase(
                        rs.getInt("id_caso"),
                        challenge,
                        rs.getString("entrada"),
                        rs.getString("salida_esperada"),
                        rs.getInt("es_oculto") == 1));
            }
        }
    }

    Challenge findForAuthoring(Connection conn, int challengeId) throws SQLException {
        try (PreparedStatement statement = conn.prepareStatement(SELECT_CHALLENGE_SQL + "WHERE r.id_reto=?")) {
            statement.setInt(1, challengeId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) throw new IllegalArgumentException("El reto ya no existe.");
                return mapChallenge(conn, result);
            }
        }
    }

    private Challenge mapChallenge(Connection conn, ResultSet rs) throws SQLException {
        Language lang = new Language(
                rs.getInt("id_lenguaje"),
                rs.getString("lenguaje_nombre"),
                rs.getString("lenguaje_desc"),
                rs.getString("icono_url"),
                rs.getInt("activo") == 1
        );
        CourseModule mod = new CourseModule(
                rs.getInt("id_modulo"),
                rs.getString("nombre_modulo"),
                rs.getString("modulo_desc"),
                rs.getInt("modulo_orden"),
                lang
        );
        Lesson lesson = new Lesson(
                rs.getInt("id_leccion"),
                rs.getString("leccion_titulo"),
                rs.getString("leccion_contenido"),
                rs.getInt("leccion_orden"),
                rs.getInt("leccion_exp"),
                mod
        );
        return mapChallengeWithLesson(conn, rs, lesson);
    }

    private Challenge mapChallengeWithLesson(Connection conn, ResultSet rs, Lesson lesson) throws SQLException {
        Challenge challenge = mapChallengeRow(rs, lesson);
        if (!(challenge instanceof CodeChallenge codeChallenge)) return challenge;

        String casesSql = "SELECT id_caso, entrada, salida_esperada, es_oculto "
                + "FROM Casos_Prueba WHERE id_reto = ? ORDER BY id_caso ASC";
        try (PreparedStatement caseStmt = conn.prepareStatement(casesSql)) {
            caseStmt.setInt(1, codeChallenge.getId());
            try (ResultSet caseRs = caseStmt.executeQuery()) {
                while (caseRs.next()) {
                    codeChallenge.addTestCase(new TestCase(
                            caseRs.getInt("id_caso"),
                            codeChallenge,
                            caseRs.getString("entrada"),
                            caseRs.getString("salida_esperada"),
                            caseRs.getInt("es_oculto") == 1));
                }
            }
        }
        return codeChallenge;
    }

    private Challenge mapChallengeRow(ResultSet rs, Lesson lesson) throws SQLException {
        int id = rs.getInt("id_reto");
        String title = rs.getString("titulo_reto");
        String statement = rs.getString("enunciado");
        String type = rs.getString("tipo_reto");
        int expReward = rs.getInt("exp_recompensa");

        if ("QUIZ".equalsIgnoreCase(type)) {
            String rawOptions = rs.getString("opciones");
            List<String> options = QuizOptionCodec.decode(rawOptions);
            String correctAnswer = rs.getString("respuesta_correcta");
            QuizChallenge quiz = new QuizChallenge(id, title, statement, lesson, expReward, options, correctAnswer);
            quiz.setDifficulty(ChallengeDifficulty.valueOf(rs.getString("dificultad")));
            return quiz;
        }

        String template = rs.getString("codigo_inicial");
        int limitMs = rs.getInt("limite_tiempo_ms");
        int limitSeconds = Math.max(1, limitMs / 1000);

        CodeChallenge codeChallenge = new CodeChallenge(
                id, title, statement, lesson, expReward, template, limitSeconds);
        codeChallenge.setDifficulty(ChallengeDifficulty.valueOf(rs.getString("dificultad")));
        return codeChallenge;
    }
}

