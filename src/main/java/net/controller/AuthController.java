package net.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.asm.TypeReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpMethod.POST;

@RestController
public class AuthController {

    public static final ResponseEntity<String> INVALID_CREDENTIALS_RESPONSE = ResponseEntity.status(401).body("Invalid credentials");

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("https://${okta.domain}/api/v1/authn")
    private String oktaAuthenticationUrl;

    public AuthController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    @PostMapping("/auth")
    public ResponseEntity<?> authorize(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("authorization");
        if (authorizationHeader == null || authorizationHeader.isEmpty() || !authorizationHeader.startsWith("Basic")) {
            return INVALID_CREDENTIALS_RESPONSE;
        }

        String[] userPassArray;
        try {
            userPassArray = new String(Base64.decodeBase64(authorizationHeader.substring(6))).split(":");
        } catch (Exception e) {
            return INVALID_CREDENTIALS_RESPONSE;
        }

        String username = userPassArray[0], password = userPassArray[1];
        Map<String, Object> authRequestBody = new HashMap<>(3); // could replace with a dedicated POJO
        authRequestBody.put("username", username);
        authRequestBody.put("password", password);
        authRequestBody.put("options", Map.of("multiOptionalFactorEnroll", false, "warnBeforePasswordExpired", false));

        ResponseEntity<Map<String, String>> authenticateResponse = restTemplate.exchange(oktaAuthenticationUrl, POST,
                new HttpEntity<>(authRequestBody), new ParameterizedTypeReference<Map<String, String>>() {});

        if (authenticateResponse.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            return INVALID_CREDENTIALS_RESPONSE;
        }

        String sessionToken = authenticateResponse.getBody().get("sessionToken");


        System.out.println("Received authorize request");
        return ResponseEntity.ok().build();
    }
}
