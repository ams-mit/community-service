package kln.ams.community.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    @Builder.Default
    private boolean success = false;

    private String message;

    private ErrorDetails error;

    @Builder.Default
    private Instant timestamp = Instant.now();

    private String requestId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDetails {
        private String code;
        private Object details;
    }

    public static ApiErrorResponse of(String message, String errorCode, Object details, String requestId) {
        return ApiErrorResponse.builder()
                .success(false)
                .message(message)
                .error(new ErrorDetails(errorCode, details))
                .timestamp(Instant.now())
                .requestId(requestId)
                .build();
    }
}
