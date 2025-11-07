package com.saxion.proj.tfms.planner.dto.routing.model;

import com.saxion.proj.tfms.commons.constants.StopType;
import com.saxion.proj.tfms.planner.dto.LocationResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stop {
    private LocationResponseDto coordinates;
    private List<Parcel> parcelsToDeliver = new ArrayList<>();
    StopType stopType;

    public static void addOrUpdateStop(List<Stop> stops, Stop newStop) {
        for (Stop existing : stops) {
            if (existing.hasSameCoordinates(newStop)) {
                existing.getParcelsToDeliver().addAll(newStop.getParcelsToDeliver());
                return;
            }
        }
        stops.add(newStop);
    }

    public boolean hasSameCoordinates(Stop other) {
        if (this.coordinates == null || other.coordinates == null) return false;
        return Objects.equals(this.coordinates.getLatitude(), other.coordinates.getLatitude()) &&
                Objects.equals(this.coordinates.getLongitude(), other.coordinates.getLongitude());
    }
}