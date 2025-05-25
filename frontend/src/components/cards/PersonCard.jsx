import { useTranslation } from 'react-i18next';

const PersonCard = ({
                      person,
                      isSelected = false,
                      showUsername = true,
                      showId = false,
                      size = 'medium',
                      gradientFrom = 'purple-500',
                      gradientTo = 'purple-600',
                      selectedGradientFrom = 'purple-600',
                      selectedGradientTo = 'indigo-700',
                      selectedTextColor = 'white',
                    }) => {
  const { t } = useTranslation();

  const sizeClasses = {
    small: 'w-10 h-10 text-sm',
    medium: 'w-12 h-12 text-sm',
    large: 'w-14 h-14 text-base',
  };

  return (
      <div className="flex flex-col h-full justify-between card-hover-fix">
        <div className="flex items-center mb-2">
          <div
              className={`${sizeClasses[size]} rounded-full flex items-center justify-center font-bold mr-3 transition-all duration-300 shadow-md avatar-initials ${
                  isSelected
                      ? 'bg-gradient-to-br from-purple-600 to-indigo-700 text-white'
                      : 'bg-gradient-to-br from-gray-500 to-gray-600 text-white'
              }`}
          >
            {person.name[0]}
            {person.surname[0]}
          </div>
          <div className="flex-1">
            <p className="font-semibold text-sm text-gray-900">
              {person.name} {person.surname}
            </p>
            {showUsername && person.username && (
                <p className="text-xs text-gray-600">@{person.username}</p>
            )}
          </div>
        </div>
        {showId && person.userId && (
            <div
                className={`flex items-center justify-between px-2 py-1 rounded-lg ${
                    isSelected
                        ? 'bg-purple-100 border border-purple-300'
                        : 'bg-gray-100'
                }`}
            >
          <span
              className={`text-xs ${isSelected ? 'text-purple-600' : 'text-gray-600'}`}
          >
            {t('personCard.id', 'ID')}
          </span>
              <span
                  className={`text-xs font-medium ${isSelected ? 'text-purple-800' : 'text-gray-700'}`}
              >
            {person.userId}
          </span>
            </div>
        )}
      </div>
  );
};

export default PersonCard;
