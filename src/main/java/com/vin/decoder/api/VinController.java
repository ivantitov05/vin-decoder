package com.vin.decoder.api;

import com.vin.decoder.dto.VinResponseDto;
import com.vin.decoder.mapper.VinMapper;
import com.vin.decoder.model.CarInfo;
import com.vin.decoder.model.User;
import com.vin.decoder.model.VinRequest;
import com.vin.decoder.repository.UserRepository;
import com.vin.decoder.service.VinCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class VinController {

    private final VinCheckService vinCheckService;
    private final UserRepository userRepository;
    private final VinMapper vinMapper;

    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    @PostMapping("/check")
    public ResponseEntity<?> checkVin(@RequestParam String vin) {
        try {
            Long userId = getCurrentUserId();
            CarInfo carInfo = vinCheckService.checkVin(vin, userId);

            return ResponseEntity.ok(VinResponseDto.builder()
                    .vin(vin)
                    .brand(carInfo.getBrand())
                    .model(carInfo.getModel())
                    .year(carInfo.getYear())
                    .status("SUCCESS")
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<VinResponseDto>> getHistory() {
        Long userId = getCurrentUserId();
        List<VinRequest> history = vinCheckService.getUserHistory(userId);
        return ResponseEntity.ok(vinMapper.toResponseDtoList(history));
    }
}