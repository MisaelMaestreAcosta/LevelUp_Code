package com.mycompany.levelup_code.data.local;

import java.io.IOException;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/** One experiment per account, mode and language, saved atomically on this computer. */
public final class LabDraftStore {
    public record Experiment(String code, String input) { }
    private final Path directory;

    public LabDraftStore() {
        this(Path.of(System.getProperty("user.home"), ".levelup-code", "laboratory"));
    }

    public LabDraftStore(Path directory) { this.directory = directory; }

    public Optional<Experiment> load(String identity, String language) throws IOException {
        Path file = file(identity, language);
        if (!Files.exists(file)) return Optional.empty();
        if (Files.size(file) > 4_194_304) throw new IOException("Experimento demasiado grande.");
        Properties properties = new Properties();
        try (var input = Files.newInputStream(file)) { properties.load(input); }
        return Optional.of(new Experiment(properties.getProperty("code", ""), properties.getProperty("input", "")));
    }

    public void save(String identity, String language, Experiment experiment) throws IOException {
        if (experiment.code().getBytes(StandardCharsets.UTF_8).length > 524_288
                || experiment.input().getBytes(StandardCharsets.UTF_8).length > 65_536) {
            throw new IOException("Reduce el código a 512 KB y la entrada a 64 KB.");
        }
        Files.createDirectories(directory);
        Path temporary = Files.createTempFile(directory, "lab-", ".tmp");
        try {
            Properties properties = new Properties();
            properties.setProperty("code", experiment.code());
            properties.setProperty("input", experiment.input());
            try (var output = Files.newOutputStream(temporary)) { properties.store(output, "LevelUp laboratory"); }
            try {
                Files.move(temporary, file(identity, language), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporary, file(identity, language), StandardCopyOption.REPLACE_EXISTING);
            }
        } finally { Files.deleteIfExists(temporary); }
    }

    private Path file(String identity, String language) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest((identity + "\n" + language).getBytes(StandardCharsets.UTF_8));
            return directory.resolve(HexFormat.of().formatHex(hash) + ".properties");
        } catch (NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
    }
}
