import React from 'react';

const StatCard = ({
  icon,
  label,
  value,
  bgColor = 'bg-purple-50',
  textColor = 'text-purple-800',
  iconColor = 'text-purple-600',
  borderColor = 'border-purple-200',
  description,
}) => {
  return (
    <div className={`${bgColor} p-4 rounded-lg border ${borderColor}`}>
      <div className="flex items-center">
        <div className="flex-shrink-0">
          <div className={`p-2 rounded-lg ${bgColor} ${iconColor}`}>{icon}</div>
        </div>
        <div className="ml-4">
          <p className="text-sm font-medium text-gray-600">{label}</p>
          <div className="mt-1 flex items-baseline">
            <p className={`text-xl font-bold ${textColor}`}>{value}</p>
            {description && (
              <p className="ml-2 text-sm text-gray-600">{description}</p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default StatCard;
