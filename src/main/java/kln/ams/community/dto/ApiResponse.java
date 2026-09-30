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
public class ApiResponse<T> {

    @Builder.Default
    private boolean success = true;

    private String message;

    private T data;

    private PaginationDto pagination;

    @Builder.Default
    private Instant timestamp = Instant.now();

    private String requestId;

    public static <T> ApiResponse<T> ok(String message, T data, String requestId) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .requestId(requestId)
                .build();
    }

    public static <T> ApiResponse<T> ok(String message, T data, PaginationDto pagination, String requestId) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .pagination(pagination)
                .timestamp(Instant.now())
                .requestId(requestId)
                .build();
    }

    public static <T> ApiResponse<T> created(String message, T data, String requestId) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .requestId(requestId)
                .build();
    }
}
