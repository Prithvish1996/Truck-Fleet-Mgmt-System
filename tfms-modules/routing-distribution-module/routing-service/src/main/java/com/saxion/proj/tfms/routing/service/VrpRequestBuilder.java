package com.saxion.proj.tfms.routing.service;

import com.saxion.proj.tfms.commons.model.DepotDao;
import com.saxion.proj.tfms.commons.model.TruckDao;
import com.saxion.proj.tfms.commons.model.ParcelDao;
import com.saxion.proj.tfms.routing.dto.*;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class VrpRequestBuilder {

    private final ModelMapper mapper;

    public VrpRequestBuilder(ModelMapper mapper) {
        this.mapper = mapper;
        setupMappings();
    }

    private void setupMappings() {
        // DepotDao -> DepotInfo
        mapper.typeMap(DepotDao.class, DepotInfo.class).addMappings(m -> {
            m.map(src -> src.getLocation().getLatitude(), DepotInfo::setLatitude);
            m.map(src -> src.getLocation().getLongitude(), DepotInfo::setLongitude);
        });

        // TruckDao -> TruckInfo
        mapper.typeMap(TruckDao.class, TruckInfo.class).addMappings(m -> {
            m.map(TruckDao::getName, TruckInfo::setTruckName);
            m.map(TruckDao::getVolume, TruckInfo::setVolume);
        });

        // ParcelDao -> ParcelInfo
        mapper.typeMap(ParcelDao.class, ParcelInfo.class).addMappings(m -> {
            m.map(ParcelDao::getName, ParcelInfo::setParcelName);
            m.map(ParcelDao::getVolume, ParcelInfo::setVolume);
            m.map(src -> src.getWarehouse().getId().toString(), ParcelInfo::setWarehouseId);
            m.map(src -> src.getWarehouse().getLocation().getLatitude(), ParcelInfo::setWarehouseLatitude);
            m.map(src -> src.getWarehouse().getLocation().getLongitude(), ParcelInfo::setWarehouseLongitude);
            m.map(src -> src.getDeliveryLocation() != null ? src.getDeliveryLocation().getLatitude() : 0.0, ParcelInfo::setDeliveryLatitude);
            m.map(src -> src.getDeliveryLocation() != null ? src.getDeliveryLocation().getLongitude() : 0.0, ParcelInfo::setDeliveryLongitude);
            m.map(ParcelDao::getRecipientName, ParcelInfo::setRecipientName);
        });
    }

    public VrpRequestDto build(DepotDao depotDao, List<TruckDao> trucks, List<ParcelDao> parcels) {

        DepotInfo depotInfo = mapper.map(depotDao, DepotInfo.class);

        List<TruckInfo> truckInfos = trucks.stream()
                .map(truck -> mapper.map(truck, TruckInfo.class))
                .collect(Collectors.toList());

        List<ParcelInfo> parcelInfos = parcels.stream()
                .map(parcel -> mapper.map(parcel, ParcelInfo.class))
                .collect(Collectors.toList());

        return new VrpRequestDto(depotInfo, truckInfos, parcelInfos, VrpMetric.DISTANCE);
    }
}

