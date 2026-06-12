package com.vin.decoder.service.validation;

import org.springframework.stereotype.Component;

@Component
public class ChecksumVinValidation implements VinValidationStrategy {

    @Override
    public boolean isValid(String vin) {
        if (vin == null || !vin.matches("^[A-HJ-NPR-Z0-9]{17}$")) {
            return false;
        }

        return validateChecksum(vin);
    }

    private boolean validateChecksum(String vin) {
        String transliterated = transliterate(vin);

        int[] weights = {8, 7, 6, 5, 4, 3, 2, 10, 0, 9, 8, 7, 6, 5, 4, 3, 2};

        int sum = 0;
        for (int i = 0; i < 17; i++) {
            char c = transliterated.charAt(i);
            int value = Character.getNumericValue(c);
            sum += value * weights[i];
        }

        int remainder = sum % 11;
        char expectedCheckChar = (remainder == 10) ? 'X' : Character.forDigit(remainder, 10);

        char actualCheckChar = vin.charAt(8);

        return actualCheckChar == expectedCheckChar;
    }

    private String transliterate(String vin) {
        StringBuilder result = new StringBuilder();
        for (char c : vin.toCharArray()) {
            result.append(getNumericValue(c));
        }
        return result.toString();
    }

    private char getNumericValue(char c) {
        return switch (c) {
            case 'A', 'J' -> '1';
            case 'B', 'K', 'S' -> '2';
            case 'C', 'L', 'T' -> '3';
            case 'D', 'M', 'U' -> '4';
            case 'E', 'N', 'V' -> '5';
            case 'F', 'W' -> '6';
            case 'G', 'P', 'X' -> '7';
            case 'H', 'Y' -> '8';
            case 'Z' -> '9';
            default -> c;
        };
    }
}