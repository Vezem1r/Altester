import React from 'react';

const InfoItem = ({ label, value, className = '' }) => {
  return (
    <div className={className}>
      <dt className="text-sm font-medium text-gray-500">{label}</dt>
      <dd className="mt-1 text-sm text-gray-900">{value}</dd>
    </div>
  );
};

export default InfoItem;
