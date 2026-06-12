package com.vin.decoder.config;

import com.vin.decoder.service.external.NhtsaAdapter;
import com.vin.decoder.service.external.VinDecoderAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class VinDecoderConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public VinDecoderAdapter vinDecoderAdapter(NhtsaAdapter nhtsaAdapter) {
        return nhtsaAdapter;
    }
}