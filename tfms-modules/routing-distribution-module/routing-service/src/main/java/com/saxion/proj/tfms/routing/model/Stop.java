package com.saxion.proj.tfms.routing.model;

import com.saxion.proj.tfms.routing.constant.StopType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class Stop {
    private Coordinates coordinates;
    private List<Parcel> parcelsToDeliver = new CopyOnWriteArrayList<>();
    StopType stopType;




    public static void addOrUpdateStop(List<Stop> stops, Stop newStop) {
        synchronized (stops) {  // synchronize on the shared list
            for (Stop existing : stops) {
                if (existing.hasSameCoordinates(newStop)) {
                    existing.getParcelsToDeliver().addAll(newStop.getParcelsToDeliver());
                    return;
                }
            }
            stops.add(newStop);
        }
    }


    public boolean hasSameCoordinates(Stop other) {
        if (this.coordinates == null || other.coordinates == null) return false;
        return Objects.equals(this.coordinates.getLatitude(), other.coordinates.getLatitude()) &&
                Objects.equals(this.coordinates.getLongitude(), other.coordinates.getLongitude());
    }
}
