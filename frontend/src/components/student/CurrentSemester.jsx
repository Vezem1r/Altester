import { useState } from 'react';
import { useTranslation } from 'react-i18next';

const CurrentSemester = ({
  currentPeriod,
  availablePeriods = [],
  onPeriodChange,
  loading = false,
  isCurrentSemester = true,
}) => {
  const { t } = useTranslation();
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);

  const formatSemester = semester => {
    return semester === 'WINTER'
      ? t('currentSemester.winterSemester', 'Winter Semester')
      : t('currentSemester.summerSemester', 'Summer Semester');
  };

  const formattedCurrentPeriod = currentPeriod
    ? `${formatSemester(currentPeriod.semester)} ${currentPeriod.academicYear} ${isCurrentSemester ? t('currentSemester.current', '(Current)') : ''}`
    : t('currentSemester.loading', 'Loading...');

  const toggleDropdown = () => {
    setIsDropdownOpen(!isDropdownOpen);
  };

  const handlePeriodSelect = period => {
    if (onPeriodChange) {
      onPeriodChange(period);
    }
    setIsDropdownOpen(false);
  };

  return (
    <div className="relative">
      {availablePeriods.length > 0 && (
        <div>
          <div className="relative">
            <button
              onClick={toggleDropdown}
              className="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500"
              disabled={loading}
            >
              <span>
                {loading
                  ? t('currentSemester.loading', 'Loading...')
                  : formattedCurrentPeriod}
              </span>
              <svg
                className="ml-2 -mr-0.5 h-4 w-4"
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 20 20"
                fill="currentColor"
              >
                <path
                  fillRule="evenodd"
                  d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z"
                  clipRule="evenodd"
                />
              </svg>
            </button>

            {isDropdownOpen && (
              <div className="origin-top-right absolute right-0 mt-2 w-56 rounded-md shadow-lg bg-white ring-1 ring-black ring-opacity-5 z-10">
                <div className="py-1" role="menu" aria-orientation="vertical">
                  <button
                    key="current-semester"
                    onClick={() =>
                      handlePeriodSelect({ currentSemester: true })
                    }
                    className={`w-full text-left block px-4 py-2 text-sm ${
                      isCurrentSemester
                        ? 'bg-purple-50 text-purple-700'
                        : 'text-gray-700 hover:bg-gray-50'
                    }`}
                    role="menuitem"
                  >
                    {t('currentSemester.currentSemester', 'Current Semester')}
                  </button>
                  <div className="border-t border-gray-100 my-1" />
                  {availablePeriods.map(period => (
                    <button
                      key={`${period.academicYear}-${period.semester}`}
                      onClick={() => handlePeriodSelect(period)}
                      className={`w-full text-left block px-4 py-2 text-sm ${
                        !isCurrentSemester &&
                        currentPeriod &&
                        currentPeriod.academicYear === period.academicYear &&
                        currentPeriod.semester === period.semester
                          ? 'bg-purple-50 text-purple-700'
                          : 'text-gray-700 hover:bg-gray-50'
                      }`}
                      role="menuitem"
                    >
                      {formatSemester(period.semester)} {period.academicYear}
                    </button>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default CurrentSemester;
