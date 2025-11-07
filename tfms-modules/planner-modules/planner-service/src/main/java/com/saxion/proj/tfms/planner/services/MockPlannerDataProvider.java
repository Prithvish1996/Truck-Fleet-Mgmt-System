package com.saxion.proj.tfms.planner.services;

import com.saxion.proj.tfms.planner.dto.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Component
public class MockPlannerDataProvider {

    private final List<DriverSummaryDto> drivers;
    private final List<TruckSummaryDto> trucks;
    private final List<Map<String, Object>> parcels;
    private final List<PlannerRequestDto> requests;
    private final List<PlannerAssignmentDto> assignments;
    private final Map<String, PlannerRouteDto> routes;
    private final Random random = new Random();

    public MockPlannerDataProvider() {
        this.drivers = initDrivers();
        this.trucks = initTrucks();
        this.parcels = initParcels();
        this.requests = new CopyOnWriteArrayList<>(initRequests());
        this.assignments = new CopyOnWriteArrayList<>(initAssignments());
        this.routes = initRoutes();
    }

    public List<DriverSummaryDto> getDrivers() {
        return drivers;
    }

    public List<TruckSummaryDto> getTrucks() {
        return trucks;
    }

    public List<Map<String, Object>> getParcels() {
        return parcels;
    }

    public List<PlannerRequestDto> getRequests() {
        return requests;
    }

    public PlannerRequestDto createRequest(ScheduleRequestDto scheduleRequest) {
        String requestId = "REQ-" + (requests.size() + 1000);
        PlannerRequestDto request = PlannerRequestDto.builder()
                .requestId(requestId)
                .truckPlateId(scheduleRequest.getTruckPlateId())
                .deliveryDate(scheduleRequest.getDeliveryDate().atTime(17, 0))
                .parcelCount(scheduleRequest.getParcelIds().size())
                .warehouse(findTruckWarehouse(scheduleRequest.getTruckPlateId()))
                .priority(scheduleRequest.getPriority())
                .parcelIds(new ArrayList<>(scheduleRequest.getParcelIds()))
                .build();
        requests.add(0, request);
        return request;
    }

    public List<PlannerAssignmentDto> getAssignments() {
        return assignments;
    }

    public void updateAssignments(List<PlannerAssignmentUpdateDto.AssignmentUpdateItem> updates) {
        Map<String, DriverSummaryDto> driverMap = drivers.stream()
                .collect(Collectors.toMap(DriverSummaryDto::getId, d -> d));
        for (PlannerAssignmentUpdateDto.AssignmentUpdateItem update : updates) {
            assignments.stream()
                    .filter(a -> a.getAssignmentId().equals(update.getAssignmentId()))
                    .findFirst()
                    .ifPresent(assignment -> {
                        assignment.setDriverId(update.getDriverId());
                        DriverSummaryDto driver = driverMap.get(update.getDriverId());
                        assignment.setDriverName(driver != null ? driver.getName() : null);
                        assignment.setStatus("SCHEDULED");
                    });
        }
    }

