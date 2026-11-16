package com.mycompany.levelup_code.data.local;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;

/** Named experiments stay in the same account and language scope as their working drafts. */
public final class LabLibraryStore {
    public record Entry(String id, String name, Instant modified) {
        @Override public String toString() { return name; }
    }
    private final Path directory;

    public LabLibraryStore() {
        this(Path.of(System.getProperty("user.home"), ".levelup-code", "experiments"));
    }
    public LabLibraryStore(Path directory) { this.directory = directory; }

    public List<Entry> list(String identity, String language) throws IOException {
        Path scope = scope(identity, language);
        if (!Files.isDirectory(scope)) return List.of();
        List<Entry> entries = new ArrayList<>();
        try (var files = Files.newDirectoryStream(scope, "*.properties")) {
            for (Path file : files) {
                Properties properties = read(file);
                String id = file.getFileName().toString().replace(".properties", "");
                entries.add(new Entry(id, properties.getProperty("name", "Sin título"), Files.getLastModifiedTime(file).toInstant()));
            }
        }
        entries.sort(Comparator.comparing(Entry::modified).reversed().thenComparing(Entry::name));
        return List.copyOf(entries);
    }

    public LabDraftStore.Experiment load(String identity, String language, String id) throws IOException {
        Properties properties = read(file(identity, language, id));
        return new LabDraftStore.Experiment(properties.getProperty("code", ""), properties.getProperty("input", ""),
                id, properties.getProperty("name", "Sin título"));
    }

    public void save(String identity, String language, LabDraftStore.Experiment experiment) throws IOException {
        if (experiment.name().isBlank() || experiment.name().length() > 100) {
            throw new IllegalArgumentException("Escribe un nombre de hasta 100 caracteres.");
        }
        if (experiment.code().getBytes(StandardCharsets.UTF_8).length > 524_288
                || experiment.input().getBytes(StandardCharsets.UTF_8).length > 65_536) {
            throw new IOException("Reduce el código a 512 KB y la entrada a 64 KB.");
        }
        Path destination = file(identity, language, experiment.libraryId());
        Files.createDirectories(destination.getParent());
        Path temporary = Files.createTempFile(destination.getParent(), "experiment-", ".tmp");
        try {
            Properties properties = new Properties();
            properties.setProperty("name", experiment.name().strip());
            properties.setProperty("code", experiment.code());
            properties.setProperty("input", experiment.input());
            try (var output = Files.newOutputStream(temporary)) { properties.store(output, "LevelUp experiment"); }
            try {
                Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally { Files.deleteIfExists(temporary); }
    }

    public void delete(String identity, String language, String id) throws IOException {
        Files.deleteIfExists(file(identity, language, id));
    }

    private Properties read(Path file) throws IOException {
        if (Files.size(file) > 4_194_304) throw new IOException("El experimento supera el tamaño permitido.");
        Properties properties = new Properties();
        try (var input = Files.newInputStream(file)) { properties.load(input); }
        return properties;
    }

    private Path file(String identity, String language, String id) {
        String safeId = UUID.fromString(id).toString();
        return scope(identity, language).resolve(safeId + ".properties");
    }

    private Path scope(String identity, String language) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest((identity + "\n" + language).getBytes(StandardCharsets.UTF_8));
            return directory.resolve(HexFormat.of().formatHex(hash));
        } catch (NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
    }
}
