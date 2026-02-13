# TFMS Parcel Service

## 📦 Overview
The **Parcel Service** is a Spring Boot module service responsible for managing parcel operations such as **creation**, **retrieval**, **update**, **deletion**, and **next-day scheduling**.  

The system is designed with **Single Responsibility Principle (SRP)** in mind — each handler performs a single, well-defined task to ensure modularity and maintainability.

---

## 🧩 Key Design Principles

| Principle | Description |
|------------|-------------|
| **Handlers** | Each operation (Create, Get, Update, Delete, Schedule) is encapsulated in its own handler class implementing an interface. |
| **SRP (Single Responsibility Principle)** | Each handler focuses on one business operation to reduce complexity. |
| **DTO Mapping** | The `ParcelMapperHandler` maps between `ParcelDao` entities and `ParcelResponseDto` objects, isolating persistence from API representation. |
| **Validation** | Input parameters such as `parcelId`, `warehouseId`, and request DTOs are validated with appropriate error responses using `ApiResponse`. |
| **MC/DC Testing** | Unit tests follow **Modified Condition/Decision Coverage (MC/DC)** principles to ensure full branch and condition coverage. |
| **Pagination & Filtering** | The `GetAllParcelsHandler` supports pagination and text filtering for scalability and usability. |

---

## ⚙️ Handlers and Responsibilities

| Handler | Responsibility | Key Validations |
|----------|----------------|----------------|
| **CreateParcelHandler** | Creates a new parcel record. | - Parcel name must be unique<br>- Warehouse ID must exist |
| **UpdateParcelHandler** | Updates an existing parcel. | - Parcel ID must be valid<br>- Parcel must exist<br>- Parcel name must be unique<br>- Warehouse ID must exist |
| **DeleteParcelHandler** | Deletes a parcel permanently (hard delete). | - Parcel ID must be valid<br>- Parcel must exist |
| **GetParcelByIdHandler** | Retrieves a parcel by ID. | - Parcel ID must be valid<br>- Parcel must exist |
| **GetAllParcelsHandler** | Retrieves parcels by warehouse with pagination and filtering. | - Warehouse ID is mandatory<br>- Optional filtering by parcel name, city, recipient name, or recipient phone |
| **GetNextDayParcelScheduleHandler** | Fetches next-day pending parcels grouped by warehouse. | - Only "pending" parcels included<br>- Grouped by warehouse name and ID<br>- Returns error if no pending parcels |

---

## API Endpoints

| Method | Endpoint | Description |
|---------|-----------|-------------|
| `POST` | `/api/parcel/create` | Create a new parcel |
| `PUT` | `/api/parcel/update/{parcelId}` | Update an existing parcel |
| `DELETE` | `/api/parcel/{parcelId}` | Delete a parcel by ID |
| `GET` | `/api/parcel/{parcelId}` | Get a parcel by ID |
| `GET` | `/api/parcel/all` | Get all parcels by warehouse with pagination & filtering |
| `GET` | `/api/parcel/schedule/next-day` | Get next-day pending parcels grouped by warehouse |
| `POST` | `/api/parcel/callback` | Bulk create parcels via external callback |

---

## 🧪 Unit Tests (MC/DC)

All tests are written using **JUnit 5** and **Mockito**, ensuring complete condition and decision coverage.

### 1. **CreateParcelHandlerTest**
**Conditions tested:**
- Parcel name already exists → returns error
- Warehouse ID invalid → returns error
- Warehouse ID valid → success
- All fields properly mapped → success

*Expected outcome:* Error messages or successful `ParcelResponseDto`.

---

### 2. **UpdateParcelHandlerTest**
**Conditions tested:**
- Parcel ID null or ≤ 0 → return error
- Parcel not found → return error
- Parcel name conflict → return error
- Warehouse not found → return error
- Valid update → return updated DTO

*Expected outcome:* Updated parcel or descriptive error message.

---

### 3. **DeleteParcelHandlerTest**
**Conditions tested:**
- Parcel ID null or ≤ 0 → return error
- Parcel not found → return error
- Valid parcel ID → delete successful

*Expected outcome:* `"Parcel deleted successfully"` confirmation.

---

### 4. **GetParcelByIdHandlerTest**
**Conditions tested:**
- Parcel ID null or ≤ 0 → return error
- Parcel not found → return error
- Parcel exists → return success

*Expected outcome:* Returns `ParcelResponseDto` or error.

---

### 5. **GetAllParcelsHandlerTest**
**Conditions tested:**
- Warehouse ID null or ≤ 0 → return error
- No parcels for warehouse → return empty result
- Filter by name, city, recipient name, recipient phone → success
- Pagination limits respected

*Expected outcome:* Paginated list of `ParcelResponseDto`.

---

### 6. **GetNextDayParcelScheduleHandlerTest**
**Conditions tested:**
- No parcels → return error
- No pending parcels → return error
- Pending parcels with null warehouse skipped
- Valid pending parcels grouped by warehouse ID and name

*Expected outcome:* Grouped map of pending parcels.

---

### 7. **ParcelMapperHandlerTest**
**Conditions tested:**
- All `ParcelDao` fields correctly mapped to `ParcelResponseDto`
- Optional warehouse name handled gracefully

*Expected outcome:* Fully mapped DTO with correct values.

---

## 🔍 Filtering and Pagination (in `GetAllParcelsHandler`)
### Parameters:
- `warehouseId` *(required)* — Warehouse to filter parcels by
- `searchText` *(optional)* — Filters where:
    - Parcel name **starts with** text
    - City **starts with** text
    - Recipient name or phone **starts with** text
- `page` *(optional, default=0)* — Page number
- `size` *(optional, default=10)* — Number of records per page

### Example API Call:
GET /api/parcel/getAll?warehouseid=1&searchText=box&page=0&size=10

Returns paginated & filtered parcel list for warehouse **1**.

---

## Design Summary
- **SRP** ensures every handler class is focused and testable.
- **MC/DC** testing ensures logic coverage across all conditional branches.
- **Pagination & Filtering** make large datasets manageable.
- **Clear DTO boundaries** prevent entity exposure in the API layer.
- **Error handling** provides consistent responses with messages in `ApiResponse`.

---
