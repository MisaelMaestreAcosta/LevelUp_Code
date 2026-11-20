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
import com.mycompany.levelup_code.domain.model.DebuggingChallenge;
import com.mycompany.levelup_code.domain.model.OutputPredictionChallenge;
import com.mycompany.levelup_code.domain.model.CodeOrderingChallenge;
import com.mycompany.levelup_code.exception.BusinessException;
import java.sql.Connection;
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

    @Override
    public Optional<Challenge> findById(int challengeId) {
        try (Connection conn = DatabaseConnection.getConnection();
             var stmt = OracleCalls.cursor(conn, "PKG_RETOS.reto_publicado", challengeId)) {
            try (ResultSet rs = OracleCalls.rows(stmt)) {
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
        try (var stmt = OracleCalls.cursor(conn, "PKG_RETOS.retos_leccion", lesson.getId())) {
            try (ResultSet rs = OracleCalls.rows(stmt)) {
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
        try (var stmt = OracleCalls.cursor(conn, "PKG_RETOS.catalogo_retos");
             ResultSet rs = OracleCalls.rows(stmt)) {
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

        OracleGuidanceRepository.load(conn, lessonsById.values().stream()
                .flatMap(lesson -> lesson.getChallenges().stream()).toList());
        OracleModuleFinalRepository.load(conn, lessonsById.values().stream()
                .flatMap(lesson -> lesson.getChallenges().stream()).toList());
        if (codeChallengesById.isEmpty()) return;
        try (var stmt = OracleCalls.cursor(conn, "PKG_RETOS.casos_catalogo");
             ResultSet rs = OracleCalls.rows(stmt)) {
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
        try (var statement = OracleCalls.cursor(conn, "PKG_RETOS.reto_editor", challengeId)) {
            try (ResultSet result = OracleCalls.rows(statement)) {
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
        OracleGuidanceRepository.load(conn, List.of(challenge));
        OracleModuleFinalRepository.load(conn, List.of(challenge));
        if (!(challenge instanceof CodeChallenge codeChallenge)) return challenge;

        try (var caseStmt = OracleCalls.cursor(conn, "PKG_RETOS.casos_reto", codeChallenge.getId())) {
            try (ResultSet caseRs = OracleCalls.rows(caseStmt)) {
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

        if ("ORDENAR".equals(type)) {
            CodeOrderingChallenge ordering = new CodeOrderingChallenge(id, title, statement, lesson, expReward,
                    QuizOptionCodec.decode(rs.getString("fragmentos")), CodeOrderingChallenge.parseOrder(rs.getString("orden_correcto")));
            ordering.setDifficulty(ChallengeDifficulty.valueOf(rs.getString("dificultad")));
            ordering.setRevision(rs.getInt("revision"));
            return ordering;
        }
        if ("QUIZ".equalsIgnoreCase(type) || "PREDICCION".equals(type)) {
            String rawOptions = rs.getString("opciones");
            List<String> options = QuizOptionCodec.decode(rawOptions);
            String correctAnswer = rs.getString("respuesta_correcta");
            QuizChallenge quiz = "PREDICCION".equals(type)
                    ? new OutputPredictionChallenge(id, title, statement, lesson, expReward, options, correctAnswer, rs.getString("codigo_ejemplo"))
                    : new QuizChallenge(id, title, statement, lesson, expReward, options, correctAnswer);
            quiz.setDifficulty(ChallengeDifficulty.valueOf(rs.getString("dificultad")));
            quiz.setRevision(rs.getInt("revision"));
            return quiz;
        }

        String template = rs.getString("codigo_inicial");
        int limitMs = rs.getInt("limite_tiempo_ms");
        int limitSeconds = Math.max(1, limitMs / 1000);

        CodeChallenge codeChallenge = "DEPURACION".equals(type)
                ? new DebuggingChallenge(id, title, statement, lesson, expReward, template, limitSeconds)
                : new CodeChallenge(id, title, statement, lesson, expReward, template, limitSeconds);
        codeChallenge.setDifficulty(ChallengeDifficulty.valueOf(rs.getString("dificultad")));
        codeChallenge.setRevision(rs.getInt("revision"));
        return codeChallenge;
    }
}

