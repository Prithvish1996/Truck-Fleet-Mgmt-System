package com.saxion.proj.tfms.routing.service.assignment.helper.impl;

import com.saxion.proj.tfms.commons.model.TruckDao;
import com.saxion.proj.tfms.routing.model.TruckInfo;
import com.saxion.proj.tfms.routing.repository.TruckNeededForRoutingRepository;
import com.saxion.proj.tfms.routing.service.assignment.helper.IGetAllTrucksAvailable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service("GetAllTrucksAvailableCheckForIsAvailable")
public class GetAllTrucksAvailableCheckIsAvailable implements IGetAllTrucksAvailable {

    private final TruckNeededForRoutingRepository truckNeededForRoutingRepository;

    public GetAllTrucksAvailableCheckIsAvailable(TruckNeededForRoutingRepository truckNeededForRoutingRepository) {
        this.truckNeededForRoutingRepository = truckNeededForRoutingRepository;
    }

    public List<TruckInfo> getAvailableTrucks() {
        List<TruckDao> availableTrucks = truckNeededForRoutingRepository.findByIsAvailableTrue();
        return availableTrucks.stream()
                .map(truck -> new TruckInfo(
                        truck.getId(),
                        truck.getName(),
                        truck.getVolume() != null ? truck.getVolume() : 0.0
                ))
                .collect(Collectors.toList());
    }

}
