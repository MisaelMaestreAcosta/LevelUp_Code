package com.mycompany.levelup_code.data.local;

import com.mycompany.levelup_code.data.oracle.OracleNotebookRepository.Note;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;

/** A recovery copy of the current note, scoped to the signed-in account. */
public final class NotebookDraftStore {
    private final Path directory;

    public NotebookDraftStore() {
        this(Path.of(System.getProperty("user.home"), ".levelup-code", "notebook-recovery"));
    }

    public NotebookDraftStore(Path directory) { this.directory = directory; }

    public Optional<Note> load(String identity) throws IOException {
        Path file = file(identity);
        if (!Files.exists(file)) return Optional.empty();
        Properties properties = new Properties();
        try (var input = Files.newInputStream(file)) { properties.load(input); }
        try {
            String challenge = properties.getProperty("challenge", "");
            return Optional.of(new Note(Integer.parseInt(properties.getProperty("id", "0")),
                    challenge.isEmpty() ? null : Integer.valueOf(challenge),
                    properties.getProperty("title", ""), properties.getProperty("content", "")));
        } catch (NumberFormatException ex) {
            throw new IOException("El borrador del cuaderno no se pudo leer.", ex);
        }
    }

    public void save(String identity, Note note) throws IOException {
        Files.createDirectories(directory);
        Path temporary = Files.createTempFile(directory, "note-", ".tmp");
        try {
            Properties properties = new Properties();
            properties.setProperty("id", Integer.toString(note.id()));
            properties.setProperty("challenge", note.challengeId() == null ? "" : note.challengeId().toString());
            properties.setProperty("title", note.title());
            properties.setProperty("content", note.content());
            try (var output = Files.newOutputStream(temporary)) { properties.store(output, "Notebook recovery"); }
            try {
                Files.move(temporary, file(identity), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporary, file(identity), StandardCopyOption.REPLACE_EXISTING);
            }
        } finally { Files.deleteIfExists(temporary); }
    }

    public void remove(String identity) throws IOException { Files.deleteIfExists(file(identity)); }

    private Path file(String identity) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(identity.trim().toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8));
            return directory.resolve(HexFormat.of().formatHex(hash) + ".properties");
        } catch (NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
    }
}
