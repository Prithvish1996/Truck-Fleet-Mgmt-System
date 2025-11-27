import React from 'react';

interface ParcelInfoRowProps {
  label: string;
  value: string;
  italic?: boolean;
}

export default function ParcelInfoRow({ label, value, italic = false }: ParcelInfoRowProps) {
  return (
    <div className="info-row">
      <div className="info-label">{label}</div>
      <div className={`info-value ${italic ? 'info-value-italic' : ''}`}>{value}</div>
    </div>
  );
}

