# TFMS Planner Service

## Overview
The Planner Service is a Spring Boot module designed to manage planner activities such as parcel operations like creation, retrieval by ID, and retrieval by warehouse. The implementation follows the **Single Responsibility Principle (SRP)**, ensuring that each component and handler has a clear, focused purpose.

## Key Design Principles
- **Handlers:** Each operation (create, get by ID, get all parcels) is encapsulated in a dedicated handler class. This adheres to SRP by keeping logic for each operation isolated.
- **DTO Mapping:** `ParcelMapperHandler` converts database entities (`ParcelDao`) into DTOs (`ParcelResponseDto`) for API responses, ensuring separation between persistence and API layers.
- **Validation:** Inputs such as `parcelId` and `warehouseId` are validated to prevent invalid operations. Errors are returned in a standardized `ApiResponse` format.
- **MC/DC Testing:** Unit tests were written following the Modified Condition/Decision Coverage (MC/DC) approach, ensuring all logical conditions in the handlers are independently tested.

## Parcel Handlers and Responsibilities

| Handler | Responsibility | Key Validations |
|---------|----------------|----------------|
| `CreateParcelHandler` | Creates a new parcel in the system | - Parcel name must be unique<br>- Warehouse ID must be valid |
| `GetParcelByIdHandler` | Retrieves a parcel by its ID | - `parcelId` must be valid<br>- Parcel must exist |
| `GetAllParcelsHandler` | Retrieves all parcels for a given warehouse | - `warehouseId` must be valid<br>- Warehouse must exist |

## Parcel API Endpoints

| Method | Endpoint | Description |
|--------|---------|-------------|
| POST   | `/api/parcel/create` | Create a new parcel |
| GET    | `/api/parcel/{parcelId}` | Get a parcel by ID |
| GET    | `/api/parcel/all/{warehouseId}` | Get all parcels for a warehouse |

## Parcel Unit Tests (MC/DC)

The following unit tests were implemented to achieve 100% condition coverage for the handlers:

### 1. `CreateParcelHandlerTest`
**Conditions tested:**
- Parcel name exists → return error
- Warehouse ID exists → success
- Warehouse ID does not exist → throw error
- All fields properly mapped → success

**Expected outcomes:** Error messages or successful DTO mapping.

### 2. `GetParcelByIdHandlerTest`
**Conditions tested:**
- `parcelId` is null → return error
- `parcelId` ≤ 0 → return error
- Parcel not found → return error
- Parcel exists → success

**Expected outcomes:** Valid `ParcelResponseDto` or error message.

### 3. `GetAllParcelsHandlerTest`
**Conditions tested:**
- `warehouseId` is null → return error
- `warehouseId` ≤ 0 → return error
- No parcels exist for warehouse → return empty list
- Parcels exist → return list of `ParcelResponseDto`

**Expected outcomes:** List of DTOs or error message.

### 4. `ParcelMapperHandlerTest`
**Conditions tested:**
- All fields in `ParcelDao` are correctly mapped to `ParcelResponseDto`
- Null safety for warehouse field (optional enhancement)

**Expected outcomes:** Fully mapped DTO with no exceptions.
