# 🧭 TFMS Parcel Service

## 📘 Overview
The **Parcel Service** is a Spring Boot module responsible for managing parcel operations, including creation, retrieval, update, deletion, and scheduling.  
It adheres strictly to the **Single Responsibility Principle (SRP)** — each handler performs exactly one well-defined operation, ensuring high maintainability and testability.

---

## ⚙️ Key Design Principles
| Principle | Description |
|------------|--------------|
| **Handlers** | Each operation (create, update, delete, retrieve, schedule) is encapsulated in its own class. |
| **SRP Compliance** | Each handler focuses on a single task, avoiding cross-responsibility logic. |
| **DTO Mapping** | `ParcelMapperHandler` maps `ParcelDao` entities to `ParcelResponseDto` objects for clean API responses. |
| **Validation** | Input validation ensures IDs, names, and warehouse references are valid before any operation. |
| **Standardized Response** | All operations return a unified `ApiResponse<T>` object. |
| **MC/DC Testing** | Unit tests follow the **Modified Condition/Decision Coverage** approach, ensuring all logical branches are independently tested. |

---

## 🧩 Handlers and Responsibilities

| Handler | Responsibility | Key Validations |
|----------|----------------|-----------------|
| **CreateParcelHandler** | Creates a new parcel | Parcel name must be unique; Warehouse ID must exist |
| **GetParcelByIdHandler** | Retrieves a parcel by its ID | `parcelId` must be valid and parcel must exist |
| **GetAllParcelsHandler** | Retrieves all parcels for a warehouse | `warehouseId` must be valid and warehouse must exist |
| **UpdateParcelHandler** | Updates parcel information | `parcelId` must be valid; Parcel must exist; Warehouse must exist; New name must be unique |
| **DeleteParcelHandler** | Permanently deletes a parcel | `parcelId` must be valid and parcel must exist |
| **GetNextDayParcelScheduleHandler** | Retrieves parcels scheduled for next day grouped by warehouse | Only includes parcels with `status = pending`; ignores null warehouses |
| **ParcelCallbackController** | Accepts a list of parcels from an external API and delegates creation to `CreateParcelHandler` | Each parcel validated before creation |

---

## 🛠️ API Endpoints

| Method | Endpoint | Description |
|--------|-----------|-------------|
| `POST` | `/api/parcel/create` | Create a new parcel |
| `GET` | `/api/parcel/{parcelId}` | Get a parcel by ID |
| `GET` | `/api/parcel/all/{warehouseId}` | Get all parcels for a warehouse |
| `PUT` | `/api/parcel/{parcelId}` | Update a parcel |
| `DELETE` | `/api/parcel/{parcelId}` | Delete a parcel permanently |
| `POST` | `/api/parcel/callback` | Callback endpoint for bulk parcel creation (external APIs) |
| `GET` | `/api/parcel/schedule/nextday` | Get next-day pending parcels grouped by warehouse |

---

## 🧪 Unit Tests (MC/DC Coverage)

### 1️⃣ CreateParcelHandlerTest
**Conditions Tested**
- Parcel name already exists → returns error
- Warehouse ID invalid → throws error
- Warehouse valid → success
- All fields mapped correctly → success

**Expected Outcomes**
- Error for duplicates
- Success on valid creation

---

### 2️⃣ GetParcelByIdHandlerTest
**Conditions Tested**
- `parcelId == null` → returns error
- `parcelId <= 0` → returns error
- Parcel not found → returns error
- Parcel found → success

**Expected Outcomes**
- Error message or valid `ParcelResponseDto`

---

### 3️⃣ GetAllParcelsHandlerTest
**Conditions Tested**
- `warehouseId == null` → returns error
- `warehouseId <= 0` → returns error
- Warehouse not found → returns error
- Parcels found → success

**Expected Outcomes**
- Empty list or list of `ParcelResponseDto`

---

### 4️⃣ UpdateParcelHandlerTest
**Conditions Tested**
- `parcelId == null` or `<= 0` → invalid
- Parcel not found → error
- Warehouse not found → error
- New name already exists → error
- Valid update → success

**Expected Outcomes**
- Either error message or successfully updated `ParcelResponseDto`

---

### 5️⃣ DeleteParcelHandlerTest
**Conditions Tested**
- `parcelId == null` or `<= 0` → invalid
- Parcel not found → error
- Valid parcel → success

**Expected Outcomes**
- Error message for invalid IDs
- Success message `"Parcel deleted successfully"`

---

### 6️⃣ GetNextDayParcelScheduleHandlerTest
**Conditions Tested**
- No parcels exist → error
- No pending parcels → error
- Pending parcels with null warehouse → skipped
- Valid pending parcels with warehouse → grouped correctly

**Expected Outcomes**
- Returns grouped list: `{ "1 - Main Warehouse": [ParcelResponseDto], "2 - Secondary Warehouse": [...] }`
- Achieves 100% **MC/DC coverage**

---

### 7️⃣ ParcelCallbackControllerTest
**Conditions Tested**
- Empty parcel list → no calls to `CreateParcelHandler`
- All parcels valid → each parcel created successfully
- Partial failure → handled gracefully

**Expected Outcomes**
- Returns a list of successfully created parcels
- Proper error responses for invalid entries

---