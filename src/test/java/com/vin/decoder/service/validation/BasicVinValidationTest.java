// Пакет должен совпадать с тестируемым классом
package com.vin.decoder.service.validation;

// Импортируем аннотации для тестов
import org.junit.jupiter.api.BeforeEach;      // Выполняется перед каждым тестом
import org.junit.jupiter.api.DisplayName;     // Даёт понятное имя тесту
import org.junit.jupiter.api.Test;            // Обозначает метод как тест
import org.junit.jupiter.params.ParameterizedTest;  // Для теста с разными параметрами
import org.junit.jupiter.params.provider.ValueSource; // Источник параметров

// Статические импорты для удобства (не нужно писать Assertions. каждый раз)
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тестирование валидации VIN")  // Это имя будет видно в отчёте
class BasicVinValidationTest {

    // Объявляем переменную, которую будем тестировать
    private BasicVinValidation validator;

    // @BeforeEach - этот метод выполняется ПЕРЕД КАЖДЫМ тестом
    // Он создаёт "свежий" объект для каждого теста
    @BeforeEach
    void setUp() {
        // Создаём экземпляр класса, который будем тестировать
        validator = new BasicVinValidation();
    }

    // ========================================
    // ТЕСТ №1: Проверка валидного VIN
    // ========================================

    @Test  // Аннотация означает "это тест"
    @DisplayName("Валидный VIN должен пройти проверку")  // Что должно произойти
    void validVinShouldReturnTrue() {
        // ----- РАЗДЕЛ "GIVEN" (Дано) -----
        // Подготавливаем данные для теста
        String validVin = "WDD2120011A123456";  // Это правильный VIN

        // ----- РАЗДЕЛ "WHEN" (Когда) -----
        // Выполняем действие, которое тестируем
        boolean result = validator.isValid(validVin);

        // ----- РАЗДЕЛ "THEN" (Тогда) -----
        // Проверяем, что результат правильный
        // assertTrue() проверяет, что result = true
        assertTrue(result);
    }

    // ========================================
    // ТЕСТ №2: Проверка нескольких валидных VIN (ParameterizedTest)
    // ========================================

    // @ParameterizedTest - тест запускается несколько раз с разными параметрами
    // @ValueSource - откуда брать параметры
    @ParameterizedTest
    @ValueSource(strings = {
            "WBA3B5G59ENF12345",  // BMW
            "1FMCU9GX6HUA12345",  // Ford
            "JTDBE32K900123456",  // Toyota
            "X7L12345678901234"   // LADA
    })
    @DisplayName("Различные валидные VIN должны проходить проверку")
    void multipleValidVinsShouldReturnTrue(String vin) {
        // WHEN & THEN в одной строке
        assertTrue(validator.isValid(vin));
    }

    // ========================================
    // ТЕСТ №3: Проверка VIN неправильной длины
    // ========================================

    @Test
    @DisplayName("VIN неправильной длины должен быть отклонён")
    void invalidLengthVinShouldReturnFalse() {
        // GIVEN
        String shortVin = "ABC123";           // Слишком короткий (6 символов)
        String longVin = "WDD2120011A123456789"; // Слишком длинный (18+ символов)

        // WHEN & THEN
        // assertFalse() проверяет, что результат = false
        assertFalse(validator.isValid(shortVin));
        assertFalse(validator.isValid(longVin));
    }

    // ========================================
    // ТЕСТ №4: Проверка VIN с недопустимыми символами
    // ========================================

    @Test
    @DisplayName("VIN с недопустимыми символами должен быть отклонён")
    void vinWithInvalidCharsShouldReturnFalse() {
        // GIVEN
        // В VIN нельзя использовать буквы I, O, Q (их легко спутать с цифрами)
        String vinWithO = "O1234567890123456";  // Содержит O (нельзя)
        String vinWithI = "I1234567890123456";  // Содержит I (нельзя)
        String vinWithQ = "Q1234567890123456";  // Содержит Q (нельзя)

        // Строчные буквы тоже нельзя (только заглавные)
        String vinWithLowercase = "wdd2120011a123456";

        // WHEN & THEN
        assertFalse(validator.isValid(vinWithO));
        assertFalse(validator.isValid(vinWithI));
        assertFalse(validator.isValid(vinWithQ));
        assertFalse(validator.isValid(vinWithLowercase));
    }

    // ========================================
    // ТЕСТ №5: Проверка null и пустой строки
    // ========================================

    @Test
    @DisplayName("Null VIN должен быть отклонён")
    void nullVinShouldReturnFalse() {
        // Проверяем, что при null возвращается false
        assertFalse(validator.isValid(null));
    }

    @Test
    @DisplayName("Пустой VIN должен быть отклонён")
    void emptyVinShouldReturnFalse() {
        // Проверяем, что при пустой строке возвращается false
        assertFalse(validator.isValid(""));
    }
}