package com.mycompany.levelup_code.data.oracle;

import com.mycompany.levelup_code.data.connection.DatabaseConnection;
import com.mycompany.levelup_code.domain.model.Language;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OracleCatalogIntegrationTest {

    @Test
    void loadsTheCompletePublishedCatalog() throws Exception {
        Assumptions.assumeTrue(DatabaseConnection.isDatabaseReady(), "Oracle local no disponible.");
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "SELECT COUNT(*) FROM user_objects WHERE object_name IN "
                     + "('SOLUCION_REFERENCIA','VERSION_RETO','VERSION_CASO_PRUEBA',"
                     + "'RETO_PREDICCION','RETO_ORDENAMIENTO','TRG_INTENTO_VERSION')")) {
            result.next();
            Assumptions.assumeTrue(result.getInt(1) == 6,
                    "Instala database/08_content_publication.sql, 09_content_versions.sql y 10_challenge_types.sql.");
        }

        List<Language> languages = new OracleContentRepository().findActiveLanguages();

        assertFalse(languages.isEmpty(), "El catálogo debe contener al menos un lenguaje activo.");
        assertTrue(languages.stream().flatMap(language -> language.getModules().stream()).findAny().isPresent(),
                "El catálogo debe cargar sus módulos.");
        assertTrue(languages.stream().flatMap(language -> language.getModules().stream())
                        .flatMap(module -> module.getLessons().stream()).findAny().isPresent(),
                "El catálogo debe cargar sus lecciones.");
        assertTrue(languages.stream().flatMap(language -> language.getModules().stream())
                        .flatMap(module -> module.getLessons().stream())
                        .flatMap(lesson -> lesson.getChallenges().stream()).findAny().isPresent(),
                "El catálogo debe cargar sus retos publicados.");
    }
}
