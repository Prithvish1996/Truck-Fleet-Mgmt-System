package com.saxion.proj.tfms.routing.service.assignment.helper.impl;

import com.saxion.proj.tfms.commons.model.TruckDao;
import com.saxion.proj.tfms.routing.model.TruckInfo;
import com.saxion.proj.tfms.routing.repository.TruckNeededForRoutingRepository;
import com.saxion.proj.tfms.routing.service.assignment.helper.IMarkTruckUnavailable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service("MarkTruckUnavailableUsingKeys")
public class MarkTruckUnavailableUsingKeys implements IMarkTruckUnavailable {

    private final TruckNeededForRoutingRepository truckNeededForRoutingRepository;

    public MarkTruckUnavailableUsingKeys(TruckNeededForRoutingRepository truckNeededForRoutingRepository) {
        this.truckNeededForRoutingRepository = truckNeededForRoutingRepository;
    }

    @Override
    @Transactional
    public TruckInfo byTruckUniqueName(String truckName) {
        TruckDao truck = truckNeededForRoutingRepository.findByPlateNumber(truckName)
                .orElseThrow(() -> new NoSuchElementException("Truck not found with name: " + truckName));

        truck.setIsAvailable(false);
        TruckDao updated = truckNeededForRoutingRepository.save(truck);

        return new TruckInfo(
                updated.getId(),
                updated.getPlateNumber(),
                updated.getVolume() != null ? updated.getVolume() : 0.0
        );
    }

    @Override
    @Transactional
    public TruckInfo byTruckUniqueId(Long truckId) {
        TruckDao truck = truckNeededForRoutingRepository.findById(truckId)
                .orElseThrow(() -> new NoSuchElementException("Truck not found with id: " + truckId));

        truck.setIsAvailable(false);
        TruckDao updated = truckNeededForRoutingRepository.save(truck);

        return new TruckInfo(
                updated.getId(),
                updated.getPlateNumber(),
                updated.getVolume() != null ? updated.getVolume() : 0.0
        );
    }
}
