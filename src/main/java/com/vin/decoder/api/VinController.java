package com.vin.decoder.api;

import com.vin.decoder.model.CarInfo;
import com.vin.decoder.service.VinCheckService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class VinController {
    private final VinCheckService vinCheckService;

    public VinController(VinCheckService vinCheckService){
        this.vinCheckService = vinCheckService;
    }

    @GetMapping
    public ResponseEntity<CarInfo> checkVIN(@RequestParam String vin){
        Long userID = 1L;
        CarInfo result = vinCheckService.checkVIN(vin);
        return ResponseEntity.ok(result);
    }


}
