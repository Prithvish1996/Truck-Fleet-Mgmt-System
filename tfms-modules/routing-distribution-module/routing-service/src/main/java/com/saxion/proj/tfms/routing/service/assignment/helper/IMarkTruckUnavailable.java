package com.saxion.proj.tfms.routing.service.assignment.helper;

import com.saxion.proj.tfms.routing.model.TruckInfo;

public interface IMarkTruckUnavailable {
    public TruckInfo byTruckUniqueId(Long truckId);
    public TruckInfo byTruckUniqueName(String truckName);
}
