package igentuman.nc.hub;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class PowSolver {

    private static final ThreadLocal<MessageDigest> SHA256 = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    });

    private PowSolver() {}

    public static String solve(String challenge, int difficulty) {
        MessageDigest sha256 = SHA256.get();
        for (long nonce = 0; ; nonce++) {
            sha256.reset();
            byte[] hash = sha256.digest((challenge + nonce).getBytes(StandardCharsets.UTF_8));
            if (countLeadingHexZeros(hash) >= difficulty) {
                return Long.toString(nonce);
            }
        }
    }

    private static int countLeadingHexZeros(byte[] hash) {
        int count = 0;
        for (byte b : hash) {
            int hi = (b >> 4) & 0xF;
            int lo = b & 0xF;
            if (hi == 0) {
                count++;
            } else {
                break;
            }
            if (lo == 0) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }
}
