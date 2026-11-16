package com.mycompany.levelup_code.data.local;

import java.nio.file.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class LabLibraryStoreTest {
    @TempDir Path directory;

    @Test void namedExperimentsRemainIndependentAndPreserveRecoveryMetadata() throws Exception {
        var library = new LabLibraryStore(directory.resolve("library"));
        String first = UUID.randomUUID().toString(), second = UUID.randomUUID().toString();
        var a = new LabDraftStore.Experiment("// ñ\nclass A {}", "á", first, "Primer trabajo");
        var b = new LabDraftStore.Experiment("class B {}", "", second, "Segundo trabajo");
        library.save("demo:one", "Java", a);
        library.save("demo:one", "Java", b);
        assertEquals(2, library.list("demo:one", "Java").size());
        assertEquals(a, library.load("demo:one", "Java", first));
        assertTrue(library.list("oracle:one", "Java").isEmpty());
        assertTrue(library.list("demo:one", "Python").isEmpty());
        assertTrue(library.list("demo:two", "Java").isEmpty());
        var changed = new LabDraftStore.Experiment("nuevo", "", first, "Renombrado");
        library.save("demo:one", "Java", changed);
        assertEquals(2, library.list("demo:one", "Java").size());
        assertEquals(changed, library.load("demo:one", "Java", first));
        library.delete("demo:one", "Java", first);
        assertEquals(b, library.load("demo:one", "Java", second));
        assertThrows(IllegalArgumentException.class, () -> library.load("demo:one", "Java", "../other"));
        var drafts = new LabDraftStore(directory.resolve("drafts"));
        drafts.save("demo:one", "Java", changed);
        assertEquals(changed, drafts.load("demo:one", "Java").orElseThrow());
    }
}
