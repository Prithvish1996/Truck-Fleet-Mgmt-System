package com.saxion.proj.tfms.planner.dto;

import com.saxion.proj.tfms.commons.constants.StopType;
import com.saxion.proj.tfms.commons.model.RouteStopDao;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StopDto {
    private Long stopId;
    private List<ParcelResponseDto> parcelsToDeliver;
    private int priority;
    private StopType stopType;
    private LocationResponseDto location;

    public static StopDto fromEntity(RouteStopDao stop) {
        StopDto dto = new StopDto();
        dto.setStopId(stop.getId());
        dto.setPriority(stop.getPriority());
        dto.setStopType(stop.getStopType());

        // Only set location for WAREHOUSE or DEPOT
        if (stop.getStopType() == StopType.WAREHOUSE && stop.getLocation() != null) {
            dto.setLocation(LocationResponseDto.fromEntity(stop.getLocation()));
        }
        else if (stop.getStopType() == StopType.DEPOT && stop.getLocation() != null) {
            dto.setLocation(LocationResponseDto.fromEntity(stop.getLocation()));
        }
        else {
            dto.setLocation(null);  // CUSTOMER → no location needed because parcel has the delivery location coordinate
        }

        dto.setParcelsToDeliver(
                stop.getParcels() != null
                        ? stop.getParcels().stream().map(ParcelResponseDto::fromEntity).collect(Collectors.toList())
                        : null
        );
        return dto;
    }
}
