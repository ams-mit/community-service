package lk.ac.kln.apartment.community_service.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class JwtAuthPrincipal {

    private final String subject;
    private final String type;
    private final List<String> roles;

    public boolean isService() {
        return "service".equals(type);
    }

    public boolean isUser() {
        return "user".equals(type);
    }

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
}