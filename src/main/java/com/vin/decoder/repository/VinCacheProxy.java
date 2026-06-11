package com.vin.decoder.repository;

import com.vin.decoder.model.VinRequest;
import com.vin.decoder.repository.VinHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@RequiredArgsConstructor
public class VinCacheProxy {
    private final VinHistoryRepository repository;
    private final Map<String, VinRequest> cache = new ConcurrentHashMap<>();

    public Optional<VinRequest> findByVinAndUserId(String vin, Long userId) {
        // сначала кэш, потом репозиторий
        return Optional.ofNullable(cache.get(vin))
                .or(() -> repository.findByVinAndUserId(vin, userId));
    }

    public VinRequest save(VinRequest request) {
        cache.put(request.getVin(), request);
        return repository.save(request);
    }

    public List<VinRequest> findAllByUserIdOrderByCreatedAtDesc(Long userId) {
        return repository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }
}