package org.example.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.VisitDto;
import org.example.repository.VisitRepository;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class VisitService {
    private final VisitRepository visitRepository;

    public VisitService(VisitRepository visitRepository) {
        this.visitRepository = visitRepository;
    }

    @PostConstruct
    public void init() {
        log.debug("Initializing Visit Cache");
        visitRepository.deleteAll();
    }

    public synchronized void incrementVisit(String url) {
        VisitDto visitDto = visitRepository.findByUrl(url)
                .orElseGet(() -> {
                    VisitDto newvisitDto = new VisitDto();
                    newvisitDto.setUrl(url);
                    newvisitDto.setVisitCount(0);
                    return newvisitDto;
                });
        visitDto.setVisitCount(visitDto.getVisitCount() + 1);
        visitRepository.save(visitDto);
    }

    public long getVisitCount(String url) {
        return visitRepository.findByUrl(url)
                .map(VisitDto::getVisitCount)
                .orElse(0L);
    }
}