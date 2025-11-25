package com.saxion.proj.tfms.planner.services.driverServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.DriverDao;
import com.saxion.proj.tfms.planner.abstractions.driverServices.IGetDriverById;
import com.saxion.proj.tfms.planner.dto.DriverResponseDto;
import com.saxion.proj.tfms.planner.repository.DriverRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Qualifier("getDriverByIdHandler")
@Transactional
public class GetDriverHandler implements IGetDriverById {

    private final DriverRepository driverRepository;

    public GetDriverHandler(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    @Override
    public ApiResponse<DriverResponseDto> Handle(Long driverId) {
        if (driverId == null || driverId <= 0) {
            return ApiResponse.error("Invalid driver ID");
        }

        Optional<DriverDao> driverOpt = driverRepository.findById(driverId);

        if (driverOpt.isEmpty()) {
            return ApiResponse.error("Driver not found");
        }

        DriverResponseDto responseDto = DriverResponseDto.fromEntity(driverOpt.get());

        return ApiResponse.success(responseDto);
    }
}
