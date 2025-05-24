import React from 'react';

const InfoCard = ({ title, description, children, className = '' }) => {
  return (
    <div
      className={`bg-white shadow-lg rounded-xl overflow-hidden ${className}`}
    >
      {(title || description) && (
        <div className="px-6 py-4 border-b border-gray-200 bg-gradient-to-r from-purple-600 to-purple-400">
          {title && (
            <h3 className="text-lg font-semibold text-white">{title}</h3>
          )}
          {description && (
            <p className="mt-1 text-sm text-white">{description}</p>
          )}
        </div>
      )}
      <div className="p-6">{children}</div>
    </div>
  );
};

export default InfoCard;
