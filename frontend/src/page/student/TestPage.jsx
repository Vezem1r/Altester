import { useState, useEffect, useRef, useCallback, memo } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { toast } from 'react-toastify';
import { useAuth } from '@/context/AuthContext';
import { StudentService } from '@/services/StudentService';
import { useTestAttempt } from '@/hooks/useTestAttempt';
import { useTranslation } from 'react-i18next';

import AuthenticatedImage from '@/components/questions/AuthenticatedImage';
import {
  QuestionProgress,
  TestSubmitModal,
  TestTimer,
  TestConfirmModal,
} from '@/components/studentTest';

const hasAnswerContent = answer => {
  if (!answer) return false;

  if (answer.selectedOptionIds && answer.selectedOptionIds.length > 0) {
    return true;
  }

  if (answer.answerText && answer.answerText.trim() !== '') {
    return true;
  }

  return false;
};

const TestOption = memo(({ option, isSelected, isMultiple, onClick }) => {
  return (
    <button
      type="button"
      className={`w-full rounded-lg border p-4 transition-all cursor-pointer text-left
        ${
          isSelected
            ? 'border-purple-300 bg-purple-50 shadow-md transform scale-[1.01]'
            : 'border-gray-200 hover:border-purple-200 hover:bg-purple-50'
        }`}
      onClick={onClick}
    >
      <div className="flex items-start">
        <div className="flex-shrink-0 mt-0.5 mr-3">
          {isMultiple ? (
            <div
              className={`h-5 w-5 border rounded flex items-center justify-center ${
                isSelected
                  ? 'bg-purple-600 border-purple-600'
                  : 'bg-white border-gray-300'
              }`}
            >
              {isSelected && (
                <svg
                  className="h-3 w-3 text-white"
                  fill="currentColor"
                  viewBox="0 0 20 20"
                  aria-hidden="true"
                >
                  <path
                    fillRule="evenodd"
                    d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                    clipRule="evenodd"
                  />
                </svg>
              )}
            </div>
          ) : (
            <div
              className={`h-5 w-5 border rounded-full flex items-center justify-center ${
                isSelected ? 'border-purple-600' : 'border-gray-300'
              }`}
            >
              {isSelected && (
                <div className="h-3 w-3 rounded-full bg-purple-600" />
              )}
            </div>
          )}
        </div>
        <div className="flex-1">
          <div
            dangerouslySetInnerHTML={{ __html: option.text }}
            className="text-gray-800"
          />
          {option.description && (
            <p className="mt-1 text-sm text-gray-500">{option.description}</p>
          )}
        </div>
      </div>
    </button>
  );
});

TestOption.displayName = 'TestOption';

const QuestionNumberGrid = memo(
  ({
    totalQuestions,
    currentQuestionNumber,
    answeredQuestionsMap,
    navigateToQuestion,
    loading,
    t,
  }) => {
    return (
      <div className="grid grid-cols-5 gap-2 mb-4">
        {Array.from({ length: totalQuestions }, (_, i) => i + 1).map(num => {
          const isCurrentQuestion = num === currentQuestionNumber;
          const isAnswered = !!answeredQuestionsMap[num];

          return (
            <button
              key={`question-${num}`}
              className={`w-full h-10 rounded-md flex items-center justify-center text-sm focus:outline-none transition-all duration-200 
              ${
                isCurrentQuestion
                  ? 'ring-2 ring-purple-500 font-bold scale-110'
                  : ''
              } 
              ${
                isAnswered
                  ? 'bg-green-50 border border-green-300'
                  : 'bg-white border border-gray-300'
              }`}
              onClick={() => navigateToQuestion(num)}
              disabled={loading}
              title={
                isAnswered
                  ? t('testPage.answered', 'Answered')
                  : t('testPage.notAnsweredYet', 'Not answered yet')
              }
              aria-current={isCurrentQuestion ? 'true' : 'false'}
              aria-label={
                isAnswered
                  ? t(
                      'testPage.questionAnswered',
                      'Question {{num}} (answered)',
                      { num }
                    )
                  : t(
                      'testPage.questionNotAnswered',
                      'Question {{num}} (not answered)',
                      { num }
                    )
              }
            >
              <span>{num}</span>
              {isAnswered && (
                <svg
                  className="ml-1 h-3 w-3 text-green-600"
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
              )}
            </button>
          );
        })}
      </div>
    );
  }
);

