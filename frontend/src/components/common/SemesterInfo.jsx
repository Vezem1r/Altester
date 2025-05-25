import { useTranslation } from 'react-i18next';

const SemesterInfo = ({
  semester,
  academicYear,
  currentSemester,
  currentAcademicYear,
}) => {
  const { t } = useTranslation();

  if (!semester || !academicYear) return null;

  const isActive =
    semester === currentSemester &&
    Number(academicYear) === currentAcademicYear;

  return (
    <div
      className={`mt-4 p-3 rounded-lg border ${
        isActive
          ? 'bg-green-50 border-green-200 text-green-700'
          : 'bg-yellow-50 border-yellow-200 text-yellow-700'
      }`}
    >
      {isActive
        ? t(
            'semesterInfo.activeGroup',
            'This group will be active for the current semester'
          )
        : t(
            'semesterInfo.inactiveGroup',
            "This group will be inactive as it's not for the current semester"
          )}
    </div>
  );
};

export default SemesterInfo;
