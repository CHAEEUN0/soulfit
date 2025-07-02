package soulfit.soulfit.controller;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import soulfit.soulfit.util.JwtUtil;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
@CrossOrigin(origins = "*", maxAge = 3600)
public class JwtDebugController {

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/token-info")
    public ResponseEntity<Map<String, Object>> getTokenInfo(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        String authHeader = request.getHeader("Authorization");
        response.put("authorizationHeader", authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            response.put("token", token.substring(0, Math.min(20, token.length())) + "...");

            try {
                String username = jwtUtil.extractUsername(token);
                response.put("username", username);
                response.put("isValid", jwtUtil.validateToken(token));
                response.put("expiration", jwtUtil.extractExpiration(token));
            } catch (Exception e) {
                response.put("error", e.getMessage());
                response.put("isValid", false);
            }
        } else {
            response.put("error", "No Bearer token found");
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate-token")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        String token = request.get("token");

        if (token != null) {
            try {
                String username = jwtUtil.extractUsername(token);
                boolean isValid = jwtUtil.validateToken(token);

                response.put("username", username);
                response.put("isValid", isValid);
                response.put("expiration", jwtUtil.extractExpiration(token));
            } catch (Exception e) {
                response.put("error", e.getMessage());
                response.put("isValid", false);
            }
        } else {
            response.put("error", "No token provided");
        }

        return ResponseEntity.ok(response);
    }
}