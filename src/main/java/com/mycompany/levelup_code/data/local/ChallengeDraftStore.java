package com.mycompany.levelup_code.data.local;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;

/** Keeps a student's unfinished answer on this computer across app restarts. */
public final class ChallengeDraftStore {

    private static final int VERSION = 1;
    private static final int MAX_CODE_BYTES = 1_048_576;
    private final Path directory;

    public record DraftData(String code, int quizIndex, int caret, double scrollTop,
            boolean saveUncertain) { }

    public ChallengeDraftStore() {
        this(Path.of(System.getProperty("user.home"), ".levelup-code", "drafts"));
    }

    public ChallengeDraftStore(Path directory) {
        this.directory = directory;
    }

    public Optional<DraftData> load(String mode, String email, int challengeId) throws IOException {
        Path file = draftPath(mode, email, challengeId);
        if (!Files.isRegularFile(file)) return Optional.empty();
        try (DataInputStream input = new DataInputStream(Files.newInputStream(file))) {
            if (input.readInt() != VERSION) throw new IOException("Versión de borrador no compatible.");
            int length = input.readInt();
            if (length < 0 || length > MAX_CODE_BYTES) throw new IOException("Borrador demasiado grande.");
            byte[] code = new byte[length];
            input.readFully(code);
            return Optional.of(new DraftData(new String(code, StandardCharsets.UTF_8),
                    input.readInt(), input.readInt(), input.readDouble(), input.readBoolean()));
        }
    }

    public void save(String mode, String email, int challengeId, DraftData draft) throws IOException {
        Path file = draftPath(mode, email, challengeId);
        byte[] code = (draft.code() == null ? "" : draft.code()).getBytes(StandardCharsets.UTF_8);
        if (code.length > MAX_CODE_BYTES) throw new IOException("Borrador demasiado grande.");
        Files.createDirectories(directory);
        Path temporary = Files.createTempFile(directory, "draft-", ".tmp");
        try {
            try (DataOutputStream output = new DataOutputStream(Files.newOutputStream(temporary))) {
                output.writeInt(VERSION);
                output.writeInt(code.length);
                output.write(code);
                output.writeInt(draft.quizIndex());
                output.writeInt(draft.caret());
                output.writeDouble(draft.scrollTop());
                output.writeBoolean(draft.saveUncertain());
            }
            try {
                Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private Path draftPath(String mode, String email, int challengeId) {
        if (mode == null || email == null || email.isBlank() || challengeId <= 0) {
            throw new IllegalArgumentException("El usuario y el reto deben ser válidos.");
        }
        String identity = mode + ":" + email.trim().toLowerCase(Locale.ROOT) + ":" + challengeId;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(identity.getBytes(StandardCharsets.UTF_8));
            return directory.resolve(HexFormat.of().formatHex(digest) + ".draft");
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no está disponible.", ex);
        }
    }
}