    public List<PlannerAssignmentDto> previewAssignments(List<String> requestIds) {
        return requestIds.stream()
                .map(this::createPreviewAssignment)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public PlannerRouteDto getRoute(String assignmentId) {
        return routes.get(assignmentId);
    }

    private PlannerAssignmentDto createPreviewAssignment(String requestId) {
        PlannerRequestDto request = requests.stream()
                .filter(r -> r.getRequestId().equals(requestId))
                .findFirst()
                .orElse(null);
        if (request == null) {
            return null;
        }
        return PlannerAssignmentDto.builder()
                .assignmentId("ASN-PREVIEW-" + requestId)
                .truckPlateId(request.getTruckPlateId())
                .date(request.getDeliveryDate())
                .parcelCount(request.getParcelCount())
                .driverId(null)
                .driverName(null)
                .requestIds(Collections.singletonList(requestId))
                .status("PREVIEW")
                .build();
    }

    private String findTruckWarehouse(String plateId) {
        return trucks.stream()
                .filter(t -> t.getPlateId().equals(plateId))
                .map(TruckSummaryDto::getWarehouse)
                .findFirst()
                .orElse("Amazon DUS2");
    }

    private List<DriverSummaryDto> initDrivers() {
        return List.of(
                DriverSummaryDto.builder()
                        .id("drv-tom")
                        .name("Tom Janssen")
                        .email("tom.janssen@example.com")
                        .licenses(List.of("B", "C1"))
                        .workWindowStart("07:30")
                        .workWindowEnd("17:30")
                        .build(),
                DriverSummaryDto.builder()
                        .id("drv-jack")
                        .name("Jack Visser")
                        .email("jack.visser@example.com")
                        .licenses(List.of("B", "BE"))
                        .workWindowStart("08:30")
                        .workWindowEnd("17:30")
                        .build(),
                DriverSummaryDto.builder()
                        .id("drv-frank")
                        .name("Frank Bakker")
                        .email("frank.bakker@example.com")
                        .licenses(List.of("B", "C1"))
                        .workWindowStart("09:00")
                        .workWindowEnd("18:00")
                        .build(),
                DriverSummaryDto.builder()
                        .id("drv-bob")
                        .name("Bob Willems")
                        .email("bob.willems@example.com")
                        .licenses(List.of("B"))
                        .workWindowStart("06:30")
                        .workWindowEnd("16:30")
                        .build(),
                DriverSummaryDto.builder()
                        .id("drv-lisa")
                        .name("Lisa Groen")
                        .email("lisa.groen@example.com")
                        .licenses(List.of("B"))
                        .workWindowStart("08:00")
                        .workWindowEnd("17:00")
                        .build()
        );
    }

    private List<TruckSummaryDto> initTrucks() {
        return List.of(
                TruckSummaryDto.builder().plateId("R-965-FK").warehouse("Amazon DUS2").capacity(120).status("available").build(),
                TruckSummaryDto.builder().plateId("K-381-LP").warehouse("Amazon DUS2").capacity(110).status("available").build(),
                TruckSummaryDto.builder().plateId("T-947-MJ").warehouse("Amazon DUS2").capacity(118).status("available").build(),
                TruckSummaryDto.builder().plateId("H-520-ZR").warehouse("Amazon DUS2").capacity(120).status("available").build(),
                TruckSummaryDto.builder().plateId("B-194-XN").warehouse("Amazon DUS2").capacity(115).status("available").build(),
                TruckSummaryDto.builder().plateId("V-803-GC").warehouse("Amazon DUS2").capacity(118).status("available").build(),
                TruckSummaryDto.builder().plateId("N-672-FD").warehouse("Amazon DUS2").capacity(112).status("available").build(),
                TruckSummaryDto.builder().plateId("P-415-HV").warehouse("Amazon DUS2").capacity(110).status("available").build(),
                TruckSummaryDto.builder().plateId("Z-209-KR").warehouse("Amazon DUS2").capacity(120).status("available").build()
        );
    }

    private List<Map<String, Object>> initParcels() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(parcel("P1234-001", "Amsterdam", "Kwadrantweg 2-12, 1042 AG Amsterdam", "Amazon DNL1"));
        list.add(parcel("P1234-002", "Rotterdam", "Maasboulevard 94, 3011 TX Rotterdam", "Amazon RTM1"));
        list.add(parcel("P1234-003", "Utrecht", "Catharijnesingel 52, 3511 GC Utrecht", "Amazon UTR1"));
        list.add(parcel("P1234-004", "Den Haag", "Spui 68, 2511 BT Den Haag", "Amazon DHG1"));
        list.add(parcel("P1234-005", "Leiden", "Breestraat 106, 2311 CS Leiden", "Amazon LDN1"));
        list.add(parcel("P1234-006", "Eindhoven", "Emmasingel 33, 5611 AZ Eindhoven", "Amazon EHV1"));
        list.add(parcel("P1234-007", "Rotterdam", "Blaak 28, 3011 TA Rotterdam", "Amazon RTM1"));
        list.add(parcel("P1234-008", "Tilburg", "Heuvelring 45, 5038 CJ Tilburg", "Amazon TLB1"));
        list.add(parcel("P1234-009", "Groningen", "Oude Boteringestraat 44, 9712 GD Groningen", "Amazon GRQ1"));
        list.add(parcel("P1234-010", "Breda", "Catharinastraat 2, 4811 XD Breda", "Amazon BRD1"));
        list.add(parcel("P1234-011", "Zwolle", "Diezerstraat 72, 8011 RX Zwolle", "Amazon ZWO1"));
        list.add(parcel("P1234-012", "Apeldoorn", "Hoofdstraat 152, 7311 BD Apeldoorn", "Amazon APL1"));
        list.add(parcel("P1234-013", "Amersfoort", "Langestraat 58, 3811 AJ Amersfoort", "Amazon AMF1"));
        list.add(parcel("P1234-014", "Haarlem", "Grote Houtstraat 99, 2011 SB Haarlem", "Amazon HLM1"));
        list.add(parcel("P1234-015", "Nijmegen", "Broerstraat 34, 6511 KP Nijmegen", "Amazon NJM1"));
        list.add(parcel("P1234-016", "Arnhem", "Roggestraat 29, 6811 BV Arnhem", "Amazon ARN1"));
        list.add(parcel("P1234-017", "Delft", "Markt 54, 2611 GP Delft", "Amazon DEL1"));
        list.add(parcel("P1234-018", "Hilversum", "Gijsbrecht van Amstelstraat 87, 1214 AV Hilversum", "Amazon HVS1"));
        list.add(parcel("P1234-019", "Enschede", "De Heurne 68, 7511 EK Enschede", "Amazon ENS1"));
        list.add(parcel("P1234-020", "Almere", "Haven 44, 1354 HT Almere", "Amazon ALM1"));
        list.add(parcel("P1234-021", "Zaandam", "Gedempte Gracht 86, 1506 CG Zaandam", "Amazon ZAA1"));
        list.add(parcel("P1234-022", "Leeuwarden", "Nieuwestad 98, 8911 CM Leeuwarden", "Amazon LWD1"));
        list.add(parcel("P1234-023", "Hoorn", "Grote Noord 75, 1621 KB Hoorn", "Amazon HRN1"));
        list.add(parcel("P1234-024", "Dordrecht", "Voorstraat 240, 3311 ET Dordrecht", "Amazon DRD1"));
        list.add(parcel("P1234-025", "Middelburg", "Lange Delft 13, 4331 AE Middelburg", "Amazon MDB1"));
        list.add(parcel("P1234-026", "Gouda", "Kleiweg 40, 2801 GJ Gouda", "Amazon GOU1"));
        list.add(parcel("P1234-027", "Helmond", "Veestraat 80, 5701 RF Helmond", "Amazon HLM2"));
        list.add(parcel("P1234-028", "Sittard", "Markt 19, 6131 EK Sittard", "Amazon SIT1"));
        list.add(parcel("P1234-029", "Venlo", "Vleesstraat 32, 5911 JE Venlo", "Amazon VNL1"));
        list.add(parcel("P1234-030", "Hengelo", "Burgemeester Jansenplein 3, 7551 ER Hengelo", "Amazon HGL1"));
        list.add(parcel("P1234-031", "Zoetermeer", "Promenade 48, 2711 AB Zoetermeer", "Amazon ZTM1"));
        list.add(parcel("P1234-032", "Vlissingen", "Walstraat 66, 4381 EP Vlissingen", "Amazon VLS1"));
        list.add(parcel("P1234-033", "Assen", "Kruisstraat 24, 9401 EE Assen", "Amazon ASN1"));
        list.add(parcel("P1234-034", "Alkmaar", "Langestraat 23, 1811 JA Alkmaar", "Amazon ALK1"));
        list.add(parcel("P1234-035", "Nieuwegein", "Passage 12, 3431 LB Nieuwegein", "Amazon NWG1"));
        list.add(parcel("P1234-036", "Emmen", "Hoofdstraat 45, 7811 ED Emmen", "Amazon EMM1"));
        list.add(parcel("P1234-037", "Oss", "Heuvelstraat 2, 5341 CW Oss", "Amazon OSS1"));
        list.add(parcel("P1234-038", "Heerlen", "Promenade 75, 6411 JJ Heerlen", "Amazon HRL1"));
        list.add(parcel("P1234-039", "Hardenberg", "Fortuinstraat 8, 7772 AX Hardenberg", "Amazon HDN1"));
        list.add(parcel("P1234-040", "Zeist", "Slotlaan 102, 3701 GS Zeist", "Amazon ZST1"));
        list.add(parcel("P1234-041", "Alphen aan den Rijn", "Van Mandersloostraat 37, 2406 CC Alphen", "Amazon ALP1"));
        list.add(parcel("P1234-042", "Barendrecht", "Middenbaan 85, 2991 CS Barendrecht", "Amazon BRD2"));
        list.add(parcel("P1234-043", "Capelle aan den IJssel", "De Koperwiek 32, 2903 AE Capelle", "Amazon CPI1"));
        list.add(parcel("P1234-044", "Houten", "Het Rond 135, 3995 DE Houten", "Amazon HTN1"));
        list.add(parcel("P1234-045", "Maastricht", "Grote Staat 19, 6211 CR Maastricht", "Amazon MST1"));
        list.add(parcel("P1234-046", "Amstelveen", "Stadshart 45, 1181 ZJ Amstelveen", "Amazon ASV1"));
        list.add(parcel("P1234-047", "Hoofddorp", "Raadhuisplein 9, 2132 TZ Hoofddorp", "Amazon HFD1"));
        list.add(parcel("P1234-048", "Sneek", "Wijde Noorderhorne 15, 8601 EB Sneek", "Amazon SNK1"));
        list.add(parcel("P1234-049", "Zutphen", "Beukerstraat 68, 7201 LD Zutphen", "Amazon ZTP1"));
        list.add(parcel("P1234-050", "Deventer", "Brink 45, 7411 BV Deventer", "Amazon DVT1"));
        return list;
    }

