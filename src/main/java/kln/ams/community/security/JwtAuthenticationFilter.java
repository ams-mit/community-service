package kln.ams.community.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kln.ams.community.dto.ApiErrorResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String PRINCIPAL_ATTRIBUTE = "jwtPrincipal";
    public static final String REQUEST_ID_ATTRIBUTE = "requestId";

    private final JwtService jwtService;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final boolean securityEnabled;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            @Value("${jwt.security.enabled:true}") boolean securityEnabled) {
        this.jwtService = jwtService;
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
        request.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);
        response.setHeader("X-Request-ID", requestId);

        if (isPublicPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!securityEnabled) {
            String headerUserId = request.getHeader("X-User-Id");
            String headerRole = request.getHeader("X-User-Role");
            String headerType = request.getHeader("X-Token-Type");

            String effectiveUser = (headerUserId != null && !headerUserId.isBlank()) ? headerUserId : "dev-user-id";
            String effectiveType = (headerType != null && !headerType.isBlank()) ? headerType : "user";
            List<String> effectiveRoles = new ArrayList<>();
            if (headerRole != null && !headerRole.isBlank()) {
                effectiveRoles.addAll(Arrays.asList(headerRole.split(",")));
            } else {
                effectiveRoles.add(RoleConstants.SYSTEM_ADMINISTRATOR);
                effectiveRoles.add(RoleConstants.APARTMENT_MANAGER);
            }

            request.setAttribute(PRINCIPAL_ATTRIBUTE, new JwtAuthPrincipal(effectiveUser, effectiveType, effectiveRoles));
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            writeUnauthorized(response, requestId, "Missing or malformed Authorization header");
            return;
        }

        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            writeUnauthorized(response, requestId, "Bearer token is required");
            return;
        }

        try {
            JwtAuthPrincipal principal = jwtService.verifyAndExtract(token);
            request.setAttribute(PRINCIPAL_ATTRIBUTE, principal);
        } catch (JwtException | IllegalArgumentException e) {
            writeUnauthorized(response, requestId, "Invalid or expired token");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return path.equals("/")
                || path.equals("/actuator/health")
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

        ApiErrorResponse error = ApiErrorResponse.of(
                message,
                "UNAUTHORIZED",
                null,
                requestId
        );

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader("WWW-Authenticate", "Bearer");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
