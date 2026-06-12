package com.vin.decoder.service.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vin.decoder.model.CarInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class NhtsaAdapter implements VinDecoderAdapter {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String NHTSA_API_URL = "https://vpic.nhtsa.dot.gov/api/vehicles/DecodeVinValues/";

    @Override
    public CarInfo decodeVin(String vin) {
        log.info("Запрос к NHTSA для VIN: {}", vin);

        String url = NHTSA_API_URL + vin + "?format=json";

        try {
            String response = restTemplate.getForObject(url, String.class);
            log.debug("Ответ NHTSA: {}", response);
            return parseResponse(response, vin);
        } catch (Exception e) {
            log.error("Ошибка при вызове NHTSA API: {}", e.getMessage());
            throw new RuntimeException("Ошибка при расшифровке VIN через NHTSA: " + e.getMessage());
        }
    }

    private CarInfo parseResponse(String responseBody, String vin) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode results = root.path("Results");

            if (results.isArray() && results.size() > 0) {
                JsonNode first = results.get(0);

                String make = first.path("Make").asText(null);
                String model = first.path("Model").asText(null);
                String yearStr = first.path("ModelYear").asText(null);
                String errorCode = first.path("ErrorCode").asText(null);
                String errorText = first.path("ErrorText").asText(null);

                if (make == null || make.isEmpty() || "null".equals(make)) {
                    log.warn("VIN {} не найден в базе NHTSA", vin);
                    throw new RuntimeException("VIN не найден в базе данных NHTSA. Проверьте правильность VIN.");
                }

                Integer year = null;
                if (yearStr != null && !yearStr.isEmpty() && !"0".equals(yearStr)) {
                    try {
                        year = Integer.parseInt(yearStr);
                    } catch (NumberFormatException e) {
                        log.warn("Не удалось распарсить год: {}", yearStr);
                    }
                }

                int qc = 0;  // полное распознавание
                if ("1".equals(errorCode)) {
                    qc = 1;  // контрольная сумма не совпадает
                    log.warn("VIN {} имеет ошибку контрольной суммы: {}", vin, errorText);
                } else if ("2".equals(errorCode)) {
                    qc = 2;  // VIN не найден
                    throw new RuntimeException("VIN не найден в базе NHTSA");
                }

                CarInfo carInfo = CarInfo.builder()
                        .brand(make)
                        .model(model != null && !"null".equals(model) ? model : "Не определена")
                        .year(year)
                        .qc(qc)
                        .build();

                log.info("VIN {} расшифрован NHTSA: {} {} ({})", vin, carInfo.getBrand(), carInfo.getModel(), year);
                return carInfo;
            }
            throw new RuntimeException("Неожиданный ответ от NHTSA API");
        } catch (Exception e) {
            log.error("Ошибка парсинга ответа NHTSA: {}", e.getMessage());
            throw new RuntimeException("Ошибка расшифровки VIN: " + e.getMessage());
        }
    }
}