QuestionNumberGrid.displayName = 'QuestionNumberGrid';

const TestQuestion = memo(
  ({
    currentQuestion,
    currentQuestionNumber,
    currentAnswer,
    handleOptionSelect,
    handleTextAnswer,
    t,
  }) => {
    if (!currentQuestion) return null;

    return (
      <div className="bg-white shadow-md rounded-lg overflow-hidden border border-gray-200">
        <div className="p-6">
          <div className="flex items-start mb-4">
            <div className="flex-shrink-0 mr-3">
              <span
                className={`inline-flex items-center justify-center h-10 w-10 rounded-full ${
                  hasAnswerContent(currentAnswer)
                    ? 'bg-green-100 text-green-800'
                    : 'bg-purple-100 text-purple-800'
                } text-lg font-semibold`}
              >
                {currentQuestionNumber}
                {hasAnswerContent(currentAnswer) && (
                  <svg
                    className="ml-1 h-4 w-4"
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
                )}
              </span>
            </div>
            <div>
              <h2
                className="text-xl font-medium text-gray-900"
                dangerouslySetInnerHTML={{
                  __html: currentQuestion.questionText,
                }}
              />
              {currentQuestion.score && (
                <p className="text-sm text-gray-500 mt-1">
                  ({currentQuestion.score}{' '}
                  {currentQuestion.score === 1
                    ? t('testPage.point', 'point')
                    : t('testPage.points', 'points')}
                  )
                </p>
              )}
            </div>
          </div>

          {currentQuestion.imagePath && (
            <div className="my-4 border rounded-lg overflow-hidden">
              <AuthenticatedImage
                imagePath={currentQuestion.imagePath}
                alt={t('testPage.questionImage', 'Question')}
                className="max-w-full h-auto mx-auto rounded-md"
              />
            </div>
          )}

          <div className="mt-6">
            {currentQuestion.questionType.includes('CHOICE') ? (
              <div className="space-y-3">
                {currentQuestion.options.map(option => {
                  const isMultiple =
                    currentQuestion.questionType.includes('MULTIPLE');
                  const isSelected =
                    currentAnswer?.selectedOptionIds?.includes(option.id) ||
                    false;

                  return (
                    <TestOption
                      key={option.id}
                      option={option}
                      isSelected={isSelected}
                      isMultiple={isMultiple}
                      onClick={() =>
                        handleOptionSelect(
                          currentQuestion.id,
                          option.id,
                          isMultiple
                        )
                      }
                    />
                  );
                })}
              </div>
            ) : (
              <div>
                <textarea
                  rows="6"
                  className="w-full px-3 py-2 text-gray-700 border rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent resize-y shadow-sm"
                  placeholder={t(
                    'testPage.typeAnswerHere',
                    'Type your answer here...'
                  )}
                  value={currentAnswer?.answerText || ''}
                  onChange={e =>
                    handleTextAnswer(currentQuestion.id, e.target.value)
                  }
                  aria-label={t('testPage.textAnswer', 'Text answer')}
                />

                <div className="mt-2 text-right text-xs text-gray-500">
                  {currentAnswer?.answerText
                    ? currentAnswer.answerText.length
                    : 0}{' '}
                  {t('testPage.characters', 'characters')}
                </div>
              </div>
            )}
          </div>
        </div>

        <div className="px-6 py-4 bg-gray-50 border-t border-gray-200">
          <div className="flex flex-col sm:flex-row justify-between items-center space-y-4 sm:space-y-0">
            <div className="flex space-x-2">
              <button
                type="button"
                className="px-4 py-2 border border-gray-300 rounded-lg shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                onClick={() => currentQuestion.onPrevious()}
                disabled={currentQuestionNumber <= 1 || currentQuestion.loading}
              >
                <span className="flex items-center">
                  <svg
                    className="mr-2 h-4 w-4"
                    xmlns="http://www.w3.org/2000/svg"
                    viewBox="0 0 20 20"
                    fill="currentColor"
                    aria-hidden="true"
                  >
                    <path
                      fillRule="evenodd"
                      d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z"
                      clipRule="evenodd"
                    />
                  </svg>
                  {t('testPage.previous', 'Previous')}
                </span>
              </button>

              {currentQuestionNumber < currentQuestion.totalQuestions ? (
                <button
                  type="button"
                  className="px-4 py-2 border border-transparent rounded-lg shadow-sm text-sm font-medium text-white bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-700 hover:to-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                  onClick={() => currentQuestion.onNext()}
                  disabled={currentQuestion.loading}
                >
                  <span className="flex items-center">
                    {t('testPage.next', 'Next')}
                    <svg
                      className="ml-2 h-4 w-4"
                      xmlns="http://www.w3.org/2000/svg"
                      viewBox="0 0 20 20"
                      fill="currentColor"
                      aria-hidden="true"
                    >
                      <path
                        fillRule="evenodd"
                        d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z"
                        clipRule="evenodd"
                      />
                    </svg>
                  </span>
                </button>
              ) : (
                <button
                  type="button"
                  className="px-4 py-2 border border-transparent rounded-lg shadow-sm text-sm font-medium text-white bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-700 hover:to-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                  onClick={() =>
                    currentQuestion.onSubmit && currentQuestion.onSubmit()
                  }
                  disabled={currentQuestion.loading}
                >
                  <span className="flex items-center">
                    {t('testPage.submitTest', 'Submit Test')}
                    <svg
                      className="ml-2 h-4 w-4"
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
                  </span>
                </button>
              )}
            </div>

            <div className="text-sm text-gray-500 flex items-center">
              {currentAnswer && hasAnswerContent(currentAnswer) ? (
                <div className="flex items-center text-green-600">
                  <span className="h-2 w-2 rounded-full bg-green-500 mr-2" />
                  <span>
                    {t(
                      'testPage.answerSavedAutomatically',
                      'Answer saved automatically'
                    )}
                    <svg
                      className="ml-1 h-4 w-4 inline"
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
                  </span>
                </div>
              ) : (
                <div className="flex items-center">
                  <span className="h-2 w-2 rounded-full bg-gray-300 mr-2" />
                  <span>
                    {t('testPage.noAnswerProvided', 'No answer provided yet')}
                  </span>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    );
  }
);

TestQuestion.displayName = 'TestQuestion';

const TestPage = () => {
  const { t } = useTranslation();
  const { id: testId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const { user: _user } = useAuth();

  const isPreConfirmed = location.state?.confirmed === true;
  const [showConfirmModal, setShowConfirmModal] = useState(!isPreConfirmed);
  const [showSubmitModal, setShowSubmitModal] = useState(false);
  const [test, setTest] = useState(location.state?.test || null);
  const [testStatus, setTestStatus] = useState(null);
  const [isTestActive, setIsTestActive] = useState(false);

  const {
    attemptId,
    loading,
    submitting,
    currentQuestion,
    currentQuestionNumber,
    totalQuestions,
    timeRemaining,
    currentAnswer,
    answeredQuestionsMap,
    totalAnsweredQuestions,
    startAttempt,
    saveAnswer,
    navigateToQuestion,
    nextQuestion,
    previousQuestion,
    handleOptionSelect,
    handleTextAnswer,
    submitAttempt,
    getAttemptStatus,
  } = useTestAttempt(testId);

  const startInProgressRef = useRef(false);
  const hasInitializedRef = useRef(false);

  useEffect(() => {
    return () => {
      if (isTestActive) {
        const path = window.location.pathname;
        if (!path.includes('test-results')) {
          localStorage.getItem(`test_${testId}_attempt`);
        }
      }
    };
  }, [testId, isTestActive]);

  useEffect(() => {
    if (!showConfirmModal || test) {
      return;
    }

    const fetchTestDetails = async () => {
      try {
        const dashboard = await StudentService.getStudentDashboard();

        let foundTest = null;

        for (const group of dashboard.currentGroups) {
          const test = group.tests.find(t => t.id === parseInt(testId));
          if (test) {
            foundTest = test;
            break;
          }
        }

        if (foundTest) {
          setTest(foundTest);
        } else {
          toast.error(t('testPage.testNotFound', 'Test not found'));
          navigate('/student');
        }
      } catch (error) {
        toast.error(
          error.message ||
            t('testPage.failedToLoadTestDetails', 'Failed to load test details')
        );
        navigate('/student');
      }
    };

    fetchTestDetails();
  }, [testId, showConfirmModal, navigate, test, t]);

  const handleStartTest = useCallback(async () => {
    if (startInProgressRef.current) {
      return;
    }

    startInProgressRef.current = true;

    try {
      await startAttempt();
      setIsTestActive(true);
      setShowConfirmModal(false);
    } catch {
      navigate('/student');
    } finally {
      startInProgressRef.current = false;
    }
  }, [startAttempt, navigate]);

  useEffect(() => {
    if (hasInitializedRef.current || isTestActive || attemptId) {
      return;
    }

    hasInitializedRef.current = true;

    const initializeTest = async () => {
      const storedAttemptId = localStorage.getItem(`test_${testId}_attempt`);

      if (isPreConfirmed && test) {
        await handleStartTest();
      } else if (storedAttemptId) {
        await handleStartTest();
      }
    };

    initializeTest();
  }, [testId, isPreConfirmed, test, handleStartTest, isTestActive, attemptId]);

  const handleCancelTest = useCallback(() => {
    navigate('/student');
  }, [navigate]);

  const handleSubmitTest = useCallback(async () => {
    try {
      const result = await submitAttempt();

      navigate(`/student/test-results/${attemptId}`, {
        state: { result },
      });

      toast.success(
        t('testPage.testSubmittedSuccess', 'Test submitted successfully!')
      );
    } catch {}
  }, [attemptId, submitAttempt, navigate, t]);

  const handleTimeExpired = useCallback(async () => {
    toast.warning(
      t(
        'testPage.timeExpired',
        'Time has expired. Your test will be submitted automatically.'
      )
    );
    await handleSubmitTest();
  }, [handleSubmitTest, t]);

  const refreshTestStatus = useCallback(async () => {
    if (!attemptId) return;

    try {
      const status = await getAttemptStatus();
      setTestStatus(status);
    } catch {}
  }, [attemptId, getAttemptStatus]);

  useEffect(() => {
    if (isTestActive && attemptId) {
      const intervalId = window.setInterval(() => {
        refreshTestStatus();
      }, 30000);

      return () => window.clearInterval(intervalId);
    }
  }, [isTestActive, attemptId, refreshTestStatus]);

  const handleOpenSubmitModal = useCallback(async () => {
    await saveAnswer();
    await refreshTestStatus();
    setShowSubmitModal(true);
  }, [saveAnswer, refreshTestStatus]);

  const handleCloseSubmitModal = useCallback(() => {
    setShowSubmitModal(false);
  }, []);

  const augmentedCurrentQuestion = currentQuestion
    ? {
        ...currentQuestion,
        totalQuestions,
        onNext: nextQuestion,
        onPrevious: previousQuestion,
        onSubmit: handleOpenSubmitModal,
        loading,
      }
    : null;

  if (loading && !currentQuestion && !test) {
    return (
      <div className="flex justify-center items-center min-h-screen bg-gray-50">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-700" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {showConfirmModal && test && (
        <TestConfirmModal
          test={test}
          onConfirm={handleStartTest}
          onCancel={handleCancelTest}
        />
      )}

      {showSubmitModal && testStatus && (
        <TestSubmitModal
          test={test}
          answeredQuestions={totalAnsweredQuestions}
          totalQuestions={totalQuestions}
          questionStatuses={Array.from({ length: totalQuestions }, (_, i) => ({
            questionNumber: i + 1,
            isAnswered: !!answeredQuestionsMap[i + 1],
          }))}
          onClose={handleCloseSubmitModal}
          onConfirm={handleSubmitTest}
          submitting={submitting}
        />
      )}

      {!showConfirmModal && currentQuestion && (
        <div className="max-w-6xl mx-auto px-4 sm:px-6 py-6">
          <div className="bg-white shadow-md rounded-lg mb-6 overflow-hidden border border-gray-200">
            <div className="px-6 py-5 bg-gradient-to-r from-purple-600 to-indigo-700 text-white relative">
              <div className="absolute top-0 right-0 bg-white opacity-10 rounded-full w-24 h-24 -mt-8 -mr-8" />
              <div className="absolute bottom-0 left-0 bg-white opacity-10 rounded-full w-16 h-16 -mb-8 -ml-8" />

              <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between relative z-10">
                <div className="mb-4 sm:mb-0">
                  <h1 className="text-xl font-bold text-white flex items-center">
                    <svg
                      className="mr-2 h-5 w-5"
                      xmlns="http://www.w3.org/2000/svg"
                      fill="none"
                      viewBox="0 0 24 24"
                      stroke="currentColor"
                      aria-hidden="true"
                    >
                      <path d="M9 2a1 1 0 000 2h2a1 1 0 100-2H9z" />
                      <path
                        fillRule="evenodd"
                        d="M4 5a2 2 0 012-2 3 3 0 003 3h2a3 3 0 003-3 2 2 0 012 2v11a2 2 0 01-2 2H6a2 2 0 01-2-2V5zm3 4a1 1 0 000 2h.01a1 1 0 100-2H7zm3 0a1 1 0 000 2h3a1 1 0 100-2h-3zm-3 4a1 1 0 100 2h.01a1 1 0 100-2H7zm3 0a1 1 0 100 2h3a1 1 0 100-2h-3z"
                        clipRule="evenodd"
                      />
                    </svg>
                    {test?.title || t('testPage.exam', 'Exam')}
                  </h1>
                  <p className="text-sm text-purple-100 mt-1">
                    {t(
                      'testPage.questionProgress',
                      'Question {{current}} of {{total}}',
                      {
                        current: currentQuestionNumber,
                        total: totalQuestions,
                      }
                    )}
                  </p>
                </div>

                {timeRemaining !== null && (
                  <div className="bg-white/20 px-4 py-2 rounded-lg backdrop-blur-sm">
                    <TestTimer
                      seconds={timeRemaining}
                      onExpire={handleTimeExpired}
                    />
                  </div>
                )}
              </div>
            </div>

            <div className="px-6 py-3 border-b border-gray-200 bg-gray-50">
              <QuestionProgress
                answeredQuestions={totalAnsweredQuestions}
                totalQuestions={totalQuestions}
              />
            </div>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
            <div className="lg:col-span-1">
              <div className="bg-white shadow-md rounded-lg overflow-hidden border border-gray-200 sticky top-6">
                <div className="px-4 py-4 bg-gray-50 border-b border-gray-200">
                  <h2 className="text-lg font-medium text-gray-900 flex items-center">
                    <svg
                      className="mr-2 h-5 w-5 text-purple-600"
                      xmlns="http://www.w3.org/2000/svg"
                      viewBox="0 0 20 20"
                      fill="currentColor"
                      aria-hidden="true"
                    >
                      <path d="M5 3a2 2 0 00-2 2v2a2 2 0 002 2h2a2 2 0 002-2V5a2 2 0 00-2-2H5zM5 11a2 2 0 00-2 2v2a2 2 0 002 2h2a2 2 0 002-2v-2a2 2 0 00-2-2H5zM11 5a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V5zM11 13a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z" />
                    </svg>
                    {t('testPage.questions', 'Questions')}
                  </h2>
                </div>

                <div className="p-4">
                  <QuestionNumberGrid
                    totalQuestions={totalQuestions}
                    currentQuestionNumber={currentQuestionNumber}
                    answeredQuestionsMap={answeredQuestionsMap}
                    navigateToQuestion={navigateToQuestion}
                    loading={loading}
                    t={t}
                  />

                  <div className="bg-gray-50 p-4 rounded-lg border border-gray-200 mb-4">
                    <h3 className="text-base font-medium text-gray-900 flex items-center">
                      <svg
                        className="mr-2 h-5 w-5 text-purple-600"
                        xmlns="http://www.w3.org/2000/svg"
                        viewBox="0 0 20 20"
                        fill="currentColor"
                        aria-hidden="true"
                      >
                        <path
                          fillRule="evenodd"
                          d="M10 18a8 8 0 100-16 8 8 0 0016 0zm-8-3a1 1 0 00.867.5 1 1 0 11.731-1A3 3 0 0113 8a3.001 3.001 0 01-2 2.83V11a1 1 0 11-2 0v-1a1 1 0 011-1 1 1 0 100-2zm0 8a1 1 0 100-2 1 1 0 000 2z"
                          clipRule="evenodd"
                        />
                      </svg>
                      {t('testPage.readyToComplete', 'Ready to Complete?')}
                    </h3>
                    <p className="text-xs text-gray-500 mt-1 mb-3">
                      {t(
                        'testPage.submitForGrading',
                        "When you've answered all questions, submit your test for grading."
                      )}
                    </p>

                    <button
                      type="button"
                      className="w-full flex justify-center items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-purple-600 hover:bg-purple-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500 transition-colors"
                      onClick={handleOpenSubmitModal}
                      disabled={submitting || loading}
                      aria-label={t('testPage.submitTest', 'Submit Test')}
                    >
                      {submitting ? (
                        <>
                          <svg
                            className="animate-spin -ml-1 mr-2 h-4 w-4 text-white"
                            xmlns="http://www.w3.org/2000/svg"
                            fill="none"
                            viewBox="0 0 24 24"
                            aria-hidden="true"
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
                          {t('testPage.submitting', 'Submitting...')}
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
                          {t('testPage.submitTest', 'Submit Test')}
                        </>
                      )}
                    </button>
                  </div>

                  <div className="mt-4 pt-4 border-t border-gray-200">
                    <div className="text-sm text-gray-500 mb-2 flex items-center">
                      <div className="w-5 h-5 bg-green-50 border border-green-300 rounded mr-2 flex items-center justify-center">
                        <svg
                          className="h-3 w-3 text-green-600"
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
                      </div>
                      <span>
                        {t('testPage.answeredAndSaved', 'Answered & Saved')}
                      </span>
                    </div>
                    <div className="text-sm text-gray-500 mb-2 flex items-center">
                      <span className="inline-block w-5 h-5 bg-white border border-gray-300 rounded mr-2" />
                      <span>
                        {t('testPage.notAnsweredYet', 'Not answered yet')}
                      </span>
                    </div>
                    <div className="text-sm text-gray-500 mb-2 flex items-center">
                      <span className="inline-block w-5 h-5 bg-white border border-gray-300 rounded mr-2 ring-2 ring-purple-500" />
                      <span>
                        {t('testPage.currentQuestion', 'Current question')}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div className="lg:col-span-3">
              <TestQuestion
                currentQuestion={augmentedCurrentQuestion}
                currentQuestionNumber={currentQuestionNumber}
                currentAnswer={currentAnswer}
                handleOptionSelect={handleOptionSelect}
                handleTextAnswer={handleTextAnswer}
                t={t}
              />
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default TestPage;
