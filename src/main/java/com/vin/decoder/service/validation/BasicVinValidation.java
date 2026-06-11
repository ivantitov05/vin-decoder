package com.vin.decoder.service.validation;

import com.vin.decoder.service.validation.VinValidationStrategy;
import org.springframework.stereotype.Component;

@Component
public class BasicVinValidation implements VinValidationStrategy {
    @Override
    public boolean isValid(String vin) {
        if (vin == null) return false;
        return vin.matches("^[A-HJ-NPR-Z0-9]{17}$");
    }
}