package com.mycompany.levelup_code.data.oracle;

import com.mycompany.levelup_code.data.connection.DatabaseConnection;
import com.mycompany.levelup_code.data.dao.ContentRepository;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación Oracle JDBC de {@link ContentRepository} para la gestión y
 * consulta jerárquica de {@code Lenguajes}, {@code Modulos}, {@code Lecciones} y {@code Retos}.
 */
public class OracleContentRepository implements ContentRepository {

    private final OracleChallengeRepository challengeRepository = new OracleChallengeRepository();

    @Override
    public boolean existsLanguageByName(String name) {
        String sql = "SELECT 1 FROM Lenguajes WHERE LOWER(nombre) = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name.trim().toLowerCase(Locale.ROOT));
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new BusinessException("Error al verificar nombre de lenguaje en Oracle: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsLanguageByNameExceptId(String name, int excludedId) {
        String sql = "SELECT 1 FROM Lenguajes WHERE LOWER(nombre) = ? AND id_lenguaje <> ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name.trim().toLowerCase(Locale.ROOT));
            stmt.setInt(2, excludedId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new BusinessException("Error al validar unicidad de lenguaje en Oracle: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Language> findLanguageById(int id) {
        String sql = "SELECT id_lenguaje, nombre, descripcion, icono_url, activo FROM Lenguajes WHERE id_lenguaje = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Language lang = mapLanguageRow(rs);
                    loadModulesAndLessons(conn, lang);
                    return Optional.of(lang);
                }
            }
        } catch (SQLException e) {
            throw new BusinessException("Error al buscar lenguaje por ID en Oracle: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<CourseModule> findModuleById(int id) {
        String sql = "SELECT m.id_modulo, m.id_lenguaje, m.nombre_modulo, m.descripcion, m.orden_secuencia, "
                + "l.nombre AS lang_nombre, l.descripcion AS lang_desc, l.icono_url, l.activo "
                + "FROM Modulos m JOIN Lenguajes l ON l.id_lenguaje = m.id_lenguaje WHERE m.id_modulo = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Language lang = new Language(
                            rs.getInt("id_lenguaje"),
                            rs.getString("lang_nombre"),
                            rs.getString("lang_desc"),
                            rs.getString("icono_url"),
                            rs.getInt("activo") == 1
                    );
                    CourseModule mod = new CourseModule(
                            rs.getInt("id_modulo"),
                            rs.getString("nombre_modulo"),
                            rs.getString("descripcion"),
                            rs.getInt("orden_secuencia"),
                            lang
                    );
                    loadLessonsForModule(conn, mod);
                    return Optional.of(mod);
                }
            }
        } catch (SQLException e) {
            throw new BusinessException("Error al buscar módulo por ID en Oracle: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Lesson> findLessonById(int id) {
        String sql = "SELECT le.id_leccion, le.id_modulo, le.titulo, le.contenido, le.orden_secuencia, le.exp_recompensa, "
                + "m.nombre_modulo, m.descripcion AS mod_desc, m.orden_secuencia AS mod_orden, "
                + "lg.id_lenguaje, lg.nombre AS lang_nombre, lg.descripcion AS lang_desc, lg.icono_url, lg.activo "
                + "FROM Lecciones le "
                + "JOIN Modulos m ON m.id_modulo = le.id_modulo "
                + "JOIN Lenguajes lg ON lg.id_lenguaje = m.id_lenguaje "
                + "WHERE le.id_leccion = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Language lang = new Language(
                            rs.getInt("id_lenguaje"),
                            rs.getString("lang_nombre"),
                            rs.getString("lang_desc"),
                            rs.getString("icono_url"),
                            rs.getInt("activo") == 1
                    );
                    CourseModule mod = new CourseModule(
                            rs.getInt("id_modulo"),
                            rs.getString("nombre_modulo"),
                            rs.getString("mod_desc"),
                            rs.getInt("mod_orden"),
                            lang
                    );
                    Lesson lesson = new Lesson(
                            rs.getInt("id_leccion"),
                            rs.getString("titulo"),
                            rs.getString("contenido"),
                            rs.getInt("orden_secuencia"),
                            rs.getInt("exp_recompensa"),
                            mod
                    );
                    for (Challenge ch : challengeRepository.findByLessonId(conn, lesson)) {
                        lesson.addChallenge(ch);
                    }
                    return Optional.of(lesson);
                }
            }
        } catch (SQLException e) {
            throw new BusinessException("Error al buscar lección por ID en Oracle: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Challenge> findChallengeById(int id) {
        return challengeRepository.findById(id);
    }

    @Override
    public List<Language> findActiveLanguages() {
        List<Language> languages = new ArrayList<>();
        Map<Integer, Language> languagesById = new LinkedHashMap<>();
        String sql = "SELECT id_lenguaje, nombre, descripcion, icono_url, activo "
                + "FROM Lenguajes WHERE activo = 1 ORDER BY id_lenguaje ASC";
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Language lang = mapLanguageRow(rs);
                    languages.add(lang);
                    languagesById.put(lang.getId(), lang);
                }
            }
            loadActiveCatalog(conn, languagesById);
        } catch (SQLException e) {
            throw new BusinessException("Error al listar lenguajes activos en Oracle: " + e.getMessage(), e);
        }
        return languages;
    }

    private void loadActiveCatalog(Connection conn, Map<Integer, Language> languagesById)
            throws SQLException {
        if (languagesById.isEmpty()) return;

        Map<Integer, CourseModule> modulesById = new LinkedHashMap<>();
        String modulesSql = "SELECT m.id_modulo, m.id_lenguaje, m.nombre_modulo, "
                + "m.descripcion, m.orden_secuencia FROM Modulos m "
                + "JOIN Lenguajes l ON l.id_lenguaje = m.id_lenguaje "
                + "WHERE l.activo = 1 ORDER BY m.id_lenguaje, m.orden_secuencia";
        try (PreparedStatement stmt = conn.prepareStatement(modulesSql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Language language = languagesById.get(rs.getInt("id_lenguaje"));
                if (language == null) continue;
                CourseModule module = new CourseModule(
                        rs.getInt("id_modulo"),
                        rs.getString("nombre_modulo"),
                        rs.getString("descripcion"),
                        rs.getInt("orden_secuencia"),
                        language);
                language.addModule(module);
                modulesById.put(module.getId(), module);
            }
        }

        Map<Integer, Lesson> lessonsById = new LinkedHashMap<>();
        String lessonsSql = "SELECT le.id_leccion, le.id_modulo, le.titulo, le.contenido, "
                + "le.orden_secuencia, le.exp_recompensa FROM Lecciones le "
                + "JOIN Modulos m ON m.id_modulo = le.id_modulo "
                + "JOIN Lenguajes l ON l.id_lenguaje = m.id_lenguaje "
                + "WHERE l.activo = 1 "
                + "ORDER BY m.id_lenguaje, m.orden_secuencia, le.orden_secuencia";
        try (PreparedStatement stmt = conn.prepareStatement(lessonsSql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                CourseModule module = modulesById.get(rs.getInt("id_modulo"));
                if (module == null) continue;
                Lesson lesson = new Lesson(
                        rs.getInt("id_leccion"),
                        rs.getString("titulo"),
                        rs.getString("contenido"),
                        rs.getInt("orden_secuencia"),
                        rs.getInt("exp_recompensa"),
                        module);
                module.addLesson(lesson);
                lessonsById.put(lesson.getId(), lesson);
            }
        }

        challengeRepository.loadActiveCatalog(conn, lessonsById);
    }

    private Language mapLanguageRow(ResultSet rs) throws SQLException {
        return new Language(
                rs.getInt("id_lenguaje"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getString("icono_url"),
                rs.getInt("activo") == 1
        );
    }

    private void loadModulesAndLessons(Connection conn, Language lang) throws SQLException {
        String modSql = "SELECT id_modulo, nombre_modulo, descripcion, orden_secuencia "
                + "FROM Modulos WHERE id_lenguaje = ? ORDER BY orden_secuencia ASC";
        try (PreparedStatement stmt = conn.prepareStatement(modSql)) {
            stmt.setInt(1, lang.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CourseModule mod = new CourseModule(
                            rs.getInt("id_modulo"),
                            rs.getString("nombre_modulo"),
                            rs.getString("descripcion"),
                            rs.getInt("orden_secuencia"),
                            lang
                    );
                    loadLessonsForModule(conn, mod);
                    lang.addModule(mod);
                }
            }
        }
    }

    private void loadLessonsForModule(Connection conn, CourseModule mod) throws SQLException {
        String lesSql = "SELECT id_leccion, titulo, contenido, orden_secuencia, exp_recompensa "
                + "FROM Lecciones WHERE id_modulo = ? ORDER BY orden_secuencia ASC";
        try (PreparedStatement stmt = conn.prepareStatement(lesSql)) {
            stmt.setInt(1, mod.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Lesson lesson = new Lesson(
                            rs.getInt("id_leccion"),
                            rs.getString("titulo"),
                            rs.getString("contenido"),
                            rs.getInt("orden_secuencia"),
                            rs.getInt("exp_recompensa"),
                            mod
                    );
                    for (Challenge ch : challengeRepository.findByLessonId(conn, lesson)) {
                        lesson.addChallenge(ch);
                    }
                    mod.addLesson(lesson);
                }
            }
        }
    }

    @Override
    public Language saveLanguage(Language language) {
        String sql = "INSERT INTO Lenguajes (nombre, descripcion, icono_url, activo) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"ID_LENGUAJE"})) {
            stmt.setString(1, language.getName());
            stmt.setString(2, language.getDescription());
            stmt.setString(3, language.getIcon());
            stmt.setInt(4, language.isActive() ? 1 : 0);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    language.setId(rs.getInt(1));
                }
            }
            return language;
        } catch (SQLException e) {
            throw new BusinessException("Error al crear lenguaje en Oracle: " + e.getMessage(), e);
        }
    }

    @Override
    public CourseModule saveModule(CourseModule module) {
        String sql = "INSERT INTO Modulos (id_lenguaje, nombre_modulo, descripcion, orden_secuencia) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"ID_MODULO"})) {
            stmt.setInt(1, module.getLanguage().getId());
            stmt.setString(2, module.getTitle());
            stmt.setString(3, module.getDescription());
            stmt.setInt(4, Math.max(1, module.getOrderIndex()));
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    module.setId(rs.getInt(1));
                }
            }
            return module;
        } catch (SQLException e) {
            throw new BusinessException("Error al crear módulo en Oracle: " + e.getMessage(), e);
        }
    }

    @Override
    public Lesson saveLesson(Lesson lesson) {
        String sql = "INSERT INTO Lecciones (id_modulo, titulo, contenido, orden_secuencia, exp_recompensa) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"ID_LECCION"})) {
            stmt.setInt(1, lesson.getModule().getId());
            stmt.setString(2, lesson.getTitle());
            stmt.setString(3, lesson.getTheory());
            stmt.setInt(4, Math.max(1, lesson.getOrderIndex()));
            stmt.setInt(5, Math.max(0, lesson.getExpReward()));
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    lesson.setId(rs.getInt(1));
                }
            }
            return lesson;
        } catch (SQLException e) {
            throw new BusinessException("Error al crear lección en Oracle: " + e.getMessage(), e);
        }
    }

    @Override
    public Challenge saveChallenge(Challenge challenge) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                saveChallenge(conn, challenge);
                conn.commit();
                return challenge;
            } catch (SQLException | RuntimeException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new BusinessException("No se pudo crear el reto.", ex);
        }
    }

    /** Caller owns the transaction, allowing publication metadata to commit together. */
    public Challenge saveChallenge(Connection conn, Challenge challenge) throws SQLException {
        String baseSql = "INSERT INTO Retos (id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia) "
                + "VALUES (?, ?, ?, ?, ?, ?, ("
                + "SELECT NVL(MAX(orden_secuencia), 0) + 1 FROM Retos WHERE id_leccion = ?"
                + "))";

        try {
                try (PreparedStatement stmt = conn.prepareStatement(baseSql, new String[]{"ID_RETO"})) {
                    stmt.setInt(1, challenge.getLesson().getId());
                    stmt.setString(2, challenge.getTitle());
                    stmt.setString(3, challenge.getDescription());
                    stmt.setString(4, challenge instanceof QuizChallenge ? "QUIZ" : "CODIGO");
                    stmt.setString(5, challenge.getDifficulty().name());
                    stmt.setInt(6, challenge.getExpReward());
                    stmt.setInt(7, challenge.getLesson().getId());
                    stmt.executeUpdate();
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            challenge.setId(rs.getInt(1));
                        }
                    }
                }

                if (challenge instanceof CodeChallenge) {
                    CodeChallenge cc = (CodeChallenge) challenge;
                    String codeSql = "INSERT INTO Reto_Codigo (id_reto, codigo_inicial, limite_tiempo_ms) VALUES (?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(codeSql)) {
                        stmt.setInt(1, cc.getId());
                        stmt.setString(2, cc.getInitialTemplate());
                        stmt.setInt(3, Math.max(1000, cc.getTimeLimitSeconds() * 1000));
                        stmt.executeUpdate();
                    }

                    String caseSql = "INSERT INTO Casos_Prueba (id_reto, entrada, salida_esperada, es_oculto) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(caseSql)) {
                        for (TestCase tc : cc.getTestCases()) {
                            stmt.setInt(1, cc.getId());
                            stmt.setString(2, tc.getInput());
                            stmt.setString(3, tc.getExpectedOutput());
                            stmt.setInt(4, tc.isHidden() ? 1 : 0);
                            stmt.addBatch();
                        }
                        stmt.executeBatch();
                    }
                } else if (challenge instanceof QuizChallenge) {
                    QuizChallenge qc = (QuizChallenge) challenge;
                    String quizSql = "INSERT INTO Reto_Quiz (id_reto, opciones, respuesta_correcta) VALUES (?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(quizSql)) {
                        stmt.setInt(1, qc.getId());
                        stmt.setString(2, QuizOptionCodec.encode(qc.getOptions()));
                        stmt.setString(3, qc.getCorrectAnswer());
                        stmt.executeUpdate();
                    }
                }

                return challenge;
        } catch (SQLException ex) {
            throw ex;
        }
    }

    @Override
    public Language updateLanguage(Language language) {
        String sql = "UPDATE Lenguajes SET nombre = ?, descripcion = ?, icono_url = ?, activo = ? WHERE id_lenguaje = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, language.getName());
            stmt.setString(2, language.getDescription());
            stmt.setString(3, language.getIcon());
            stmt.setInt(4, language.isActive() ? 1 : 0);
            stmt.setInt(5, language.getId());
            stmt.executeUpdate();
            return language;
        } catch (SQLException e) {
            throw new BusinessException("Error al actualizar lenguaje en Oracle: " + e.getMessage(), e);
        }
    }

    @Override
    public CourseModule updateModule(CourseModule module) {
        String sql = "UPDATE Modulos SET nombre_modulo = ?, descripcion = ?, orden_secuencia = ? WHERE id_modulo = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, module.getTitle());
            stmt.setString(2, module.getDescription());
            stmt.setInt(3, module.getOrderIndex());
            stmt.setInt(4, module.getId());
            stmt.executeUpdate();
            return module;
        } catch (SQLException e) {
            throw new BusinessException("Error al actualizar módulo en Oracle: " + e.getMessage(), e);
        }
    }

    @Override
    public Lesson updateLesson(Lesson lesson) {
        String sql = "UPDATE Lecciones SET titulo = ?, contenido = ?, orden_secuencia = ?, exp_recompensa = ? WHERE id_leccion = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, lesson.getTitle());
            stmt.setString(2, lesson.getTheory());
            stmt.setInt(3, lesson.getOrderIndex());
            stmt.setInt(4, lesson.getExpReward());
            stmt.setInt(5, lesson.getId());
            stmt.executeUpdate();
            return lesson;
        } catch (SQLException e) {
            throw new BusinessException("Error al actualizar lección en Oracle: " + e.getMessage(), e);
        }
    }

    @Override
    public Challenge updateChallenge(Challenge challenge) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                updateChallenge(conn, challenge);
                conn.commit();
                return challenge;
            } catch (SQLException | RuntimeException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new BusinessException("No se pudo actualizar el reto.", ex);
        }
    }

    public Challenge updateChallenge(Connection conn, Challenge challenge) throws SQLException {
        String baseSql = "UPDATE Retos SET titulo_reto = ?, enunciado = ?, "
                + "exp_recompensa = ?, dificultad = ? WHERE id_reto = ?";
        try {
                try (PreparedStatement stmt = conn.prepareStatement(baseSql)) {
                    stmt.setString(1, challenge.getTitle());
                    stmt.setString(2, challenge.getDescription());
                    stmt.setInt(3, challenge.getExpReward());
                    stmt.setString(4, challenge.getDifficulty().name());
                    stmt.setInt(5, challenge.getId());
                    if (stmt.executeUpdate() != 1) {
                        throw new SQLException("El reto ya no existe.");
                    }
                }

                if (challenge instanceof CodeChallenge code) {
                    String codeSql = "UPDATE Reto_Codigo SET codigo_inicial = ?, limite_tiempo_ms = ? "
                            + "WHERE id_reto = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(codeSql)) {
                        stmt.setString(1, code.getInitialTemplate());
                        stmt.setInt(2, Math.max(1000, code.getTimeLimitSeconds() * 1000));
                        stmt.setInt(3, code.getId());
                        if (stmt.executeUpdate() != 1) {
                            throw new SQLException("No se encontró la configuración de código del reto.");
                        }
                    }
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "DELETE FROM Casos_Prueba WHERE id_reto = ?")) {
                        stmt.setInt(1, code.getId());
                        stmt.executeUpdate();
                    }
                    String caseSql = "INSERT INTO Casos_Prueba "
                            + "(id_reto, entrada, salida_esperada, es_oculto) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(caseSql)) {
                        for (TestCase testCase : code.getTestCases()) {
                            stmt.setInt(1, code.getId());
                            stmt.setString(2, testCase.getInput());
                            stmt.setString(3, testCase.getExpectedOutput());
                            stmt.setInt(4, testCase.isHidden() ? 1 : 0);
                            stmt.addBatch();
                        }
                        stmt.executeBatch();
                    }
                } else if (challenge instanceof QuizChallenge quiz) {
                    String quizSql = "UPDATE Reto_Quiz SET opciones = ?, respuesta_correcta = ? "
                            + "WHERE id_reto = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(quizSql)) {
                        stmt.setString(1, QuizOptionCodec.encode(quiz.getOptions()));
                        stmt.setString(2, quiz.getCorrectAnswer());
                        stmt.setInt(3, quiz.getId());
                        if (stmt.executeUpdate() != 1) {
                            throw new SQLException("No se encontró la configuración del quiz.");
                        }
                    }
                }

                return challenge;
        } catch (SQLException ex) {
            throw ex;
        }
    }
}

