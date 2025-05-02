package org.lucky0111.pettalk.service.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ResponseService {

    public ResponseEntity<?> createSuccessResponse(Object data, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("message", message);

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> createErrorResponse(String code, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);

        Map<String, String> error = new HashMap<>();
        error.put("code", code);
        error.put("message", message);

        response.put("error", error);
        response.put("timestamp", java.time.Instant.now().toString());

        return ResponseEntity.badRequest().body(response);
    }
}