    private Map<String, Object> parcel(String id, String receiver, String address, String warehouse) {
        Map<String, Object> map = new HashMap<>();
        map.put("parcelId", id);
        map.put("receiver", receiver);
        map.put("deliveryLocation", address);
        map.put("warehouse", warehouse);
        map.put("status", "Pending");
        return map;
    }

    private List<PlannerRequestDto> initRequests() {
        LocalDate date = LocalDate.of(2025, 10, 10);
        return List.of(
                PlannerRequestDto.builder()
                        .requestId("REQ-0001")
                        .truckPlateId("R-965-FK")
                        .deliveryDate(date.atTime(17, 0))
                        .parcelCount(15)
                        .warehouse("Amazon DUS2")
                        .priority("High")
                        .parcelIds(List.of("P1234-001","P1234-002","P1234-003","P1234-004","P1234-005","P1234-006","P1234-007","P1234-008","P1234-009","P1234-010","P1234-011","P1234-012","P1234-013","P1234-014","P1234-015"))
                        .build(),
                PlannerRequestDto.builder()
                        .requestId("REQ-0002")
                        .truckPlateId("K-381-LP")
                        .deliveryDate(date.atTime(17, 0))
                        .parcelCount(20)
                        .warehouse("Amazon DUS2")
                        .priority("Low")
                        .parcelIds(List.of("P1234-016","P1234-017","P1234-018","P1234-019","P1234-020","P1234-021","P1234-022","P1234-023","P1234-024","P1234-025","P1234-026","P1234-027","P1234-028","P1234-029","P1234-030","P1234-031","P1234-032","P1234-033","P1234-034","P1234-035"))
                        .build(),
                PlannerRequestDto.builder()
                        .requestId("REQ-0003")
                        .truckPlateId("T-947-MJ")
                        .deliveryDate(date.atTime(17, 0))
                        .parcelCount(22)
                        .warehouse("Amazon DUS2")
                        .priority("Low")
                        .parcelIds(List.of("P1234-036","P1234-037","P1234-038","P1234-039","P1234-040","P1234-041","P1234-042","P1234-043","P1234-044","P1234-045","P1234-046","P1234-047","P1234-048","P1234-049","P1234-050","P1234-001","P1234-002","P1234-003","P1234-004","P1234-005","P1234-006","P1234-007"))
                        .build(),
                PlannerRequestDto.builder()
                        .requestId("REQ-0004")
                        .truckPlateId("H-520-ZR")
                        .deliveryDate(date.atTime(17, 0))
                        .parcelCount(22)
                        .warehouse("Amazon DUS2")
                        .priority("Medium")
                        .parcelIds(List.of("P1234-008","P1234-009","P1234-010","P1234-011","P1234-012","P1234-013","P1234-014","P1234-015","P1234-016","P1234-017","P1234-018","P1234-019","P1234-020","P1234-021","P1234-022","P1234-023","P1234-024","P1234-025","P1234-026","P1234-027","P1234-028","P1234-029"))
                        .build(),
                PlannerRequestDto.builder()
                        .requestId("REQ-0005")
                        .truckPlateId("B-194-XN")
                        .deliveryDate(date.atTime(17, 0))
                        .parcelCount(22)
                        .warehouse("Amazon DUS2")
                        .priority("Medium")
                        .parcelIds(List.of("P1234-030","P1234-031","P1234-032","P1234-033","P1234-034","P1234-035","P1234-036","P1234-037","P1234-038","P1234-039","P1234-040","P1234-041","P1234-042","P1234-043","P1234-044","P1234-045","P1234-046","P1234-047","P1234-048","P1234-049","P1234-050","P1234-001"))
                        .build(),
                PlannerRequestDto.builder()
                        .requestId("REQ-0006")
                        .truckPlateId("V-803-GC")
                        .deliveryDate(date.atTime(17, 0))
                        .parcelCount(22)
                        .warehouse("Amazon DUS2")
                        .priority("High")
                        .parcelIds(List.of("P1234-002","P1234-003","P1234-004","P1234-005","P1234-006","P1234-007","P1234-008","P1234-009","P1234-010","P1234-011","P1234-012","P1234-013","P1234-014","P1234-015","P1234-016","P1234-017","P1234-018","P1234-019","P1234-020","P1234-021","P1234-022","P1234-023"))
                        .build(),
                PlannerRequestDto.builder()
                        .requestId("REQ-0007")
                        .truckPlateId("N-672-FD")
                        .deliveryDate(date.atTime(17, 0))
                        .parcelCount(22)
                        .warehouse("Amazon DUS2")
                        .priority("Low")
                        .parcelIds(List.of("P1234-024","P1234-025","P1234-026","P1234-027","P1234-028","P1234-029","P1234-030","P1234-031","P1234-032","P1234-033","P1234-034","P1234-035","P1234-036","P1234-037","P1234-038","P1234-039","P1234-040","P1234-041","P1234-042","P1234-043","P1234-044","P1234-045"))
                        .build(),
                PlannerRequestDto.builder()
                        .requestId("REQ-0008")
                        .truckPlateId("P-415-HV")
                        .deliveryDate(date.atTime(17, 0))
                        .parcelCount(22)
                        .warehouse("Amazon DUS2")
                        .priority("Low")
                        .parcelIds(List.of("P1234-046","P1234-047","P1234-048","P1234-049","P1234-050","P1234-001","P1234-002","P1234-003","P1234-004","P1234-005","P1234-006","P1234-007","P1234-008","P1234-009","P1234-010","P1234-011","P1234-012","P1234-013","P1234-014","P1234-015","P1234-016","P1234-017"))
                        .build(),
                PlannerRequestDto.builder()
                        .requestId("REQ-0009")
                        .truckPlateId("Z-209-KR")
                        .deliveryDate(date.atTime(17, 0))
                        .parcelCount(22)
                        .warehouse("Amazon DUS2")
                        .priority("Medium")
                        .parcelIds(List.of("P1234-018","P1234-019","P1234-020","P1234-021","P1234-022","P1234-023","P1234-024","P1234-025","P1234-026","P1234-027","P1234-028","P1234-029","P1234-030","P1234-031","P1234-032","P1234-033","P1234-034","P1234-035","P1234-036","P1234-037","P1234-038","P1234-039"))
                        .build()
        );
    }

