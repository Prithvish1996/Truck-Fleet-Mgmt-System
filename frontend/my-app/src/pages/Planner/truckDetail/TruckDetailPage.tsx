import React, { useState, useEffect } from 'react';
import { TruckParcel } from '../../../types/index';
import './TruckDetailPage.css';

interface TruckDetailPageProps {
  truckPlateNo: string;
  onReturn: () => void;
  onParcelClick?: (parcelId: string) => void;
}

// Mock function to generate parcels for a truck
const generateMockParcels = (truckPlateNo: string, count: number = 11): TruckParcel[] => {
  const customers = [
    'George', 'Sam', 'James Carter', 'Emily Johnson', 'Michael Brown',
    'Sarah Miller', 'Daniel Smith', 'Jessica Taylor', 'William Davis',
    'Olivia Wilson', 'Benjamin Harris', 'Sophia Martinez', 'Ethan Anderson',
    'Isabella Thompson', 'Alexander Garcia', 'Mia Rodriguez', 'Noah Lee'
  ];

  const locations = [
    'Kwadrantweg 2-12, 1042 AG Amsterdam',
    'Noordeinde 68, 2514 GL Den Haag',
    'Hoofdstraat 45, 2312 KL Leiden',
    'Kerkstraat 12, 2131 AB Hoofddorp',
    'Stationsplein 5, 2264 AB Leidschendam',
    'Voorburgstraat 20, 2271 CC Voorburg',
    'Prinsestraat 8, 2513 CJ Den Haag',
    'Damrak 1, 1012 LG Amsterdam',
    'Rokin 15, 1012 KK Amsterdam',
    'Kalverstraat 100, 1012 PG Amsterdam',
    'Nieuwendijk 150, 1012 ML Amsterdam',
    'Damstraat 20, 1012 HK Amsterdam'
  ];

  const parcelPrefixes = ['P1223', 'P1224', 'P1225', 'P1226'];
  const parcels: TruckParcel[] = [];

  for (let i = 0; i < count; i++) {
    const prefix = parcelPrefixes[i % parcelPrefixes.length];
    const num = String(Math.floor(i / parcelPrefixes.length) * parcelPrefixes.length + (i % parcelPrefixes.length) + 1).padStart(2, '0');
    
    parcels.push({
      id: `parcel-${i + 1}`,
      parcelId: `${prefix}-${num}`,
      customer: customers[i % customers.length],
      deliveryLocation: locations[i % locations.length],
      driverId: 'tom' // All parcels assigned to Tom as shown in the image
    });
  }

  return parcels;
};

export default function TruckDetailPage({ truckPlateNo, onReturn, onParcelClick }: TruckDetailPageProps) {
  const [parcels, setParcels] = useState<TruckParcel[]>([]);
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 12;

  useEffect(() => {
    // Generate mock parcels for the truck
    const mockParcels = generateMockParcels(truckPlateNo, 11); // 11 parcels as shown in image
    setParcels(mockParcels);
  }, [truckPlateNo]);

  const totalPages = Math.ceil(parcels.length / itemsPerPage);

  const handlePageChange = (page: number) => {
    if (page >= 1 && page <= totalPages) {
      setCurrentPage(page);
    }
  };

  const getCurrentPageParcels = () => {
    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    return parcels.slice(startIndex, endIndex);
  };

  const getDriverName = (driverId: string | null): string => {
    if (!driverId) return 'Unassigned';
    const driverMap: { [key: string]: string } = {
      'tom': 'Tom',
      'jack': 'Jack',
      'frank': 'Frank',
      'bob': 'Bob'
    };
    return driverMap[driverId] || 'Tom';
  };

  return (
    <div className="truck-detail-page">
      <div className="truck-detail-container">
        <h2 className="truck-detail-title">{truckPlateNo} Truck</h2>
        
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
              {getCurrentPageParcels().map((parcel, index) => {
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
                    <td>{getDriverName(parcel.driverId)}</td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        <div className="pagination">
          <button
            className="pagination-button"
            onClick={() => handlePageChange(currentPage - 1)}
            disabled={currentPage === 1}
          >
            Previous page
          </button>
                    <div className="pagination-numbers">
            {Array.from({ length: Math.min(7, totalPages) }, (_, i) => {     
              let pageNum: number;
              if (totalPages <= 7) {
                pageNum = i + 1;
              } else if (currentPage <= 4) {
                pageNum = i + 1;
              } else if (currentPage >= totalPages - 3) {
                pageNum = totalPages - 6 + i;
              } else {
                pageNum = currentPage - 3 + i;
              }
              
              return (
                <button
                  key={pageNum}
                  className={`pagination-number ${currentPage === pageNum ? 'active' : ''}`}
                  onClick={() => handlePageChange(pageNum)}
                >
                  {pageNum}
                </button>
              );
            })}
            {totalPages > 7 && <span className="pagination-ellipsis">...</span>}
          </div>
          <button
            className="pagination-button"
            onClick={() => handlePageChange(currentPage + 1)}
            disabled={currentPage === totalPages}
          >
            Next page
          </button>
        </div>

        {/* Return Button */}
        <div className="return-button-container">
          <button className="return-button" onClick={onReturn}>
            Return
          </button>
        </div>
      </div>
    </div>
  );
}
