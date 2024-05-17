package edu.tinkoff.imageeditorapi.service;

import edu.tinkoff.imageeditorapi.entity.PreferencesIntegerEntity;
import edu.tinkoff.imageeditorapi.repository.PreferencesIntegerRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PreferencesService {

    private static final String REMAINING_IMAGGA_REQUESTS = "rem_imagga_req";

    private final PreferencesIntegerRepository preferencesIntegerRepository;

    @PostConstruct
    public void init() {
        if (!preferencesIntegerRepository.existsById(REMAINING_IMAGGA_REQUESTS)) {
            var preference = new PreferencesIntegerEntity(REMAINING_IMAGGA_REQUESTS,
                    1000);
            preferencesIntegerRepository.save(preference);
        }
    }

    public int getRemainingImaggaRequests() {
        return preferencesIntegerRepository.getReferenceById(REMAINING_IMAGGA_REQUESTS).getValue();
    }

    @Transactional
    public void decrementRemainingImaggaRequests() {
        var preference = preferencesIntegerRepository.getReferenceById(REMAINING_IMAGGA_REQUESTS);
        preference.setValue(preference.getValue() - 1);
        preferencesIntegerRepository.save(preference);
    }

}
