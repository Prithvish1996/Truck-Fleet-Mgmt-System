package com.saxion.proj.tfms.planner.abstractions.depotServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface IListDepot {
    ApiResponse<Map<String, Object>> Handle(Pageable pageable);
}
