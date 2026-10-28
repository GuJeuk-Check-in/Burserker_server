package com.example.burserker.beopdong.domain.organ.facade;

import com.example.burserker.beopdong.domain.organ.domain.Organ;
import com.example.burserker.beopdong.domain.organ.domain.repository.OrganRepository;
import com.example.burserker.beopdong.domain.organ.exception.OrganNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrganFacade {
    private final OrganRepository organRepository;

    public Organ getOrganById(Long organId) {
        return organRepository.findById(organId)
                .orElseThrow(() -> OrganNotFoundException.EXCEPTION);
    }

    public Organ getOrganReference(Long organId) {
        return organRepository.getReferenceById(organId);
    }
}
