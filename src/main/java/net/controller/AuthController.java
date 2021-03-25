package net.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpMethod.POST;

@RestController
public class AuthController {

    public static final ResponseEntity<String> INVALID_CREDENTIALS_RESPONSE = ResponseEntity.status(401).body("Invalid credentials");

    private final RestTemplate restTemplate;

    @Value("https://${okta.domain}/oauth2/default/v1/token")
    private String oktaTokenUrl;

    @Value("${okta.oauth2.client-id}")
    private String clientId;

    @Value("${okta.oauth2.client-secret}")
    private String clientSecret;

    public AuthController() {
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
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientId, clientSecret);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("scope", "openid");
        formData.add("username", username);
        formData.add("password", password);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(oktaTokenUrl, entity, Map.class);
        return ResponseEntity.ok(response.getBody());
    }
}
