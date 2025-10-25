package com.saxion.proj.tfms.planner.services.ParcelServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.ParcelDao;
import com.saxion.proj.tfms.planner.abstractions.ParcelServices.IGetParcelById;
import com.saxion.proj.tfms.planner.dto.ParcelResponseDto;
import com.saxion.proj.tfms.planner.repository.ParcelRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class GetParcelByIdHandler implements IGetParcelById {

    private final ParcelRepository parcelRepository;
    private final ParcelMapperHandler parcelMapper;

    public GetParcelByIdHandler(ParcelRepository parcelRepository, ParcelMapperHandler parcelMapper) {
        this.parcelRepository = parcelRepository;
        this.parcelMapper = parcelMapper;
    }

    @Override
    public ApiResponse<ParcelResponseDto> Handle(Long parcelId) {
        if (parcelId == null || parcelId <= 0) {
            return ApiResponse.error("Invalid parcel ID");
        }

        Optional<ParcelDao> parcelOpt = parcelRepository.findByIdWithRelations(parcelId);

        if (parcelOpt.isEmpty()) {
            return ApiResponse.error("Parcel not found");
        }

        ParcelResponseDto dto = parcelMapper.toDto(parcelOpt.get());
        return ApiResponse.success(dto);
    }
}