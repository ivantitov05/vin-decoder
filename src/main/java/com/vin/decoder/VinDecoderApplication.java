package com.vin.decoder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class VinDecoderApplication {
    public static void main(String[] args){
        SpringApplication.run(VinDecoderApplication.class, args);
    }
}
