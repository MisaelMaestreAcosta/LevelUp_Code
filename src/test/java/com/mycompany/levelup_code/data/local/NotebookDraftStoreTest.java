package com.mycompany.levelup_code.data.local;

import com.mycompany.levelup_code.data.oracle.OracleNotebookRepository.Note;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class NotebookDraftStoreTest {
    @TempDir Path directory;

    @Test
    void recoversIncompleteUnicodeNotesAndSeparatesAccounts() throws Exception {
        var store = new NotebookDraftStore(directory);
        var note = new Note(12, 101, "", "Código pendiente\nSystem.out.println(\"¡Hola!\");");
        store.save("oracle:Uno@example.com", note);
        assertEquals(note, store.load("oracle:uno@example.com").orElseThrow());
        assertTrue(store.load("oracle:otro@example.com").isEmpty());
        assertTrue(store.load("demo:uno@example.com").isEmpty());
        var replacement = new Note(0, null, "Nueva nota", "");
        store.save("oracle:uno@example.com", replacement);
        assertEquals(replacement, store.load("oracle:uno@example.com").orElseThrow());
        store.remove("oracle:uno@example.com");
        assertTrue(store.load("oracle:uno@example.com").isEmpty());
    }
}
