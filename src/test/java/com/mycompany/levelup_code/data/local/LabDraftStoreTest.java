package com.mycompany.levelup_code.data.local;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class LabDraftStoreTest {
    @TempDir Path directory;

    @Test void preservesUnicodeAndSeparatesAccountsLanguagesAndModes() throws Exception {
        LabDraftStore store = new LabDraftStore(directory);
        var experiment = new LabDraftStore.Experiment("// ñ\nclass Hola {}", "á\n\t=:");
        store.save("oracle:student@example.com", "Java", experiment);
        assertEquals(experiment, store.load("oracle:student@example.com", "Java").orElseThrow());
        assertTrue(store.load("oracle:other@example.com", "Java").isEmpty());
        assertTrue(store.load("demo:student@example.com", "Java").isEmpty());
        assertTrue(store.load("oracle:student@example.com", "Python").isEmpty());
    }
}
