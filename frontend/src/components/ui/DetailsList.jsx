import { useTranslation } from 'react-i18next';

const DetailItem = ({ label, value, className = '', icon }) => {
  const { t } = useTranslation();

  return (
    <div className={`relative ${className}`}>
      {icon && (
        <div className="absolute left-0 top-0 h-full flex items-center">
          <div className="h-8 w-8 rounded-lg bg-purple-100 flex items-center justify-center">
            {icon}
          </div>
        </div>
      )}
      <div className={icon ? 'pl-12' : ''}>
        <dt className="text-xs font-medium text-gray-500 uppercase tracking-wider">
          {label}
        </dt>
        <dd className="mt-1 text-sm font-semibold text-gray-900 break-words">
          {value || (
            <span className="text-gray-400 italic">
              {t('detailsList.na', 'N/A')}
            </span>
          )}
        </dd>
      </div>
    </div>
  );
};

const DetailsList = ({ items, columns = 2, className = '' }) => {
  const gridCols = {
    1: 'sm:grid-cols-1',
    2: 'sm:grid-cols-2',
    3: 'sm:grid-cols-3',
    4: 'sm:grid-cols-4',
  };

  return (
    <div
      className={`bg-gradient-to-br from-gray-50 to-gray-100 p-6 rounded-xl ${className}`}
    >
      <dl className={`grid grid-cols-1 gap-6 ${gridCols[columns]}`}>
        {items.map((item, index) => (
          <DetailItem
            key={index}
            label={item.label}
            value={item.value}
            icon={item.icon}
            className={item.className}
          />
        ))}
      </dl>
    </div>
  );
};

const InfoCard = ({ title, children, icon, className = '' }) => {
  return (
    <div
      className={`bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-md transition-shadow duration-200 ${className}`}
    >
      {title && (
        <div className="px-6 py-4 bg-gradient-to-r from-gray-50 to-gray-100 border-b border-gray-100">
          <div className="flex items-center">
            {icon && (
              <div className="mr-3 p-2 bg-white rounded-lg shadow-sm">
                {icon}
              </div>
            )}
            <h3 className="text-base font-semibold text-gray-800">{title}</h3>
          </div>
        </div>
      )}
      <div className="p-6">{children}</div>
    </div>
  );
};

export { DetailItem, DetailsList, InfoCard };
