package com.saxion.proj.tfms.planner.controller;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.security.UserContext;
import com.saxion.proj.tfms.commons.security.annotations.CurrentUser;
import com.saxion.proj.tfms.planner.dto.*;
import com.saxion.proj.tfms.planner.services.MockPlannerDataProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/planner")
@RequiredArgsConstructor
public class PlannerWorkflowController {

    private final MockPlannerDataProvider dataProvider;

    private boolean hasPlannerAccess(UserContext user) {
        return user != null && user.isValid() && Objects.equals(user.getRole(), "PLANNER");
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSummary(@CurrentUser UserContext user) {
        if (!hasPlannerAccess(user)) {
            return ResponseEntity.status(403).body(ApiResponse.error("Not authorized"));
        }
        return ResponseEntity.ok(ApiResponse.success(dataProvider.summarizeStatus()));
    }

    @GetMapping("/drivers/available")
    public ResponseEntity<ApiResponse<List<DriverSummaryDto>>> getDrivers(@CurrentUser UserContext user) {
        if (!hasPlannerAccess(user)) {
            return ResponseEntity.status(403).body(ApiResponse.error("Not authorized"));
        }
        return ResponseEntity.ok(ApiResponse.success(dataProvider.getDrivers()));
    }

    @GetMapping("/trucks/available")
    public ResponseEntity<ApiResponse<List<TruckSummaryDto>>> getTrucks(@CurrentUser UserContext user) {
        if (!hasPlannerAccess(user)) {
            return ResponseEntity.status(403).body(ApiResponse.error("Not authorized"));
        }
        return ResponseEntity.ok(ApiResponse.success(dataProvider.getTrucks()));
    }

    @GetMapping("/parcels/pending")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPendingParcels(@CurrentUser UserContext user) {
        if (!hasPlannerAccess(user)) {
            return ResponseEntity.status(403).body(ApiResponse.error("Not authorized"));
        }
        return ResponseEntity.ok(ApiResponse.success(dataProvider.getParcels()));
    }

    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<PlannerRequestDto>>> getRequests(@CurrentUser UserContext user,
                                                                            @RequestParam(required = false) String priority) {
        if (!hasPlannerAccess(user)) {
            return ResponseEntity.status(403).body(ApiResponse.error("Not authorized"));
        }
        List<PlannerRequestDto> requests = dataProvider.getRequests();
        if (StringUtils.hasText(priority)) {
            requests = requests.stream()
                    .filter(r -> priority.equalsIgnoreCase(r.getPriority()))
                    .collect(Collectors.toList());
        }
        return ResponseEntity.ok(ApiResponse.success(requests));
    }

    @PostMapping("/requests")
    public ResponseEntity<ApiResponse<PlannerRequestDto>> createRequest(@CurrentUser UserContext user,
                                                                        @Valid @RequestBody ScheduleRequestDto scheduleRequest) {
        if (!hasPlannerAccess(user)) {
            return ResponseEntity.status(403).body(ApiResponse.error("Not authorized"));
        }
        PlannerRequestDto created = dataProvider.createRequest(scheduleRequest);
        return ResponseEntity.ok(ApiResponse.success(created));
    }

    @PostMapping("/assignments/preview")
    public ResponseEntity<ApiResponse<List<PlannerAssignmentDto>>> previewAssignments(@CurrentUser UserContext user,
                                                                                      @RequestBody Map<String, List<String>> payload) {
        if (!hasPlannerAccess(user)) {
            return ResponseEntity.status(403).body(ApiResponse.error("Not authorized"));
        }
        List<String> requestIds = payload.getOrDefault("requestIds", List.of());
        return ResponseEntity.ok(ApiResponse.success(dataProvider.previewAssignments(requestIds)));
    }

    @GetMapping("/assignments")
    public ResponseEntity<ApiResponse<List<PlannerAssignmentDto>>> getAssignments(@CurrentUser UserContext user,
                                                                                  @RequestParam(required = false) String status) {
        if (!hasPlannerAccess(user)) {
            return ResponseEntity.status(403).body(ApiResponse.error("Not authorized"));
        }
        List<PlannerAssignmentDto> assignments = dataProvider.getAssignments();
        if (StringUtils.hasText(status)) {
            assignments = assignments.stream()
                    .filter(a -> status.equalsIgnoreCase(a.getStatus()))
                    .collect(Collectors.toList());
        }
        return ResponseEntity.ok(ApiResponse.success(assignments));
    }

    @PostMapping("/assignments")
    public ResponseEntity<ApiResponse<Void>> updateAssignments(@CurrentUser UserContext user,
                                                               @Valid @RequestBody PlannerAssignmentUpdateDto dto) {
        if (!hasPlannerAccess(user)) {
            return ResponseEntity.status(403).body(ApiResponse.error("Not authorized"));
        }
        dataProvider.updateAssignments(dto.getAssignments());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/routes/{assignmentId}")
    public ResponseEntity<ApiResponse<PlannerRouteDto>> getRoute(@CurrentUser UserContext user,
                                                                 @PathVariable String assignmentId) {
        if (!hasPlannerAccess(user)) {
            return ResponseEntity.status(403).body(ApiResponse.error("Not authorized"));
        }
        PlannerRouteDto route = dataProvider.getRoute(assignmentId);
        if (route == null) {
            return ResponseEntity.status(404).body(ApiResponse.error("Route not found"));
        }
        return ResponseEntity.ok(ApiResponse.success(route));
    }
}

