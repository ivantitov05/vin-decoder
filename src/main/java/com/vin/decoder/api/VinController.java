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

    @GetMapping("/check")
    public ResponseEntity<VinResponseDto> checkVIN(@RequestParam String vin) {
        Long userId = getCurrentUserId();
        CarInfo carInfo = vinCheckService.checkVIN(vin, userId);

        VinResponseDto response = vinMapper.toResponseDto(
                carInfo, null, "SUCCESS", LocalDateTime.now()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<VinResponseDto>> getHistory() {
        Long userId = getCurrentUserId();
        List<VinRequest> history = vinCheckService.getUserHistory(userId);
        return ResponseEntity.ok(vinMapper.toResponseDtoList(history));
    }
}