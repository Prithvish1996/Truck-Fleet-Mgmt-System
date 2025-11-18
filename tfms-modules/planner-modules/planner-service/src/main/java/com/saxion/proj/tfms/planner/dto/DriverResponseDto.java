package com.saxion.proj.tfms.planner.dto;

import com.saxion.proj.tfms.commons.model.DriverDao;
import com.saxion.proj.tfms.commons.model.DriverSuggestionDao;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverResponseDto {
    private Long id;
    private String Name;
    private String userName;
    private String email;
    private Boolean isAvailable;

    // Optional fields for displaying location info
    private String city;
    private String address;
    private Double latitude;
    private Double longitude;

    // Driver availability List
    private List<DriverAvailabilityResponseDto> availability;

    // Driver suggestion List
    private List<DriverSuggestionResponseDto> suggestions;

    public DriverResponseDto(Long id, String userName, String email, boolean isAvailable, String city, String address) {
        this.id = id;
        this.userName = userName;
        this.city = city;
        this.email = email;
        this.isAvailable = isAvailable;
        this.address = address;
    }

    /**
     * Map from DriverDao entity to DTO
     */
    public static DriverResponseDto fromEntity(DriverDao driver) {
        if (driver == null) return null;

        String city = driver.getLocation() != null ? driver.getLocation().getCity() : null;
        String address = driver.getLocation() != null ? driver.getLocation().getAddress() : null;

        DriverResponseDto dto = new DriverResponseDto(
                driver.getId(),
                driver.getUser().getUsername(),
                driver.getUser().getEmail(),
                driver.getIsAvailable(),
                city,
                address
        );

        // Map availability (ONLY future dates)
        ZonedDateTime now = ZonedDateTime.now();

        List<DriverAvailabilityResponseDto> availabilityDtos = driver.getAvailabilities()
                .stream()
                .filter(a -> a.getAvailableAt() != null && !a.getAvailableAt().isBefore(now))
                .map(a -> new DriverAvailabilityResponseDto(
                        a.getId(),
                        a.getAvailableAt(),
                        a.getStartTime(),
                        a.getEndTime(),
                        a.getStatus().name()
                ))
                .toList();

        dto.setAvailability(availabilityDtos);

        // ---- Suggestions ----
        List<DriverSuggestionResponseDto> driverSuggestions = driver.getSuggestions()
                .stream()
                .filter(a -> a.getSuggestion() != null)
                .map(a -> new DriverSuggestionResponseDto(
                        a.getId(),
                        a.getSuggestion(),
                        a.getCreatedAt()
                ))
                .toList();

        dto.setSuggestions(driverSuggestions);

        return dto;
    }

}
