import React, { useState, useEffect } from 'react';
import { ParcelDetail } from '../../../types/index';
import './ParcelDetailPage.css';

interface ParcelDetailPageProps {
  parcelId: string;
  onReturn: () => void;
}

// Mock function to generate parcel detail
const generateParcelDetail = (parcelId: string): ParcelDetail => {
  // Generate mock data based on parcel ID
  const mockDetails: { [key: string]: ParcelDetail } = {
    'P1223-01': {
      parcelId: 'P1223-01',
      internalId: 'P-001',
      contactPerson: 'Kevin Wang',
      phone: '0617331229',
      email: 'kevinwang22@gmail.com',
      streetName: 'Liguster',
      houseNumber: '202',
      zipCode: '2262 AC',
      city: 'Leidschendam',
      country: 'Netherlands',
      typesOfItems: 'Fragile goods',
      specialInstructions: 'Special Instructions / Delivery Notes (e.g. doorbell, preferred delivery time, unloading method, etc.)',
      remarks: 'Call to notify delivery upon arrival'
    }
  };

  // If parcel ID exists in mock data, return it, otherwise generate a default
  if (mockDetails[parcelId]) {
    return mockDetails[parcelId];
  }

  // Generate default data for other parcels
  return {
    parcelId: parcelId,
    internalId: `P-${parcelId.split('-')[1]}`,
    contactPerson: 'Customer Name',
    phone: '0612345678',
    email: 'customer@example.com',
    streetName: 'Street Name',
    houseNumber: '123',
    zipCode: '1234 AB',
    city: 'City',
    country: 'Netherlands',
    typesOfItems: 'General goods',
    specialInstructions: 'Special Instructions / Delivery Notes (e.g. doorbell, preferred delivery time, unloading method, etc.)',
    remarks: 'Please handle with care'
  };
};

export default function ParcelDetailPage({ parcelId, onReturn }: ParcelDetailPageProps) {
  const [parcelDetail, setParcelDetail] = useState<ParcelDetail | null>(null);

  useEffect(() => {
    const detail = generateParcelDetail(parcelId);
    setParcelDetail(detail);
  }, [parcelId]);

  if (!parcelDetail) {
    return (
      <div className="parcel-detail-page">
        <div className="parcel-detail-container">
          <div>Loading...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="parcel-detail-page">
      <div className="parcel-detail-container">
        <h2 className="parcel-detail-title">{parcelId} Information</h2>
        
        <div className="parcel-detail-info">
          <div className="info-row">
            <div className="info-label">Parcel</div>
            <div className="info-value">{parcelDetail.internalId}</div>
          </div>
          
          <div className="info-row">
            <div className="info-label">Contact Person</div>
            <div className="info-value">{parcelDetail.contactPerson}</div>
          </div>
          
          <div className="info-row">
            <div className="info-label">Phone / Mobile Number</div>
            <div className="info-value">{parcelDetail.phone}</div>
          </div>
          
          <div className="info-row">
            <div className="info-label">Email Address</div>
            <div className="info-value">{parcelDetail.email}</div>
          </div>
          
          <div className="info-row">
            <div className="info-label">Street Name</div>
            <div className="info-value">{parcelDetail.streetName}</div>
          </div>
          
          <div className="info-row">
            <div className="info-label">House Number</div>
            <div className="info-value">{parcelDetail.houseNumber}</div>
          </div>
          
          <div className="info-row">
            <div className="info-label">ZIP Code</div>
            <div className="info-value">{parcelDetail.zipCode}</div>
          </div>
          
          <div className="info-row">
            <div className="info-label">City</div>
            <div className="info-value">{parcelDetail.city}</div>
          </div>
          
          <div className="info-row">
            <div className="info-label">Country</div>
            <div className="info-value">{parcelDetail.country}</div>
          </div>
          
          <div className="info-row">
            <div className="info-label">Types of items</div>
            <div className="info-value">{parcelDetail.typesOfItems}</div>
          </div>
          
          <div className="info-row">
            <div className="info-label">Special Instructions</div>
            <div className="info-value info-value-italic">{parcelDetail.specialInstructions}</div>
          </div>
          
          <div className="info-row">
            <div className="info-label">Remarks</div>
            <div className="info-value info-value-italic">{parcelDetail.remarks}</div>
          </div>
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