    private List<PlannerAssignmentDto> initAssignments() {
        LocalDateTime dateTime = LocalDate.of(2025, 10, 10).atTime(17, 0);
        return new ArrayList<>(List.of(
                PlannerAssignmentDto.builder()
                        .assignmentId("ASN-0001")
                        .truckPlateId("R-965-FK")
                        .date(dateTime)
                        .parcelCount(25)
                        .driverId("drv-tom")
                        .driverName("Tom Janssen")
                        .requestIds(List.of("REQ-0001"))
                        .status("SCHEDULED")
                        .build(),
                PlannerAssignmentDto.builder()
                        .assignmentId("ASN-0002")
                        .truckPlateId("K-381-LP")
                        .date(dateTime)
                        .parcelCount(25)
                        .driverId("drv-jack")
                        .driverName("Jack Visser")
                        .requestIds(List.of("REQ-0002"))
                        .status("SCHEDULED")
                        .build(),
                PlannerAssignmentDto.builder()
                        .assignmentId("ASN-0003")
                        .truckPlateId("T-947-MJ")
                        .date(dateTime)
                        .parcelCount(25)
                        .driverId("drv-frank")
                        .driverName("Frank Bakker")
                        .requestIds(List.of("REQ-0003"))
                        .status("IN_PROGRESS")
                        .build(),
                PlannerAssignmentDto.builder()
                        .assignmentId("ASN-0004")
                        .truckPlateId("H-520-ZR")
                        .date(dateTime)
                        .parcelCount(25)
                        .driverId("drv-bob")
                        .driverName("Bob Willems")
                        .requestIds(List.of("REQ-0004"))
                        .status("IN_PROGRESS")
                        .build(),
                PlannerAssignmentDto.builder()
                        .assignmentId("ASN-0005")
                        .truckPlateId("B-194-XN")
                        .date(dateTime)
                        .parcelCount(25)
                        .driverId(null)
                        .driverName(null)
                        .requestIds(List.of("REQ-0005"))
                        .status("PENDING")
                        .build(),
                PlannerAssignmentDto.builder()
                        .assignmentId("ASN-0006")
                        .truckPlateId("V-803-GC")
                        .date(dateTime)
                        .parcelCount(25)
                        .driverId(null)
                        .driverName(null)
                        .requestIds(List.of("REQ-0006"))
                        .status("PENDING")
                        .build()
        ));
    }

