package com.vin.decoder.config;

import com.vin.decoder.service.validation.BasicVinValidation;
import com.vin.decoder.service.validation.VinValidationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ValidationConfig {

    @Bean
    @Primary
    public VinValidationStrategy vinValidationStrategy() {
        return new BasicVinValidation();
    }
}