import { useState, memo, useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import { toast } from 'react-toastify';
import { StudentService } from '@/services/StudentService';

const RegradeRequestComponent = memo(({ questions, onRefresh }) => {
  const { t } = useTranslation();
  const [selectedQuestions, setSelectedQuestions] = useState([]);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const aiGradedQuestions = useMemo(() => {
    return questions.filter(question => question.aiGraded);
  }, [questions]);

  const eligibleQuestions = useMemo(() => {
    return aiGradedQuestions.filter(question => !question.requested);
  }, [aiGradedQuestions]);

  const handleQuestionToggle = submissionId => {
    setSelectedQuestions(prev =>
      prev.includes(submissionId)
        ? prev.filter(id => id !== submissionId)
        : [...prev, submissionId]
    );
  };

  const handleSubmitRequest = async () => {
    if (selectedQuestions.length === 0) {
      toast.warning(
        t(
          'regradeRequestComponent.selectQuestionsWarning',
          'Please select at least one question to request re-grading'
        )
      );
      return;
    }

    try {
      setIsSubmitting(true);
      await StudentService.requestRegrade(selectedQuestions);
      toast.success(
        t(
          'regradeRequestComponent.successMessage',
          'Successfully requested re-grading'
        )
      );
      setSelectedQuestions([]);
      if (onRefresh) {
        onRefresh();
      }
    } catch (error) {
      toast.error(
        error.message ||
          t(
            'regradeRequestComponent.errorMessage',
            'Failed to request re-grading'
          )
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  const getDisplayScore = question => {
    if (
      question.score === -1 &&
      question.aiScore !== null &&
      question.aiScore !== undefined
    ) {
      return question.aiScore;
    }

    if (question.score !== null && question.score !== undefined) {
      return question.score;
    }

    if (question.aiScore !== null && question.aiScore !== undefined) {
      return question.aiScore;
    }

    return 0;
  };

  if (aiGradedQuestions.length === 0) {
    return null;
  }

  return (
    <div className="bg-white shadow-md rounded-xl overflow-hidden mb-6">
      <div className="bg-gradient-to-r from-pink-500 to-purple-600 px-6 py-5 text-white">
        <h2 className="text-xl font-bold relative z-10 flex items-center">
          <svg
            className="h-6 w-6 mr-2"
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth="2"
              d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
            />
          </svg>
          {t('regradeRequestComponent.requestRegrading', 'Request Re-grading')}
        </h2>
        <p className="text-pink-100 text-sm mt-1 relative z-10">
          {t(
            'regradeRequestComponent.selectAiQuestions',
            'Select AI-graded questions to request teacher review'
          )}
        </p>
      </div>

      <div className="px-6 py-5 bg-white">
        {eligibleQuestions.length === 0 ? (
          <div className="text-center py-4">
            <svg
              className="mx-auto h-12 w-12 text-gray-400"
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="2"
                d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
            <p className="mt-2 text-sm text-gray-500">
              {t(
                'regradeRequestComponent.allQuestionsRequested',
                'All AI-graded questions have already been requested for re-grading'
              )}
            </p>
          </div>
        ) : (
          <>
            <div className="space-y-3 max-h-64 overflow-y-auto pr-2">
              {aiGradedQuestions.map((question, index) => {
                const plainQuestionText = question.questionText.replace(
                  /<[^>]*>?/gm,
                  ''
                );
                const isEligible = !question.requested;
                const isSelected = selectedQuestions.includes(
                  question.submissionId
                );

                const displayScore = getDisplayScore(question);

                return (
                  <div
                    key={question.submissionId}
                    className={`flex items-center p-3 rounded-lg border ${
                      isEligible
                        ? isSelected
                          ? 'border-purple-300 bg-purple-50'
                          : 'border-gray-200 hover:border-purple-200 hover:bg-purple-50/50'
                        : 'border-gray-100 bg-gray-50 opacity-60'
                    } transition-all duration-200`}
                  >
                    <div className="flex-shrink-0 mr-3">
                      {isEligible ? (
                        <label className="relative flex items-center cursor-pointer">
                          <input
                            type="checkbox"
                            checked={isSelected}
                            onChange={() =>
                              handleQuestionToggle(question.submissionId)
                            }
                            className="form-checkbox h-5 w-5 text-purple-600 rounded border-gray-300 focus:ring-purple-500"
                          />
                        </label>
                      ) : (
                        <div className="flex items-center justify-center h-5 w-5 rounded bg-green-100 text-green-600">
                          <svg
                            className="h-4 w-4"
                            xmlns="http://www.w3.org/2000/svg"
                            viewBox="0 0 20 20"
                            fill="currentColor"
                          >
                            <path
                              fillRule="evenodd"
                              d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                              clipRule="evenodd"
                            />
                          </svg>
                        </div>
                      )}
                    </div>

                    <div className="flex-1 min-w-0">
                      <div className="flex items-center justify-between">
                        <div className="flex items-center space-x-2">
                          <span className="inline-flex items-center justify-center h-6 w-6 rounded-full bg-purple-100 text-purple-800 text-xs font-medium">
                            {index + 1}
                          </span>
                          <span
                            className={`text-sm ${isEligible ? 'text-gray-800' : 'text-gray-500'} truncate max-w-xs`}
                          >
                            {plainQuestionText}
                          </span>
                        </div>
                        <div className="flex items-center space-x-2 ml-2">
                          <span
                            className={`px-2 py-0.5 rounded-full text-xs font-medium ${
                              displayScore === question.maxScore
                                ? 'bg-green-100 text-green-800'
                                : displayScore > 0
                                  ? 'bg-yellow-100 text-yellow-800'
                                  : 'bg-red-100 text-red-800'
                            }`}
                          >
                            {displayScore}/{question.maxScore}
                          </span>
                          {question.requested && (
                            <span className="px-2 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                              {t(
                                'regradeRequestComponent.requested',
                                'Requested'
                              )}
                            </span>
                          )}
                        </div>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>

            {eligibleQuestions.length > 0 && (
              <div className="mt-6 flex justify-between items-center">
                <p className="text-sm text-gray-500">
                  {t(
                    'regradeRequestComponent.questionsSelected',
                    '{{count}} of {{total}} eligible questions selected',
                    {
                      count: selectedQuestions.length,
                      total: eligibleQuestions.length,
                    }
                  )}
                </p>
                <button
                  onClick={handleSubmitRequest}
                  disabled={selectedQuestions.length === 0 || isSubmitting}
                  className={`inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white ${
                    selectedQuestions.length === 0 || isSubmitting
                      ? 'bg-gray-400 cursor-not-allowed'
                      : 'bg-purple-600 hover:bg-purple-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500'
                  }`}
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
                      {t('regradeRequestComponent.submitting', 'Submitting...')}
                    </>
                  ) : (
                    <>
                      <svg
                        className="mr-2 h-4 w-4"
                        xmlns="http://www.w3.org/2000/svg"
                        viewBox="0 0 20 20"
                        fill="currentColor"
                      >
                        <path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z" />
                        <path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z" />
                      </svg>
                      {t(
                        'regradeRequestComponent.requestRegrading',
                        'Request Re-grading'
                      )}
                    </>
                  )}
                </button>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
});

RegradeRequestComponent.displayName = 'RegradeRequestComponent';

export default RegradeRequestComponent;
