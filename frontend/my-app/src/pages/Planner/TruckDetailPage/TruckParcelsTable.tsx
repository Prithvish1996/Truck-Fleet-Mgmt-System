import React from 'react';
import { TruckParcel } from '../../../types';
import DriverNameCell from '../../../components/common/DriverNameCell';
import { DriverResponse } from '../../../services/plannerService';

interface TruckParcelsTableProps {
  parcels: TruckParcel[];
  currentPage: number;
  itemsPerPage: number;
  drivers: DriverResponse[];
  onParcelClick?: (parcelId: string) => void;
}

export default function TruckParcelsTable({
  parcels,
  currentPage,
  itemsPerPage,
  drivers,
  onParcelClick
}: TruckParcelsTableProps) {
  return (
    <div className="truck-detail-table-container">
      <table className="truck-detail-table">
        <thead>
          <tr>
            <th>No.</th>
            <th>Parcel ID</th>
            <th>Customer</th>
            <th>Delivery Location</th>
            <th>Driver</th>
          </tr>
        </thead>
        <tbody>
          {parcels.map((parcel, index) => {
            const rowNumber = (currentPage - 1) * itemsPerPage + index + 1;
            return (
              <tr key={parcel.id}>
                <td>{rowNumber}</td>
                <td>
                  {onParcelClick ? (
                    <button
                      className="parcel-id-link"
                      onClick={() => onParcelClick(parcel.parcelId)}
                    >
                      {parcel.parcelId}
                    </button>
                  ) : (
                    parcel.parcelId
                  )}
                </td>
                <td>{parcel.customer}</td>
                <td>{parcel.deliveryLocation}</td>
                <td>
                  <DriverNameCell driverId={parcel.driverId} drivers={drivers} />
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}

