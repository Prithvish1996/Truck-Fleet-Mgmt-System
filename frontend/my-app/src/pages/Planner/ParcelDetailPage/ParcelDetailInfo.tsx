import React from 'react';
import { ParcelDetail } from '../../../types';
import ParcelInfoRow from './ParcelInfoRow';

interface ParcelDetailInfoProps {
  parcelDetail: ParcelDetail;
}

export default function ParcelDetailInfo({ parcelDetail }: ParcelDetailInfoProps) {
  return (
    <div className="parcel-detail-info">
      <ParcelInfoRow label="Parcel" value={parcelDetail.internalId} />
      <ParcelInfoRow label="Contact Person" value={parcelDetail.contactPerson} />
      <ParcelInfoRow label="Phone / Mobile Number" value={parcelDetail.phone} />
      <ParcelInfoRow label="Email Address" value={parcelDetail.email || 'N/A'} />
      <ParcelInfoRow label="Street Name" value={parcelDetail.streetName} />
      <ParcelInfoRow label="House Number" value={parcelDetail.houseNumber || 'N/A'} />
      <ParcelInfoRow label="ZIP Code" value={parcelDetail.zipCode} />
      <ParcelInfoRow label="City" value={parcelDetail.city} />
      <ParcelInfoRow label="Country" value={parcelDetail.country} />
      <ParcelInfoRow label="Types of items" value={parcelDetail.typesOfItems} />
      <ParcelInfoRow label="Special Instructions" value={parcelDetail.specialInstructions} italic />
      <ParcelInfoRow label="Remarks" value={parcelDetail.remarks} italic />
    </div>
  );
}

