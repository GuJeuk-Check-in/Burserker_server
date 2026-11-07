package com.example.burserker.beopdong.domain.check_in.service.util;

import com.example.burserker.beopdong.domain.organ.domain.Organ;
import com.example.burserker.beopdong.domain.organ.domain.repository.OrganRepository;
import com.example.burserker.beopdong.domain.organ.exception.OrganNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsageLockService {

    private final OrganRepository organRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void lock() {
        Organ organ = organRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> OrganNotFoundException.EXCEPTION);

        organRepository.findWithLockById(organ.getId())
                .orElseThrow(() -> OrganNotFoundException.EXCEPTION);
    }
}