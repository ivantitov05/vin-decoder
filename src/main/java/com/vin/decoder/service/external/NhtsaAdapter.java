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

                // Проверяем ошибку
                String errorCode = first.path("ErrorCode").asText(null);
                if ("1".equals(errorCode) || "2".equals(errorCode)) {
                    log.warn("VIN {} не найден в базе NHTSA", vin);
                    return extractBasicInfoFromVin(vin);
                }

                String make = first.path("Make").asText(null);
                String model = first.path("Model").asText(null);
                String yearStr = first.path("ModelYear").asText(null);

                // Если не распознан (российский VIN)
                if ((make == null || make.isEmpty() || "null".equals(make)) &&
                        (model == null || model.isEmpty() || "null".equals(model))) {
                    log.info("VIN {} не распознан NHTSA, извлекаем базовую информацию", vin);
                    return extractBasicInfoFromVin(vin);
                }

                Integer year = null;
                if (yearStr != null && !yearStr.isEmpty() && !"0".equals(yearStr)) {
                    try {
                        year = Integer.parseInt(yearStr);
                    } catch (NumberFormatException e) {
                        log.warn("Не удалось распарсить год: {}", yearStr);
                    }
                }

                CarInfo carInfo = CarInfo.builder()
                        .brand(make != null && !"null".equals(make) ? make : "Не определена")
                        .model(model != null && !"null".equals(model) ? model : "Не определена")
                        .year(year)
                        .qc(0)
                        .build();

                log.info("VIN {} успешно расшифрован: {} {} ({})", vin, carInfo.getBrand(), carInfo.getModel(), year);
                return carInfo;
            }
            return extractBasicInfoFromVin(vin);
        } catch (Exception e) {
            log.error("Ошибка парсинга ответа NHTSA: {}", e.getMessage());
            throw new RuntimeException("Ошибка парсинга ответа от NHTSA", e);
        }
    }

    /**
     * Базовая расшифровка для российских или нераспознанных VIN
     */
    private CarInfo extractBasicInfoFromVin(String vin) {
        if (vin == null || vin.length() < 17) {
            return CarInfo.builder()
                    .brand("Не определен")
                    .model("Не определен")
                    .qc(1)
                    .build();
        }

        String wmi = vin.substring(0, 3);
        String brand = getBrandByWmi(wmi);
        Integer year = extractYearFromVin(vin);

        return CarInfo.builder()
                .brand(brand)
                .model("Информация отсутствует")
                .year(year)
                .qc(brand.equals("Не определен") ? 1 : 3)
                .build();
    }

    private String getBrandByWmi(String wmi) {
        return switch (wmi) {
            case "X7L", "X7M", "X7N", "X7P", "X7R" -> "LADA (ВАЗ)";
            case "X96", "X98" -> "ГАЗ";
            case "X99" -> "УАЗ";
            case "X9F", "X9K", "X9M" -> "КАМАЗ";
            case "ZAR", "ZFA", "ZFR", "ZGA", "ZLA" -> "Fiat/Italian";
            case "WBA", "WBS", "WBX", "WBY" -> "BMW";
            case "WDB", "WDC", "WDD", "WDF", "W1K" -> "Mercedes-Benz";
            case "JTD", "JTJ", "JTM", "JTN", "JT8" -> "Toyota";
            case "1F", "2F", "3F" -> "Ford";
            case "1G", "2G", "3G" -> "General Motors";
            case "1H", "3H", "5H" -> "Honda";
            case "1N", "2N", "3N" -> "Nissan";
            default -> "Не определен";
        };
    }

    private Integer extractYearFromVin(String vin) {
        if (vin.length() < 10) return null;

        char yearChar = vin.charAt(9);
        // Коды годов для автомобилей после 2000 года
        // 1=2001, 2=2002, ..., 9=2009, A=2010, B=2011, ..., Y=2030
        if (yearChar >= '1' && yearChar <= '9') {
            return 2000 + (yearChar - '0');
        } else if (yearChar >= 'A' && yearChar <= 'Y') {
            // A=2010, B=2011, ..., Y=2030 (пропущены I, O, Q, U)
            int offset = yearChar - 'A' + 1;
            if (yearChar > 'I') offset--;
            if (yearChar > 'O') offset--;
            if (yearChar > 'Q') offset--;
            if (yearChar > 'U') offset--;
            return 2010 + offset;
        }
        return null;
    }
}