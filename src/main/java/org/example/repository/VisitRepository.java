package org.example.repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.example.dto.VisitDto;
import org.springframework.stereotype.Repository;

@Repository
public class VisitRepository {
    private final Map<String, VisitDto> visitMap = new ConcurrentHashMap<>();

    public Optional<VisitDto> findByUrl(String url) {
        return Optional.ofNullable(visitMap.get(url));
    }

    public synchronized VisitDto save(VisitDto visitDto) {
        visitMap.put(visitDto.getUrl(), visitDto);
        return visitDto;
    }

    public void deleteAll() {
        visitMap.clear();
    }
}