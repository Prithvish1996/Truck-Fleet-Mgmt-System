package com.saxion.proj.tfms.routing.service.assignment.helper;

import com.saxion.proj.tfms.routing.model.TruckInfo;

import java.util.List;

public interface IGetAllTrucksAvailable {
    public List<TruckInfo> getAvailableTrucks();
}
