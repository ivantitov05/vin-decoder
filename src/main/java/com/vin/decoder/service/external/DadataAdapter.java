package com.vin.decoder.service.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vin.decoder.model.CarInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DadataAdapter implements VinDecoderAdapter{

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${dadata.api.url}")
    private String apiUrl;

    @Value("${dadata.api.key}")
    private String apiKey;

    @Value("${dadata.secret.key}")
    private String secretKey;

    public CarInfo decodeVin(String vin) {
        log.info("Запрос к DaData для VIN: {}", vin);

        //telo zaprosa
        String[] requestBody = {vin};

        //заголовки
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/json");
        headers.set("Authorization", "Token " + apiKey);
        headers.set("X-Secret", secretKey);

        HttpEntity<String[]> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return parseResponse(response.getBody());
            } else if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                log.error("Ошибка авторизации DaData: неверный ключ");
                throw new RuntimeException("Ошибка авторизации DaData");
            } else if (response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                log.error("Превышен лимит запросов к DaData");
                throw new RuntimeException("Превышен лимит запросов");
            } else {
                log.error("Ошибка DaData: HTTP {}", response.getStatusCode());
                throw new RuntimeException("Ошибка вызова DaData");
            }
        } catch (Exception e) {
            log.error("Сетевая ошибка при вызове DaData: {}", e.getMessage());
            throw new RuntimeException("Сетевая ошибка при вызове DaData", e);
        }
    }

    private CarInfo parseResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            if (root.isArray() && root.size() > 0) {
                JsonNode first = root.get(0);
                int qc = first.path("qc").asInt();

                // Код качества qc:
                // 0 — уверенно распознаны марка и модель
                // 3 — уверенно распознана марка, модели нет
                // 1 — распознано с допущениями или не распознано
                // 2 — пустое или мусорное значение

                if (qc == 0 || qc == 3) {
                    String brand = first.path("brand").asText();
                    String model = first.path("model").asText();
                    CarInfo carInfo = CarInfo.builder()
                            .brand(brand)
                            .model(model)
                            .qc(qc)
                            .build();
                    return carInfo;
                } else {
                    log.warn("VIN не распознан DaData, qc={}", qc);
                    throw new RuntimeException("VIN не распознан DaData");
                }
            }
            throw new RuntimeException("Неожиданный ответ от DaData");
        } catch (Exception e) {
            log.error("Ошибка парсинга ответа DaData: {}", e.getMessage());
            throw new RuntimeException("Ошибка парсинга ответа DaData", e);
        }
    }
}