    private Map<String, PlannerRouteDto> initRoutes() {
        PlannerRouteDto route = PlannerRouteDto.builder()
                .assignmentId("ASN-0003")
                .truckPlateId("T-947-MJ")
                .driverName("Frank Bakker")
                .polyline("o`miH_yzp@e@x@]Zg@^_Ab@}At@}Az@{Aj@u@")
                .stops(List.of(
                        RouteStopDto.builder().order(1).parcelId("P1234-036").receiver("Emmen").address("Hoofdstraat 45, 7811 ED").latitude(52.785).longitude(6.897).eta("2025-10-10T08:30:00Z").build(),
                        RouteStopDto.builder().order(2).parcelId("P1234-037").receiver("Oss").address("Heuvelstraat 2, 5341 CW").latitude(51.765).longitude(5.520).eta("2025-10-10T09:15:00Z").build(),
                        RouteStopDto.builder().order(3).parcelId("P1234-038").receiver("Heerlen").address("Promenade 75, 6411 JJ").latitude(50.889).longitude(5.980).eta("2025-10-10T10:40:00Z").build()
                ))
                .build();
        return Map.of("ASN-0003", route);
    }

    public Map<String, Object> summarizeStatus() {
        long todaysRequests = requests.size();
        long processing = assignments.stream()
                .filter(a -> "IN_PROGRESS".equalsIgnoreCase(a.getStatus()))
                .count();
        long exceptions = assignments.stream()
                .filter(a -> "EXCEPTION".equalsIgnoreCase(a.getStatus()))
                .count();

        Map<String, Object> summary = new HashMap<>();
        summary.put("todaysRequests", todaysRequests);
        summary.put("availableDrivers", drivers.size());
        summary.put("processing", processing);
        summary.put("exceptions", exceptions);
        return summary;
    }
}

