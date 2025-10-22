package com.saxion.proj.tfms.planner.services.ParcelServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.logging.ServiceLogger;
import com.saxion.proj.tfms.commons.logging.ServiceName;
import com.saxion.proj.tfms.commons.model.ParcelDao;
import com.saxion.proj.tfms.planner.abstractions.ParcelServices.ICreateParcel;
import com.saxion.proj.tfms.planner.dto.ParcelRequestDto;
import com.saxion.proj.tfms.planner.dto.ParcelResponseDto;
import com.saxion.proj.tfms.planner.repository.ParcelRepository;
import com.saxion.proj.tfms.planner.repository.WarehouseRepository;
import org.springframework.stereotype.Service;

@Service
public class CreateParcelHandler implements ICreateParcel {
    private final ParcelRepository parcelRepository;
    private final ParcelMapperHandler parcelMapper;
    private final WarehouseRepository warehouseRepository;
    private static final ServiceLogger logger = ServiceLogger.getLogger(CreateParcelHandler.class);


    public CreateParcelHandler(ParcelRepository parcelRepository,
                                ParcelMapperHandler parcelMapper,
                                WarehouseRepository warehouseRepository) {
        this.parcelRepository = parcelRepository;
        this.parcelMapper = parcelMapper;
        this.warehouseRepository = warehouseRepository;
    }

    @Override
    public ApiResponse<ParcelResponseDto> Handle(ParcelRequestDto dto) {
        logger.infoOp(ServiceName.PARCEL_SERVICE, "CREATE", "Creation of new parcel: {}", dto.getName());

        if (parcelRepository.existsByName(dto.getName())) {
            return ApiResponse.error("Parcel name already exists");
        }

        // 🔹 Fetch warehouse by ID
        var warehouse = warehouseRepository.findById(dto.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Warehouse not found with ID: " + dto.getWarehouseId()));

        ParcelDao parcel = new ParcelDao();
        parcel.setName(dto.getName());
        parcel.setAddress(dto.getAddress());
        parcel.setPostalcode(dto.getPostalCode());
        parcel.setWeight(dto.getWeight());
        parcel.setCity(dto.getCity());
        parcel.setWarehouse(warehouse);
        parcel.setStatus(ParcelDao.StatusEnum.PENDING);
        parcel.setDeliveryInstructions(dto.getDeliveryInstructions());
        parcel.setRecipientName(dto.getRecipientName());
        parcel.setRecipientPhone(dto.getRecipientPhone());
        parcel.setLatitude(dto.getLatitude());
        parcel.setLongitude(dto.getLongitude());
        parcel.setActive(true);

        parcelRepository.save(parcel);
        logger.infoOp(ServiceName.PARCEL_SERVICE, "CREATE", "Successfully saved the newly created parcel: {}", dto.getName());

        return ApiResponse.success(parcelMapper.toDto(parcel));
    }
}