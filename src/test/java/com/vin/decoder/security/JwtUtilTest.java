package com.vin.decoder.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тестирование JWT утилиты - генерация и валидация токенов")
class JwtUtilTest {

    // Объект, который тестируем
    private JwtUtil jwtUtil;

    // Тестовые данные
    private static final String TEST_SECRET = "testSecretKeyForJWTTesting2026ThatIs32CharsLong!";
    private static final Long TEST_EXPIRATION = 3600000L; // 1 час в миллисекундах

    @BeforeEach
    void setUp() {
        // Создаём экземпляр JwtUtil
        jwtUtil = new JwtUtil();

        // Устанавливаем тестовые значения (так как @Value из properties не работают в тестах)
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", TEST_EXPIRATION);
    }

    // ========================================
    // ТЕСТ №1: Генерация токена
    // ========================================

    @Test
    @DisplayName("Генерация токена должна создать непустую строку")
    void generateTokenShouldReturnNonEmptyString() {
        // GIVEN - подготовка
        String username = "testuser";

        // WHEN - выполняем действие
        String token = jwtUtil.generateToken(username);

        // THEN - проверяем результат
        assertNotNull(token, "Токен не должен быть null");
        assertFalse(token.isEmpty(), "Токен не должен быть пустым");
        assertTrue(token.split("\\.").length == 3, "Токен должен состоять из 3 частей (header.payload.signature)");
    }

    // ========================================
    // ТЕСТ №2: Извлечение username из токена
    // ========================================

    @Test
    @DisplayName("Из токена должен корректно извлекаться username")
    void extractUsernameShouldReturnCorrectUsername() {
        // GIVEN
        String expectedUsername = "PolPot";
        String token = jwtUtil.generateToken(expectedUsername);

        // WHEN
        String extractedUsername = jwtUtil.extractUsername(token);

        // THEN
        assertEquals(expectedUsername, extractedUsername, "Извлечённый username должен совпадать с тем, который положили");
    }

    // ========================================
    // ТЕСТ №3: Извлечение username из разных токенов
    // ========================================

    @ParameterizedTest
    @ValueSource(strings = {"user1", "admin", "test@mail.com", "veryLongUsername123"})
    @DisplayName("Из токена извлекается правильный username для разных значений")
    void extractUsernameShouldWorkForDifferentUsernames(String username) {
        // GIVEN
        String token = jwtUtil.generateToken(username);

        // WHEN
        String extracted = jwtUtil.extractUsername(token);

        // THEN
        assertEquals(username, extracted);
    }

    // ========================================
    // ТЕСТ №4: Валидный токен проходит проверку
    // ========================================

    @Test
    @DisplayName("Валидный токен должен проходить проверку")
    void validTokenShouldPassValidation() {
        // GIVEN
        String token = jwtUtil.generateToken("user");

        // WHEN
        boolean isValid = jwtUtil.validateToken(token);

        // THEN
        assertTrue(isValid, "Правильно сгенерированный токен должен быть валидным");
    }

    // ========================================
    // ТЕСТ №5: Разные валидные токены проходят проверку
    // ========================================

    @ParameterizedTest
    @ValueSource(strings = {"alice", "bob", "charlie"})
    @DisplayName("Токены для разных пользователей должны быть валидны")
    void tokensForDifferentUsersShouldBeValid(String username) {
        // GIVEN
        String token = jwtUtil.generateToken(username);

        // WHEN & THEN
        assertTrue(jwtUtil.validateToken(token));
        assertEquals(username, jwtUtil.extractUsername(token));
    }

    // ========================================
    // ТЕСТ №6: Невалидный токен не проходит проверку
    // ========================================

    @Test
    @DisplayName("Невалидный токен не должен проходить проверку")
    void invalidTokenShouldFailValidation() {
        // GIVEN - неправильный токен
        String invalidToken = "this.is.not.a.valid.token";

        // WHEN
        boolean isValid = jwtUtil.validateToken(invalidToken);

        // THEN
        assertFalse(isValid, "Случайная строка не должна быть валидным токеном");
    }

    // ========================================
    // ТЕСТ №7: Испорченный токен не проходит проверку
    // ========================================

    @Test
    @DisplayName("Испорченный (изменённый) токен не должен проходить проверку")
    void tamperedTokenShouldFailValidation() {
        // GIVEN
        String originalToken = jwtUtil.generateToken("user");
        // Портим токен - меняем последний символ
        String tamperedToken = originalToken.substring(0, originalToken.length() - 1) + "X";

        // WHEN
        boolean isValid = jwtUtil.validateToken(tamperedToken);

        // THEN
        assertFalse(isValid, "Изменённый токен должен быть невалидным");
    }

    // ========================================
    // ТЕСТ №8: Пустой и null токен
    // ========================================

    @Test
    @DisplayName("Пустой токен не должен проходить проверку")
    void emptyTokenShouldFailValidation() {
        assertFalse(jwtUtil.validateToken(""), "Пустая строка не валидный токен");
        assertFalse(jwtUtil.validateToken(null), "null не валидный токен");
    }

    // ========================================
    // ТЕСТ №9: Токен с неправильным форматом
    // ========================================

    @ParameterizedTest
    @ValueSource(strings = {
            "no.dots",                           // нет точек
            "only.two.parts",                    // 2 части (должно быть 3)
            "header.payload.signature.extra",    // 4 части (много)
            "",                                  // пустой
            "   "                                // пробелы
    })
    @DisplayName("Токены с неправильным форматом должны быть отклонены")
    void malformedTokensShouldFailValidation(String malformedToken) {
        assertFalse(jwtUtil.validateToken(malformedToken),
                "Токен с неправильным форматом не должен быть валидным: " + malformedToken);
    }
}