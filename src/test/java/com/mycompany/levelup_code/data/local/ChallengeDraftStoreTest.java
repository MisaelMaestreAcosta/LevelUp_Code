package com.mycompany.levelup_code.data.local;

import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChallengeDraftStoreTest {

    @TempDir Path temp;

    @Test
    void preservesUnicodeDraftAndSeparatesUsersModesAndChallenges() throws IOException {
        ChallengeDraftStore store = new ChallengeDraftStore(temp.resolve("drafts"));
        ChallengeDraftStore.DraftData saved = new ChallengeDraftStore.DraftData(
                "System.out.println(\"¡Hola!\");\n", -1, 15, 32.5, true,
                "request-123", "payload-hash-123");

        store.save("oracle", "Estudiante@Ejemplo.com", 101, saved);
        assertEquals(saved, store.load("oracle", "estudiante@ejemplo.com", 101).orElseThrow());
        assertTrue(store.load("demo", "estudiante@ejemplo.com", 101).isEmpty());
        assertTrue(store.load("oracle", "otra@ejemplo.com", 101).isEmpty());
        assertTrue(store.load("oracle", "estudiante@ejemplo.com", 102).isEmpty());

        ChallengeDraftStore.DraftData updated = new ChallengeDraftStore.DraftData(
                "nuevo", 2, 5, 0, false, null, null);
        store.save("oracle", "estudiante@ejemplo.com", 101, updated);
        assertEquals(updated, store.load("oracle", "estudiante@ejemplo.com", 101).orElseThrow());
    }
}
