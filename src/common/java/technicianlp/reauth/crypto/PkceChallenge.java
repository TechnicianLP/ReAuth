package technicianlp.reauth.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility for generating a PKCE Verifier and Challenge as specified by RFC7636
 */
public final class PkceChallenge {

    final String challenge;
    final String verifier;

    PkceChallenge() {
        Base64.Encoder base64url = Base64.getUrlEncoder().withoutPadding();

        byte[] verifierCode = new byte[32];
        new SecureRandom().nextBytes(verifierCode);
        this.verifier = base64url.encodeToString(verifierCode);

        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] challengeCode = sha256.digest(this.verifier.getBytes(StandardCharsets.US_ASCII));
            this.challenge = base64url.encodeToString(challengeCode);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("SHA-256 unavailable", e);
        }
    }

    public final String getChallenge() {
        return this.challenge;
    }

    public final String getVerifier() {
        return this.verifier;
    }
}
