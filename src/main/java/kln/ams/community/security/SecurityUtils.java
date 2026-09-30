package kln.ams.community.security;

import jakarta.servlet.http.HttpServletRequest;
import kln.ams.community.exception.ForbiddenException;
import kln.ams.community.exception.UnauthorizedException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static JwtAuthPrincipal getCurrentPrincipal() {
        RequestAttributes attribs = RequestContextHolder.getRequestAttributes();
        if (attribs instanceof ServletRequestAttributes servletAttribs) {
            HttpServletRequest request = servletAttribs.getRequest();
            Object principal = request.getAttribute(JwtAuthenticationFilter.PRINCIPAL_ATTRIBUTE);
            if (principal instanceof JwtAuthPrincipal p) {
                return p;
            }
        }
        return null;
    }

    public static JwtAuthPrincipal requirePrincipal() {
        JwtAuthPrincipal principal = getCurrentPrincipal();
        if (principal == null) {
            throw new UnauthorizedException("Authentication required");
        }
        return principal;
    }

    public static String getCurrentRequestId() {
        RequestAttributes attribs = RequestContextHolder.getRequestAttributes();
        if (attribs instanceof ServletRequestAttributes servletAttribs) {
            HttpServletRequest request = servletAttribs.getRequest();
            Object reqId = request.getAttribute(JwtAuthenticationFilter.REQUEST_ID_ATTRIBUTE);
            if (reqId instanceof String s && !s.isBlank()) {
                return s;
            }
            String headerId = request.getHeader("X-Request-ID");
            if (headerId != null && !headerId.isBlank()) {
                return headerId;
            }
        }
        return UUID.randomUUID().toString();
    }

    public static void requireRole(String... roles) {
        JwtAuthPrincipal principal = requirePrincipal();
        if (!principal.hasAnyRole(roles)) {
            throw new ForbiddenException("Access denied: insufficient role privileges");
        }
    }

    public static void requireService(String... allowedServices) {
        JwtAuthPrincipal principal = requirePrincipal();
        if (!principal.isService()) {
            throw new ForbiddenException("Access denied: service token required");
        }
        if (allowedServices.length > 0) {
            boolean matched = false;
            for (String allowed : allowedServices) {
                if (allowed.equalsIgnoreCase(principal.getSubject())) {
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                throw new ForbiddenException("Access denied: service " + principal.getSubject() + " is not authorized for this endpoint");
            }
        }
    }
}
