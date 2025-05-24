import { useTranslation } from 'react-i18next';

const GroupCard = ({ group, isSelected = false, showStatus = true }) => {
  const { t } = useTranslation();

  const getStatusConfig = () => {
    if (group.active && !group.inFuture) {
      return { color: 'green', label: t('groupCard.status.active', 'Active') };
    } else if (group.inFuture) {
      return { color: 'blue', label: t('groupCard.status.future', 'Future') };
    }
    return {
      color: 'gray',
      label: t('groupCard.status.inactive', 'Inactive'),
    };
  };

  const status = getStatusConfig();

  return (
    <div className="flex flex-col h-full justify-between">
      <div>
        <p
          className={`font-semibold text-base ${isSelected ? 'text-gray-900' : 'text-gray-900'}`}
        >
          {group.name}
        </p>
        <p
          className={`text-sm ${isSelected ? 'text-gray-700' : 'text-gray-600'}`}
        >
          {group.semester} {group.academicYear}
        </p>
      </div>
      {showStatus && (
        <div
          className={`mt-2 flex items-center justify-between px-2 py-1 rounded-lg ${
            isSelected ? 'bg-purple-500/30' : 'bg-gray-100'
          }`}
        >
          <span
            className={`text-xs ${isSelected ? 'text-purple-100' : 'text-gray-600'}`}
          >
            {t('groupCard.status.label', 'Status')}
          </span>
          <span className="text-xs font-medium flex items-center">
            <span
              className={`h-2 w-2 bg-${status.color}-500 rounded-full mr-1`}
            />
            <span
              className={isSelected ? 'text-white' : `text-${status.color}-700`}
            >
              {status.label}
            </span>
          </span>
        </div>
      )}
    </div>
  );
};

export default GroupCard;
