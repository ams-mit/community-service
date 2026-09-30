package kln.ams.community.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthPrincipal {

    private String subject;
    private String type;
    private List<String> roles;

    public List<String> getRoles() {
        return roles != null ? roles : Collections.emptyList();
    }

    public boolean isService() {
        return "service".equalsIgnoreCase(type);
    }

    public boolean hasRole(String role) {
        if (roles == null) return false;
        return roles.stream().anyMatch(r -> r.equalsIgnoreCase(role));
    }

    public boolean hasAnyRole(String... expectedRoles) {
        if (roles == null) return false;
        for (String expected : expectedRoles) {
            if (hasRole(expected)) return true;
        }
        return false;
    }

    public String getUserId() {
        return subject;
    }
}
