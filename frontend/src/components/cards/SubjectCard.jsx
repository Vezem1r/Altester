import { useTranslation } from 'react-i18next';

const SubjectCard = ({
  subject,
  isSelected = false,
  showGroups = false,
  groups = [],
  iconSize = 'medium',
}) => {
  const { t } = useTranslation();

  const sizeClasses = {
    small: 'w-10 h-10 text-xs',
    medium: 'w-12 h-12 text-xs',
    large: 'w-14 h-14 text-sm',
  };

  return (
    <div className="flex flex-col h-full justify-between">
      <div className="mb-2">
        <div className="flex items-center mb-2">
          <div
            className={`${sizeClasses[iconSize]} rounded-xl flex items-center justify-center font-bold mr-3 shadow-md transition-all duration-300 ${
              isSelected
                ? 'bg-gradient-to-br from-blue-600 to-indigo-700 text-white'
                : 'bg-gradient-to-br from-blue-500 to-indigo-600 text-white'
            }`}
          >
            {subject.shortName?.substring(0, 3).toUpperCase() || 'SUB'}
          </div>
          <div className="flex-1">
            <p className="font-semibold text-sm text-gray-900">
              {subject.name}
            </p>
            {subject.shortName && (
              <p className="text-xs text-gray-600">
                {t('subjectCard.code', 'Code')}: {subject.shortName}
              </p>
            )}
          </div>
        </div>
      </div>
      {showGroups && (
        <div className="space-y-1">
          <div
            className={`flex items-center justify-between px-3 py-1.5 rounded-lg ${
              isSelected ? 'bg-blue-100 border border-blue-300' : 'bg-gray-100'
            }`}
          >
            <span
              className={`text-xs ${isSelected ? 'text-blue-600' : 'text-gray-600'}`}
            >
              {t('subjectCard.totalGroups', 'Total Groups')}
            </span>
            <span
              className={`text-sm font-bold ${isSelected ? 'text-blue-800' : 'text-gray-700'}`}
            >
              {subject.groups?.length || 0}
            </span>
          </div>
          {isSelected && groups.length > 0 && (
            <div className="px-3 py-2 bg-blue-50 border border-blue-200 rounded-lg">
              <p className="text-xs text-blue-800 mb-1">
                {t(
                  'subjectCard.testAssignedTo',
                  'This test will be assigned to:'
                )}
              </p>
              <div className="flex flex-wrap gap-1">
                {groups.slice(0, 3).map((group, idx) => (
                  <span
                    key={idx}
                    className="text-xs bg-blue-200 text-blue-800 px-2 py-0.5 rounded"
                  >
                    {group.name}
                  </span>
                ))}
                {groups.length > 3 && (
                  <span className="text-xs text-blue-600">
                    +{groups.length - 3} {t('subjectCard.more', 'more')}
                  </span>
                )}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default SubjectCard;
