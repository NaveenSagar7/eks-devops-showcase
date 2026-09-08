package com.myapp.dailyjournal.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.regex.Pattern;

/**
 * PAN (Indian Permanent Account Number) is sensitive PII, so the plaintext value is never
 * persisted: a keyed hash (HMAC-SHA256, keyed with app.security.pan-secret) is stored for
 * returning-user lookup, and only a masked form is kept for display.
 */
public final class PanUtil {

    private static final Pattern PAN_PATTERN = Pattern.compile("^[A-Z]{5}[0-9]{4}[A-Z]$");
    private static final String HMAC_ALGO = "HmacSHA256";

    private PanUtil() {
    }

    public static boolean isValid(String pan) {
        return pan != null && PAN_PATTERN.matcher(pan.trim().toUpperCase()).matches();
    }

    public static String normalize(String pan) {
        return pan.trim().toUpperCase();
    }

    public static String hash(String pan, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGO));
            byte[] digest = mac.doFinal(normalize(pan).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Unable to hash PAN", e);
        }
    }

    public static String mask(String pan) {
        String normalized = normalize(pan);
        return "XXXXX" + normalized.substring(5);
    }
}
