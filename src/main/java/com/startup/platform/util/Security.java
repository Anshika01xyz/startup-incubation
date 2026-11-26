package com.startup.platform.util;

import com.startup.platform.config.*;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/** Password hashing for the coursework application using PBKDF2-HMAC-SHA256. */
public final class Security {
    private static final int ITERATIONS = 210_000;
    private static final int KEY_BITS = 256;
    private Security() {}
    public static String hash(String password) {
        try {
            byte[] salt = new byte[16]; new SecureRandom().nextBytes(salt);
            byte[] derived = derive(password.toCharArray(), salt, ITERATIONS);
            return "pbkdf2$" + ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt) + "$" + Base64.getEncoder().encodeToString(derived);
        } catch (Exception e) { throw new IllegalStateException("Could not hash password", e); }
    }
    public static boolean verify(String password, String stored) {
        try {
            String[] parts = stored.split("\\$");
            if (parts.length != 4 || !parts[0].equals("pbkdf2")) return false;
            int rounds = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            return java.security.MessageDigest.isEqual(expected, derive(password.toCharArray(), salt, rounds));
        } catch (Exception e) { return false; }
    }
    private static byte[] derive(char[] password, byte[] salt, int rounds) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, rounds, KEY_BITS);
        try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(); }
        finally { spec.clearPassword(); }
    }
}
