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

    public static StopDto fromEntity(RouteStopDao stop) {
        StopDto dto = new StopDto();
        dto.setStopId(stop.getId());
        dto.setPriority(stop.getPriority());
        dto.setStopType(stop.getStopType());
        dto.setParcelsToDeliver(
                stop.getParcels() != null
                        ? stop.getParcels().stream().map(ParcelResponseDto::fromEntity).collect(Collectors.toList())
                        : null
        );
        return dto;
    }
}
