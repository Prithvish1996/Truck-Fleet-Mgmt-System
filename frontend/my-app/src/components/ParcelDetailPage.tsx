import React, { useState, useEffect } from 'react';
import { ParcelDetail } from '../types';
import { plannerService } from '../services/plannerService';
import { extractParcelId } from '../utils/dataTransformers';
import './ParcelDetailPage.css';

interface ParcelDetailPageProps {
  parcelId: string;
  onReturn: () => void;
}

export default function ParcelDetailPage({ parcelId, onReturn }: ParcelDetailPageProps) {
  const [parcelDetail, setParcelDetail] = useState<ParcelDetail | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    loadParcelDetail();
  }, [parcelId]);

  const loadParcelDetail = async () => {
    setLoading(true);
    setError('');
    try {
      // Extract numeric parcel ID from string format (e.g., "P1223-01" -> 1223)
      const parcelIdNum = extractParcelId(parcelId);
      
      if (!parcelIdNum || parcelIdNum === 0) {
        setError('Invalid parcel ID format.');
        return;
      }

      const parcel = await plannerService.getParcelById(parcelIdNum);
      
      // Parse address components
      const addressParts = parcel.deliveryAddress?.split(/\s+/) || [];
      let streetName = '';
      let houseNumber = '';
      
      // Try to extract house number (usually a number at the end of the address)
      const lastPart = addressParts[addressParts.length - 1];
      if (/^\d+/.test(lastPart)) {
        houseNumber = lastPart;
        streetName = addressParts.slice(0, -1).join(' ');
      } else {
        streetName = parcel.deliveryAddress || '';
        houseNumber = '';
      }

      const detail: ParcelDetail = {
        parcelId: parcelId,
        internalId: `P-${parcel.parcelId}`,
        contactPerson: parcel.recipientName || 'Unknown',
        phone: parcel.recipientPhone || 'N/A',
        email: '', // Backend doesn't provide email in ParcelResponse
        streetName: streetName,
        houseNumber: houseNumber,
        zipCode: parcel.deliveryPostalCode || 'N/A',
        city: parcel.deliveryCity || 'Unknown',
        country: 'Netherlands',
        typesOfItems: parcel.weight ? `Weight: ${parcel.weight}kg, Volume: ${parcel.volume || 'N/A'}` : 'N/A',
        specialInstructions: parcel.deliveryInstructions || 'No special instructions',
        remarks: `Status: ${parcel.status || 'Unknown'}`
      };

      setParcelDetail(detail);
    } catch (err: any) {
      console.error('Error loading parcel detail:', err);
      setError(err.message || 'Failed to load parcel details.');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="parcel-detail-page">
        <div className="parcel-detail-container">
          <div>Loading...</div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="parcel-detail-page">
        <div className="parcel-detail-container">
          <div style={{ color: 'red', padding: '20px' }}>{error}</div>
          <div className="return-button-container">
            <button className="return-button" onClick={onReturn}>
              Return
            </button>
          </div>
        </div>
      </div>
    );
  }

  if (!parcelDetail) {
    return (
      <div className="parcel-detail-page">
        <div className="parcel-detail-container">
          <div>No parcel details available</div>
          <div className="return-button-container">
            <button className="return-button" onClick={onReturn}>
              Return
            </button>
          </div>
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
            <div className="info-value">{parcelDetail.email || 'N/A'}</div>
          </div>
          
          <div className="info-row">
            <div className="info-label">Street Name</div>
            <div className="info-value">{parcelDetail.streetName}</div>
          </div>
          
          <div className="info-row">
            <div className="info-label">House Number</div>
            <div className="info-value">{parcelDetail.houseNumber || 'N/A'}</div>
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
