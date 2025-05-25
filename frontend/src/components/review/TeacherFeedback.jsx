import { memo } from 'react';
import { useTranslation } from 'react-i18next';

const TeacherFeedback = memo(({ feedback, aiFeedback, status }) => {
  const { t } = useTranslation();
  const isAiReviewed = status === 'AI_REVIEWED';
  const hasFeedback = !!feedback;
  const hasAiFeedback = !!aiFeedback;

  return (
    <div
      className={`px-6 py-4 border-t ${hasFeedback || hasAiFeedback ? 'border-blue-200 bg-blue-50' : 'border-gray-200 bg-gray-50'}`}
    >
      {/* Teacher Feedback Section */}
      {(!isAiReviewed || (isAiReviewed && hasFeedback)) && (
        <>
          <h3
            className={`text-md font-medium mb-2 flex items-center ${hasFeedback ? 'text-blue-900' : 'text-gray-500'}`}
          >
            <svg
              className={`h-5 w-5 mr-2 ${hasFeedback ? 'text-blue-500' : 'text-gray-400'}`}
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 20 20"
              fill="currentColor"
            >
              <path
                fillRule="evenodd"
                d="M18 5v8a2 2 0 01-2 2h-5l-5 4v-4H4a2 2 0 01-2-2V5a2 2 0 012-2h12a2 2 0 012 2zM7 8H5v2h2V8zm2 0h2v2H9V8zm6 0h-2v2h2V8z"
                clipRule="evenodd"
              />
            </svg>
            {t('teacherFeedback.teacherFeedback', 'Teacher Feedback')}
          </h3>

          {hasFeedback ? (
            <div className="p-3 bg-white rounded-md border border-blue-200 shadow-sm mb-4">
              <p className="text-blue-700 whitespace-pre-wrap">{feedback}</p>
            </div>
          ) : (
            <div className="flex items-center text-gray-500 mb-4">
              <svg
                className="h-5 w-5 mr-2 text-gray-400"
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
              <p className="text-sm italic">
                {t(
                  'teacherFeedback.feedbackNotProvided',
                  'Feedback not provided yet'
                )}
              </p>
            </div>
          )}
        </>
      )}

      {/* AI Feedback Section */}
      {hasAiFeedback && (
        <>
          <h3 className="text-md font-medium mb-2 flex items-center text-green-900">
            <svg
              className="h-5 w-5 mr-2 text-green-500"
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 20 20"
              fill="currentColor"
            >
              <path
                fillRule="evenodd"
                d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-11a1 1 0 10-2 0v2H7a1 1 0 100 2h2v2a1 1 0 102 0v-2h2a1 1 0 100-2h-2V7z"
                clipRule="evenodd"
              />
            </svg>
            {t('teacherFeedback.aiFeedback', 'AI Feedback')}
          </h3>

          <div className="p-3 bg-white rounded-md border border-green-200 shadow-sm">
            <p className="text-green-700 whitespace-pre-wrap">{aiFeedback}</p>
          </div>
        </>
      )}
    </div>
  );
});

TeacherFeedback.displayName = 'TeacherFeedback';

export default TeacherFeedback;
