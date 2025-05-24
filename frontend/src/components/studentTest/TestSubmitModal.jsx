import { memo, useState } from 'react';
import { useTranslation } from 'react-i18next';

const TestSubmitModal = memo(
  ({
    onClose,
    onConfirm,
    test,
    answeredQuestions,
    totalQuestions,
    questionStatuses,
    submitting = false,
  }) => {
    const { t } = useTranslation();
    const [isSubmitting, setIsSubmitting] = useState(submitting);
    const unansweredQuestions = questionStatuses
      ? questionStatuses.filter(q => !q.isAnswered).map(q => q.questionNumber)
      : [];

    const handleConfirm = async () => {
      setIsSubmitting(true);
      try {
        await onConfirm();
      } catch {
        setIsSubmitting(false);
      }
    };

    return (
      <div className="fixed inset-0 bg-gray-900 bg-opacity-75 flex items-center justify-center z-50 px-4 sm:px-0 backdrop-blur-sm">
        <div className="bg-white rounded-xl shadow-2xl max-w-lg w-full overflow-hidden transform transition-all animate-fadeIn">
          <div className="bg-gradient-to-r from-purple-600 to-indigo-600 px-6 py-5 text-white relative">
            <div className="absolute top-0 right-0 -mt-6 -mr-6 w-24 h-24 bg-purple-200 rounded-full opacity-20" />
            <div className="absolute bottom-0 left-0 -mb-6 -ml-6 w-16 h-16 bg-indigo-200 rounded-full opacity-20" />

            <h3 className="text-xl font-bold relative z-10 flex items-center">
              <svg
                className="h-6 w-6 mr-2"
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
                aria-hidden="true"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
              {t('testSubmitModal.title', 'Submit Test')}
            </h3>
            <p className="text-purple-100 text-sm mt-1 relative z-10">
              {t(
                'testSubmitModal.pleaseConfirm',
                'Please confirm to submit your test'
              )}
            </p>
          </div>

          <div className="p-6">
            <h2 className="text-xl font-bold text-gray-900 mb-4">
              {test?.title ||
                t('testSubmitModal.completeYourTest', 'Complete Your Test')}
            </h2>

            <div className="mb-6">
              <div className="flex items-center justify-between mb-2">
                <div className="text-sm font-medium text-gray-700">
                  {t('testSubmitModal.completionStatus', 'Completion Status')}
                </div>
                <div className="text-sm font-medium text-purple-600">
                  {t(
                    'testSubmitModal.answeredCount',
                    '{{answered}}/{{total}} answered',
                    {
                      answered: answeredQuestions,
                      total: totalQuestions,
                    }
                  )}
                </div>
              </div>

              <div className="w-full bg-gray-200 rounded-full h-2.5">
                <div
                  className="bg-purple-600 h-2.5 rounded-full transition-all duration-300 ease-in-out"
                  style={{
                    width: `${Math.round((answeredQuestions / totalQuestions) * 100)}%`,
                  }}
                  aria-valuemax={totalQuestions}
                  aria-valuemin="0"
                  aria-valuenow={answeredQuestions}
                  role="progressbar"
                />
              </div>
            </div>

            <div className="bg-gray-50 rounded-lg p-4 mb-6">
              <h3 className="text-sm font-medium text-gray-700 mb-3">
                {t('testSubmitModal.questionStatus', 'Question Status:')}
              </h3>

              <div className="grid grid-cols-5 gap-2">
                {questionStatuses &&
                  questionStatuses.map(status => (
                    <div
                      key={`status-${status.questionNumber}`}
                      className={`flex flex-col items-center justify-center p-2 rounded-lg ${
                        status.isAnswered
                          ? 'bg-green-100 text-green-800 border border-green-200'
                          : 'bg-amber-100 text-amber-800 border border-amber-200'
                      }`}
                    >
                      <span className="text-sm font-medium">
                        {status.questionNumber}
                      </span>
                      <span className="text-xs mt-1">
                        {status.isAnswered
                          ? t('testSubmitModal.answered', 'Answered')
                          : t('testSubmitModal.unanswered', 'Unanswered')}
                      </span>
                    </div>
                  ))}
              </div>
            </div>

            {unansweredQuestions.length > 0 && (
              <div className="mb-6 bg-amber-50 rounded-lg p-4 border-l-4 border-amber-400">
                <div className="flex">
                  <div className="flex-shrink-0 text-amber-500">
                    <svg
                      className="h-5 w-5"
                      xmlns="http://www.w3.org/2000/svg"
                      viewBox="0 0 20 20"
                      fill="currentColor"
                      aria-hidden="true"
                    >
                      <path
                        fillRule="evenodd"
                        d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z"
                        clipRule="evenodd"
                      />
                    </svg>
                  </div>
                  <div className="ml-3">
                    <h3 className="text-sm font-medium text-amber-800">
                      {t(
                        'testSubmitModal.unansweredQuestionsWarning',
                        'You have unanswered questions'
                      )}
                    </h3>
                    <div className="mt-2 text-sm text-amber-700">
                      <p>
                        {t(
                          'testSubmitModal.unansweredQuestionsDetail',
                          'Questions {{questions}} are not answered yet. Do you still want to submit?',
                          {
                            questions: unansweredQuestions.join(', '),
                          }
                        )}
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            )}

            <p className="text-gray-600 mb-6">
              {t(
                'testSubmitModal.submissionWarning',
                "Once submitted, you won't be able to make any changes to your answers. Are you sure you want to submit your test?"
              )}
            </p>

            <div className="flex justify-end space-x-3">
              <button
                type="button"
                className="inline-flex items-center px-4 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                onClick={onClose}
                disabled={isSubmitting}
              >
                {t('testSubmitModal.continueTest', 'Continue Test')}
              </button>
              <button
                type="button"
                className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-700 hover:to-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                onClick={handleConfirm}
                disabled={isSubmitting}
              >
                {isSubmitting ? (
                  <>
                    <svg
                      className="animate-spin -ml-1 mr-2 h-4 w-4 text-white"
                      xmlns="http://www.w3.org/2000/svg"
                      fill="none"
                      viewBox="0 0 24 24"
                    >
                      <circle
                        className="opacity-25"
                        cx="12"
                        cy="12"
                        r="10"
                        stroke="currentColor"
                        strokeWidth="4"
                      />
                      <path
                        className="opacity-75"
                        fill="currentColor"
                        d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                      />
                    </svg>
                    {t('testSubmitModal.submitting', 'Submitting...')}
                  </>
                ) : (
                  <>
                    <svg
                      className="mr-2 h-4 w-4"
                      xmlns="http://www.w3.org/2000/svg"
                      viewBox="0 0 20 20"
                      fill="currentColor"
                      aria-hidden="true"
                    >
                      <path
                        fillRule="evenodd"
                        d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                        clipRule="evenodd"
                      />
                    </svg>
                    {t('testSubmitModal.submitTest', 'Submit Test')}
                  </>
                )}
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }
);

TestSubmitModal.displayName = 'TestSubmitModal';

export default TestSubmitModal;
