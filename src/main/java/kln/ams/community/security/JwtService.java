package kln.ams.community.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

@Component
public class JwtService {

    private final PublicKey gatewayPublicKey;

    public JwtService(@Value("${jwt.gateway.public-key:}") String publicKeyBase64) {
        this.gatewayPublicKey = parsePublicKey(publicKeyBase64);
    }

    private PublicKey parsePublicKey(String base64Key) {
        if (base64Key == null || base64Key.isBlank()) {
            return null;
        }
        try {
            String cleanKey = base64Key.replaceAll("-----(BEGIN|END) PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] decoded = Base64.getDecoder().decode(cleanKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid Gateway JWT public key configuration", e);
        }
    }

    public JwtAuthPrincipal verifyAndExtract(String token) {
        if (gatewayPublicKey == null) {
            throw new JwtException("Gateway public key is not configured");
        }

        Claims claims = Jwts.parser()
                .verifyWith(gatewayPublicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String subject = claims.getSubject();
        String type = claims.get("type", String.class);

        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);

        return new JwtAuthPrincipal(subject, type, roles);
    }
}
