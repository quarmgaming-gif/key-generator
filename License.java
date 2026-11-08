package carb;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDate;
import java.util.Base64;

public final class License {
    // Wklej tu klucz publiczny z "java KeyGen.java init"
    private static final String PUBLIC_KEY = "WKLEJ_TUTAJ_PUBLIC_KEY";
    private static final String ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    public enum Status { VALID_MONTH, VALID_LIFE, EXPIRED, INVALID }

    private static Status current = Status.INVALID;

    public static boolean isActive() {
        return current == Status.VALID_MONTH || current == Status.VALID_LIFE;
    }

    public static Status status() {
        return current;
    }

    /** Wywolaj przy starcie moda. Klucz jest w config/carb/license.txt */
    public static void loadFromDisk() {
        try {
            Path p = Path.of("config", "carb", "license.txt");
            current = Files.exists(p) ? check(Files.readString(p).trim()) : Status.INVALID;
        } catch (Exception e) {
            current = Status.INVALID;
        }
    }

    /** Wywolaj z GUI po wpisaniu klucza. */
    public static Status activate(String key) {
        current = check(key);
        if (isActive()) {
            try {
                Path p = Path.of("config", "carb", "license.txt");
                Files.createDirectories(p.getParent());
                Files.writeString(p, key.trim());
            } catch (Exception ignored) {}
        }
        return current;
    }

    public static Status check(String key) {
        try {
            key = key.trim().toUpperCase();
            if (!key.startsWith("CARB-")) return Status.INVALID;
            byte[] all = b32decode(key.substring(5));
            if (all.length < 73) return Status.INVALID;

            byte[] payload = new byte[9];
            byte[] sig = new byte[64];
            System.arraycopy(all, 0, payload, 0, 9);
            System.arraycopy(all, 9, sig, 0, 64);

            PublicKey pub = KeyFactory.getInstance("Ed25519")
                    .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(PUBLIC_KEY)));
            Signature s = Signature.getInstance("Ed25519");
            s.initVerify(pub);
            s.update(payload);
            if (!s.verify(sig)) return Status.INVALID;

            ByteBuffer b = ByteBuffer.wrap(payload);
            byte type = b.get();
            int expiry = b.getInt();
            if (type == 2) return Status.VALID_LIFE;
            if (type == 1) {
                return LocalDate.now().toEpochDay() <= expiry ? Status.VALID_MONTH : Status.EXPIRED;
            }
            return Status.INVALID;
        } catch (Exception e) {
            return Status.INVALID;
        }
    }

    private static byte[] b32decode(String s) {
        byte[] out = new byte[s.length() * 5 / 8];
        int buf = 0, bits = 0, i = 0;
        for (char c : s.toCharArray()) {
            int v = ALPHA.indexOf(c);
            if (v < 0) throw new IllegalArgumentException();
            buf = (buf << 5) | v;
            bits += 5;
            if (bits >= 8) {
                out[i++] = (byte) (buf >> (bits - 8));
                bits -= 8;
                buf &= (1 << bits) - 1;
            }
        }
        return out;
    }
}
