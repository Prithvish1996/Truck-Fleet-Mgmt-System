import React, { useEffect, useState } from "react";
import { plannerParcelService } from "../../../services/plannerParcelService";
import "../PlannerDashboard.css";
import { PlannerParcel } from "../../../services/plannerParcelService";

const ParcelsSection = () => {
  const [warehouseId, setWarehouseId] = useState(1);
  const [parcels, setParcels] = useState<PlannerParcel[]>([]);
  const [pageInfo, setPageInfo] = useState({
  page: 0,
  totalPages: 1
});

  const loadParcels = async () => {
    const result = await plannerParcelService.getWarehouseParcels({
      warehouseId,
      page: pageInfo.page,
      size: 10,
    });
    setParcels(result.data);
    setPageInfo({
      page: result.currentPage,
      totalPages: result.totalPages,
    });
  };

  useEffect(() => {
    loadParcels();
  }, [warehouseId, pageInfo.page]);

  return (
    <div className="eco-card">
      <h2>Warehouse Parcels</h2>
      <p className="subtext">View parcels inside any warehouse</p>

      <label>
        Warehouse ID:
        <input
          type="number"
          value={warehouseId}
          onChange={(e) => setWarehouseId(Number(e.target.value))}
          style={{ marginLeft: "10px" }}
        />
      </label>

      <table className="eco-table">
        <thead>
          <tr>
            <th>ID</th><th>Name</th><th>Status</th>
            <th>Weight</th><th>Volume</th>
            <th>City</th><th>Address</th><th>Recipient</th>
          </tr>
        </thead>
        <tbody>
          {parcels.map((p) => (
            <tr key={p.parcelId}>
              <td>{p.parcelId}</td>
              <td>{p.name}</td>
              <td>{p.status}</td>
              <td>{p.weight}</td>
              <td>{p.volume}</td>
              <td>{p.deliveryCity}</td>
              <td>{p.deliveryAddress}</td>
              <td>{p.recipientName}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <div style={{ marginTop: "10px" }}>
        <button
          className="eco-button"
          disabled={pageInfo.page === 0}
          onClick={() => setPageInfo({ ...pageInfo, page: pageInfo.page - 1 })}
        >
          Prev
        </button>

        <button
          className="eco-button"
          disabled={pageInfo.page + 1 >= pageInfo.totalPages}
          onClick={() => setPageInfo({ ...pageInfo, page: pageInfo.page + 1 })}
          style={{ marginLeft: "8px" }}
        >
          Next
        </button>
      </div>
    </div>
  );
};

export default ParcelsSection;
