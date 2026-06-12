package com.vin.decoder.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vin.decoder.dto.AuthRequest;
import com.vin.decoder.model.User;
import com.vin.decoder.repository.UserRepository;
import com.vin.decoder.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Интеграционные тесты VIN контроллера")
class VinControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private String validToken;
    private String invalidToken;
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "password123";

    @BeforeEach
    void setUp() {
        // Очищаем тестового пользователя
        userRepository.findByUsername(TEST_USERNAME).ifPresent(user -> userRepository.delete(user));

        // СОЗДАЁМ ТЕСТОВОГО ПОЛЬЗОВАТЕЛЯ В БД
        User testUser = User.builder()
                .username(TEST_USERNAME)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .role("USER")
                .build();
        userRepository.save(testUser);

        // Генерируем валидный токен для тестового пользователя
        validToken = jwtUtil.generateToken(TEST_USERNAME);
        // Невалидный токен
        invalidToken = "invalid.token.here";
    }

    // ========================================
    // ТЕСТ №1: Проверка VIN без токена
    // ========================================

    @Test
    @DisplayName("POST /api/check - без токена возвращает 401")
    void checkVinWithoutTokenShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/check?vin=WBA3B5G59ENF12345"))
                .andExpect(status().isUnauthorized());
    }

    // ========================================
    // ТЕСТ №2: Проверка VIN с невалидным токеном
    // ========================================

    @Test
    @DisplayName("POST /api/check - с невалидным токеном возвращает 401")
    void checkVinWithInvalidTokenShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/check?vin=WBA3B5G59ENF12345")
                        .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized());
    }

    // ========================================
    // ТЕСТ №3: Проверка VIN с валидным токеном
    // ========================================

    @Test
    @DisplayName("POST /api/check - с валидным токеном возвращает 200")
    void checkVinWithValidTokenShouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/check?vin=WBA3B5G59ENF12345")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.brand").exists())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    // ========================================
    // ТЕСТ №4: Проверка невалидного VIN
    // ========================================

    @Test
    @DisplayName("POST /api/check - невалидный VIN возвращает ошибку")
    void checkInvalidVinShouldReturnError() throws Exception {
        mockMvc.perform(post("/api/check?vin=INVALIDVIN")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    // ========================================
    // ТЕСТ №5: Проверка VIN со слишком коротким значением
    // ========================================

    @Test
    @DisplayName("POST /api/check - слишком короткий VIN возвращает ошибку")
    void checkTooShortVinShouldReturnError() throws Exception {
        mockMvc.perform(post("/api/check?vin=ABC123")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isBadRequest());
    }

    // ========================================
    // ТЕСТ №6: Проверка VIN с запрещёнными символами
    // ========================================

    @Test
    @DisplayName("POST /api/check - VIN с буквой O возвращает ошибку")
    void checkVinWithLetterOShouldReturnError() throws Exception {
        mockMvc.perform(post("/api/check?vin=O1234567890123456")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isBadRequest());
    }

    // ========================================
    // ТЕСТ №7: История без токена
    // ========================================

    @Test
    @DisplayName("GET /api/history - без токена возвращает 401")
    void getHistoryWithoutTokenShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/history"))
                .andExpect(status().isUnauthorized());
    }

    // ========================================
    // ТЕСТ №8: История с невалидным токеном
    // ========================================

    @Test
    @DisplayName("GET /api/history - с невалидным токеном возвращает 401")
    void getHistoryWithInvalidTokenShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/history")
                        .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized());
    }

    // ========================================
    // ТЕСТ №9: История с валидным токеном
    // ========================================

    @Test
    @DisplayName("GET /api/history - с валидным токеном возвращает 200")
    void getHistoryWithValidTokenShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/history")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());
    }
}