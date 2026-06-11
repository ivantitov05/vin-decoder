package com.vin.decoder.api;

import com.vin.decoder.model.CarInfo;
import com.vin.decoder.model.User;
import com.vin.decoder.model.VinRequest;
import com.vin.decoder.repository.UserRepository;
import com.vin.decoder.service.VinCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class VinController {

    private final VinCheckService vinCheckService;
    private final UserRepository userRepository;

    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    @GetMapping("/check")
    public ResponseEntity<CarInfo> checkVIN(@RequestParam String vin) {
        Long userId = getCurrentUserId();
        CarInfo result = vinCheckService.checkVIN(vin, userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    public ResponseEntity<List<VinRequest>> getHistory() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(vinCheckService.getUserHistory(userId));
    }
}