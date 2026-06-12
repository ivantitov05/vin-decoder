package com.vin.decoder.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vin.decoder.dto.AuthRequest;
import com.vin.decoder.model.User;
import com.vin.decoder.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Интеграционные тесты контроллера аутентификации")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;  // Эмуляция HTTP запросов

    @Autowired
    private ObjectMapper objectMapper;  // Для конвертации в JSON

    @Autowired
    private UserRepository userRepository;  // Для очистки тестовых данных

    @BeforeEach
    void cleanUp() {
        // Очищаем ВСЕХ тестовых пользователей перед каждым тестом
        userRepository.findByUsername("testuser").ifPresent(user -> userRepository.delete(user));
        userRepository.findByUsername("existinguser").ifPresent(user -> userRepository.delete(user));
        userRepository.findByUsername("loginuser").ifPresent(user -> userRepository.delete(user));
        userRepository.findByUsername("testuser2").ifPresent(user -> userRepository.delete(user));
        userRepository.findByUsername("nonexistent").ifPresent(user -> userRepository.delete(user));
        userRepository.findByUsername("").ifPresent(user -> userRepository.delete(user));
    }

    // ========================================
    // ТЕСТ №1: Успешная регистрация
    // ========================================

    @Test
    @DisplayName("POST /api/auth/register - успешная регистрация нового пользователя")
    void registerNewUserShouldReturnOk() throws Exception {
        // GIVEN
        AuthRequest request = new AuthRequest("testuser", "password123");

        // WHEN & THEN
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully"));
    }

    // ========================================
    // ТЕСТ №2: Регистрация существующего пользователя
    // ========================================

    @Test
    @DisplayName("POST /api/auth/register - регистрация существующего пользователя возвращает ошибку")
    void registerExistingUserShouldReturnBadRequest() throws Exception {
        // GIVEN - сначала создаём пользователя
        User existingUser = User.builder()
                .username("existinguser")
                .password("hashedPassword")
                .role("USER")
                .build();
        userRepository.save(existingUser);

        AuthRequest request = new AuthRequest("existinguser", "anypassword");

        // WHEN & THEN
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username already exists"));
    }

    // ========================================
    // ТЕСТ №3: Успешный логин
    // ========================================

    @Test
    @DisplayName("POST /api/auth/login - успешный логин возвращает JWT токен")
    void loginWithValidCredentialsShouldReturnToken() throws Exception {
        // GIVEN - сначала регистрируем пользователя
        AuthRequest registerRequest = new AuthRequest("loginuser", "mypassword");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // WHEN - пытаемся войти
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                // THEN
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.username").value("loginuser"));
    }

    // ========================================
    // ТЕСТ №4: Логин с неправильным паролем
    // ========================================

    @Test
    @DisplayName("POST /api/auth/login - неправильный пароль возвращает 401")
    void loginWithInvalidPasswordShouldReturnUnauthorized() throws Exception {
        // GIVEN - создаём пользователя
        AuthRequest registerRequest = new AuthRequest("testuser2", "correctpass");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // WHEN - пытаемся войти с неправильным паролем
        AuthRequest loginRequest = new AuthRequest("testuser2", "wrongpass");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                // THEN
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    // ========================================
    // ТЕСТ №5: Логин с несуществующим пользователем
    // ========================================

    @Test
    @DisplayName("POST /api/auth/login - несуществующий пользователь возвращает 401")
    void loginWithNonExistentUserShouldReturnUnauthorized() throws Exception {
        // GIVEN - пользователь не создавался
        AuthRequest request = new AuthRequest("nonexistent", "password");

        // WHEN & THEN
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    // ========================================
    // ТЕСТ №6: Пустые поля при регистрации
    // ========================================

    @Test
    @DisplayName("POST /api/auth/register - пустые поля возвращают ошибку")
    void registerWithEmptyFieldsShouldReturnError() throws Exception {
        // GIVEN
        AuthRequest request = new AuthRequest("", "");

        // WHEN & THEN
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}