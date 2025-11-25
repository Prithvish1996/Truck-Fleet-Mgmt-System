import React, { useState, useEffect } from 'react';
import { ParcelDetail } from '../../types';
import { plannerService } from '../../services/plannerService';
import { extractParcelId } from '../../utils/dataTransformers';
import ParcelDetailInfo from './ParcelDetailInfo';
import '../ParcelDetailPage.css';

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
      const parcelIdNum = extractParcelId(parcelId);
      
      if (!parcelIdNum || parcelIdNum === 0) {
        setError('Invalid parcel ID format.');
        return;
      }

      const parcel = await plannerService.getParcelById(parcelIdNum);
      
      const addressParts = parcel.deliveryAddress?.split(/\s+/) || [];
      let streetName = '';
      let houseNumber = '';
      
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
        email: '',
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
        
        <ParcelDetailInfo parcelDetail={parcelDetail} />

        <div className="return-button-container">
          <button className="return-button" onClick={onReturn}>
            Return
          </button>
        </div>
      </div>
    </div>
  );
}

