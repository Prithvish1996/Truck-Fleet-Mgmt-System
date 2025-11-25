package com.saxion.proj.tfms.planner.services.driverServices;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.DriverAvailabilityDao;
import com.saxion.proj.tfms.commons.model.DriverDao;
import com.saxion.proj.tfms.commons.model.DriverSuggestionDao;
import com.saxion.proj.tfms.planner.abstractions.driverServices.ICreateDriverSuggestion;
import com.saxion.proj.tfms.planner.repository.DriverAvailabilityRepository;
import com.saxion.proj.tfms.planner.repository.DriverRepository;
import com.saxion.proj.tfms.planner.repository.DriverSuggestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Qualifier("createDriverSuggestion")
public class CreateDriverSuggestionHandler implements ICreateDriverSuggestion {
    private final DriverRepository driverRepository;
    private final DriverSuggestionRepository suggestionRepository;

    @Autowired
    public CreateDriverSuggestionHandler(DriverRepository driverRepository,
                                           DriverSuggestionRepository suggestionRepository) {
        this.driverRepository = driverRepository;
        this.suggestionRepository = suggestionRepository;
    }

    @Override
    @Transactional
    public ApiResponse<String> Handle(Long driverId, String suggestion) {

        if (driverId == null || driverId <= 0) {
            return ApiResponse.error("Invalid driver ID");
        }

        Optional<DriverDao> driverOpt = driverRepository.findById(driverId);
        if (driverOpt.isEmpty()) {
            return ApiResponse.error("Driver not found");
        }

        if (suggestion == null || suggestion.isBlank()) {
            return ApiResponse.error("Suggestion cannot be empty");
        }

        boolean exists = suggestionRepository.existsByDriver_IdAndSuggestion(driverId, suggestion.trim());
        if (exists) {
            return ApiResponse.error("Duplicate suggestion");
        }

        DriverDao driver = driverOpt.get();

        // save suggestion
        DriverSuggestionDao entity = new DriverSuggestionDao();
        entity.setSuggestion(suggestion);
        entity.setDriver(driver);
        suggestionRepository.save(entity);

        return ApiResponse.success("Suggestion saved successfully");
    }
}