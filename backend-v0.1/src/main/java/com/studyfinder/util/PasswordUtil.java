package com.studyfinder.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Hashes and checks passwords with PBKDF2-HMAC-SHA256, which is built into Java,
 * so no extra library is needed.
 *
 * Stored format: pbkdf2_sha256$iterations$salt$hash (salt and hash in Base64).
 * Each password gets its own random salt, so two users with the same password
 * end up with different hashes. Plain-text passwords are never stored.
 *
 * To make a hash by hand (for seed data), run:
 *   java com.studyfinder.util.PasswordUtil "the password"
 */
public final class PasswordUtil {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String PREFIX = "pbkdf2_sha256";
    private static final int ITERATIONS = 600_000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;

    private static final SecureRandom RANDOM = new SecureRandom();

    // Used when an email isn't found, so that "no such account" takes as long
    // as "wrong password" and response times don't reveal which emails exist.
    private static final String DUMMY_HASH = hash("timing-equalizer-not-a-real-password");

    private PasswordUtil() {
    }

    public static String hash(String password) {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] key = derive(password, salt, ITERATIONS, KEY_BITS);

        Base64.Encoder encoder = Base64.getEncoder();
        return PREFIX + "$" + ITERATIONS + "$" + encoder.encodeToString(salt) + "$" + encoder.encodeToString(key);
    }

    public static boolean verify(String password, String storedHash) {
        if (password == null || password.isEmpty() || storedHash == null) {
            return false;
        }

        String[] parts = storedHash.split("\\$");
        if (parts.length != 4 || !PREFIX.equals(parts[0])) {
            return false;
        }

        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = derive(password, salt, iterations, expected.length * 8);

            // Constant-time comparison, so timing doesn't leak how many bytes matched.
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /** Does the same amount of work as verify() without checking a real account. */
    public static void simulateVerify(String password) {
        verify(password == null || password.isEmpty() ? "x" : password, DUMMY_HASH);
    }

    private static byte[] derive(String password, byte[] salt, int iterations, int keyBits) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyBits);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("PBKDF2 is not available in this Java runtime", e);
        } finally {
            spec.clearPassword();
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java com.studyfinder.util.PasswordUtil \"password\"");
            System.exit(1);
        }
        System.out.println(hash(args[0]));
    }
}
