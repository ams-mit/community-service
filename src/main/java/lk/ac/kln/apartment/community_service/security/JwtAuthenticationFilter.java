package lk.ac.kln.apartment.community_service.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String PRINCIPAL_ATTRIBUTE = "jwtPrincipal";

    private final JwtService jwtService;
    private final JsonMapper jsonMapper;
    private final boolean securityEnabled;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            JsonMapper jsonMapper,
            @Value("${jwt.security.enabled:true}") boolean securityEnabled) {

        this.jwtService = jwtService;
        this.jsonMapper = jsonMapper;
        this.securityEnabled = securityEnabled;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String requestId = request.getHeader("X-Request-ID");

        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        request.setAttribute("requestId", requestId);
        response.setHeader("X-Request-ID", requestId);

        if (!securityEnabled || isPublicPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null
                || authHeader.length() < 7
                || !authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {

            writeUnauthorized(
                    response,
                    requestId,
                    "Missing or malformed Authorization header");
            return;
        }

        String token = authHeader.substring(7).trim();

        if (token.isEmpty()) {
            writeUnauthorized(response, requestId, "Bearer token is required");
            return;
        }

        JwtAuthPrincipal principal;

        try {
            principal = jwtService.verifyAndExtract(token);
        } catch (JwtException | IllegalArgumentException e) {
            writeUnauthorized(response, requestId, "Invalid or expired token");
            return;
        }

        request.setAttribute(PRINCIPAL_ATTRIBUTE, principal);

        // Keep controller execution outside the JWT exception handler.
        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(HttpServletRequest request) {
        String path = request.getRequestURI()
                .substring(request.getContextPath().length());

        return path.equals("/actuator/health")
                || path.equals("/actuator/info")
                || path.equals("/swagger-ui.html")
                || path.equals("/swagger-ui")
                || path.startsWith("/swagger-ui/")
                || path.equals("/v3/api-docs")
                || path.startsWith("/v3/api-docs/");
    }

    private void writeUnauthorized(
            HttpServletResponse response,
            String requestId,
            String message) throws IOException {

        Map<String, Object> error = new LinkedHashMap<>();
        error.put("code", "AUTHENTICATION_FAILED");
        error.put("details", null);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("message", message);
        body.put("error", error);
        body.put("timestamp", Instant.now().toString());
        body.put("requestId", requestId);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader("WWW-Authenticate", "Bearer");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(jsonMapper.writeValueAsString(body));
    }
}