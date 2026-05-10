package rw.gov.bnr.bnrlicensingportal.api.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int status,
        String error,
        String message,
        String path,
        OffsetDateTime timestamp,
        Map<String, String> fieldErrors
) {
    public ErrorResponse(int status, String error, String message, String path) {
        this(status, error, message, path, OffsetDateTime.now(), null);
    }
}
