import React, { useState } from 'react';

interface DashboardRequest {
  truckPlateId: string;
  deliveryDate: string;
  parcels: number;
  warehouse: string;
  parcelIds: number[];
  warehouseId: number;
}

interface NewRequestsPanelProps {
  requests: DashboardRequest[];
  onGenerateRouteClick: () => void;
  isGenerating?: boolean;
}

export default function NewRequestsPanel({ requests, onGenerateRouteClick, isGenerating = false }: NewRequestsPanelProps) {
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 5;
  const totalPages = Math.ceil(requests.length / itemsPerPage);
  
  const getPageNumbers = (): (number | string)[] => {
    const maxButtons = 5;
    
    if (totalPages <= maxButtons) {
      return Array.from({ length: totalPages }, (_, i) => i + 1);
    }

    const pages: (number | string)[] = [];
    
    if (currentPage <= 3) {
      for (let i = 1; i <= maxButtons; i++) {
        pages.push(i);
      }
      if (totalPages > maxButtons) {
        pages.push('...');
        pages.push(totalPages);
      }
    } else if (currentPage >= totalPages - 2) {
      pages.push(1);
      pages.push('...');
      for (let i = totalPages - maxButtons + 1; i <= totalPages; i++) {
        pages.push(i);
      }
    } else {
      pages.push(1);
      pages.push('...');
      pages.push(currentPage - 1);
      pages.push(currentPage);
      pages.push(currentPage + 1);
      pages.push('...');
      pages.push(totalPages);
    }
    
    return pages;
  };

  const pageNumbers = getPageNumbers();
  const startIndex = (currentPage - 1) * itemsPerPage;
  const endIndex = startIndex + itemsPerPage;
  const currentPageRequests = requests.slice(startIndex, endIndex);

  const handlePageChange = (page: number) => {
    if (page >= 1 && page <= totalPages) {
      setCurrentPage(page);
    }
  };

  const isEmpty = requests.length === 0;

  return (
    <div className="panel new-requests">
      <div className="panel-header">
        <h2>New Requests</h2>
        {!isEmpty && <span className="panel-badge">{requests.length}</span>}
      </div>
      
      {isEmpty ? (
        <div className="panel-empty-state">
          <div className="empty-state-icon">📦</div>
          <h3 className="empty-state-title">No New Requests</h3>
          <p className="empty-state-description">
            Scheduled parcels will appear here. To get started:
          </p>
          <ol className="empty-state-steps">
            <li>Go to <strong>Schedule</strong> in the sidebar</li>
            <li>Select parcels from a warehouse</li>
            <li>Click <strong>"Schedule Parcels"</strong></li>
            <li>They will appear here as new requests</li>
          </ol>
        </div>
      ) : (
        <>
          <div className="table-wrapper">
            <table className="new-requests-table">
              <thead>
                <tr>
                  <th>Truck Plate ID</th>
                  <th>Delivery Date</th>
                  <th>No. of Parcels</th>
                  <th>Warehouse</th>
                </tr>
              </thead>
              <tbody>
                {currentPageRequests.map((request, index) => (
                  <tr key={`${request.truckPlateId}-${request.deliveryDate}-${index}`}>
                    <td>{request.truckPlateId}</td>
                    <td>{request.deliveryDate}</td>
                    <td>{request.parcels}</td>
                    <td>{request.warehouse}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="panel-footer">
            <div className="pagination">
              <button 
                type="button" 
                className="pagination-btn" 
                aria-label="Previous page"
                onClick={() => handlePageChange(currentPage - 1)}
                disabled={currentPage === 1}
              >
                ‹
              </button>
              {pageNumbers.map((page, index) => {
                if (page === '...') {
                  return (
                    <span key={`ellipsis-${index}`} className="pagination-ellipsis">
                      ...
                    </span>
                  );
                }
                
                const pageNum = page as number;
                return (
                  <button
                    key={pageNum}
                    type="button"
                    className={`pagination-btn ${currentPage === pageNum ? 'active' : ''}`}
                    onClick={() => handlePageChange(pageNum)}
                  >
                    {pageNum}
                  </button>
                );
              })}
              <button 
                type="button" 
                className="pagination-btn" 
                aria-label="Next page"
                onClick={() => handlePageChange(currentPage + 1)}
                disabled={currentPage === totalPages}
              >
                ›
              </button>
            </div>
            <button
              className="primary-action"
              type="button"
              onClick={onGenerateRouteClick}
              disabled={isGenerating || requests.length === 0}
            >
              {isGenerating ? 'Generating...' : 'Generate Route'}
            </button>
          </div>
        </>
      )}
    </div>
  );
}

