import React, {
  useState,
  useEffect,
  useCallback,
  useRef,
  useMemo,
} from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { StudentService } from '@/services/StudentService';
import { useAuth } from '@/context/AuthContext';
import { useTranslation } from 'react-i18next';
import StudentHeader from '@/components/header/StudentHeader';
import {
  ReviewNavigationProvider,
  QuestionView,
  SummaryView,
  ReviewHeader,
} from '@/components/review';

const AttemptReviewPage = () => {
  const { t } = useTranslation();
  const { id: attemptId } = useParams();
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const [review, setReview] = useState(null);
  const [loading, setLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(1);
  const [questionsPerPage] = useState(1);
  const [showSummary, setShowSummary] = useState(true);

  const fetchedRef = useRef(false);
  const fetchInProgressRef = useRef(false);

  const fetchAttemptReview = useCallback(async () => {
    if (fetchInProgressRef.current || !attemptId) return;

    try {
      setLoading(true);
      fetchInProgressRef.current = true;

      const reviewData = await StudentService.getAttemptReview(attemptId);
      setReview(reviewData);
      fetchedRef.current = true;
    } catch (error) {
      toast.error(
        error.message ||
          t('attemptReviewPage.loadError', 'Failed to load attempt review')
      );
      navigate('/student');
    } finally {
      setLoading(false);
      fetchInProgressRef.current = false;
    }
  }, [attemptId, navigate, t]);

  const handleRefresh = useCallback(() => {
    fetchedRef.current = false;
    fetchAttemptReview();
  }, [fetchAttemptReview]);

  useEffect(() => {
    fetchAttemptReview();
  }, [fetchAttemptReview]);

  const handleBackToDashboard = useCallback(() => {
    navigate('/student');
  }, [navigate]);

  const totalPages = useMemo(() => {
    return review?.questions
      ? Math.ceil(review.questions.length / questionsPerPage)
      : 0;
  }, [review, questionsPerPage]);

  const goToNextPage = useCallback(() => {
    if (currentPage < totalPages) {
      setCurrentPage(currentPage + 1);
      setShowSummary(false);
      window.scrollTo(0, 0);
    }
  }, [currentPage, totalPages]);

  const goToPrevPage = useCallback(() => {
    if (currentPage > 1) {
      setCurrentPage(currentPage - 1);
      window.scrollTo(0, 0);
    } else if (currentPage === 1 && !showSummary) {
      setShowSummary(true);
      window.scrollTo(0, 0);
    }
  }, [currentPage, showSummary]);

  const goToPage = useCallback(
    page => {
      if (page >= 1 && page <= totalPages) {
        setCurrentPage(page);
        setShowSummary(false);
        window.scrollTo(0, 0);
      }
    },
    [totalPages]
  );

  const toggleSummary = useCallback(() => {
    setShowSummary(!showSummary);
    window.scrollTo(0, 0);
  }, [showSummary]);

  useEffect(() => {
    const handleKeyDown = e => {
      if (e.key === 'ArrowRight') {
        if (showSummary) {
          setShowSummary(false);
          setCurrentPage(1);
        } else {
          goToNextPage();
        }
      } else if (e.key === 'ArrowLeft') {
        if (!showSummary && currentPage === 1) {
          setShowSummary(true);
        } else {
          goToPrevPage();
        }
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [currentPage, goToNextPage, goToPrevPage, showSummary, totalPages]);

  const getCurrentQuestions = useCallback(() => {
    if (!review?.questions) return [];

    const indexOfLastQuestion = currentPage * questionsPerPage;
    const indexOfFirstQuestion = indexOfLastQuestion - questionsPerPage;
    return review.questions.slice(indexOfFirstQuestion, indexOfLastQuestion);
  }, [review, currentPage, questionsPerPage]);

  const currentQuestions = useMemo(() => {
    return getCurrentQuestions();
  }, [getCurrentQuestions]);

  const percentage = useMemo(() => {
    if (!review) return 0;
    return Math.round((review.score / review.totalScore) * 100) || 0;
  }, [review]);

  const navigationContext = useMemo(
    () => ({
      goToNextPage,
      goToPrevPage,
      goToPage,
      toggleSummary,
      handleBackToDashboard,
      currentPage,
      totalPages,
      showSummary,
    }),
    [
      goToNextPage,
      goToPrevPage,
      goToPage,
      toggleSummary,
      handleBackToDashboard,
      currentPage,
      totalPages,
      showSummary,
    ]
  );

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen bg-gray-50">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-700" />
      </div>
    );
  }

  if (!review) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="bg-white p-8 rounded-lg shadow-md max-w-md w-full">
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
              d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0zm-8-3a1 1 0 00-.867.5 1 1 0 11-1.731-1A3 3 0 0113 8a3.001 3.001 0 01-2 2.83V11a1 1 0 11-2 0v-1a1 1 0 011-1 1 1 0 100-2zm0 8a1 1 0 100-2 1 1 0 000 2z"
            />
          </svg>
          <h3 className="mt-2 text-center text-lg font-medium text-gray-900">
            {t('attemptReviewPage.reviewNotAvailable', 'Review Not Available')}
          </h3>
          <p className="mt-1 text-center text-sm text-gray-500">
            {t(
              'attemptReviewPage.reviewUnavailableMessage',
              'The review for this attempt is not available or could not be loaded.'
            )}
          </p>
          <div className="mt-6">
            <button
              onClick={handleBackToDashboard}
              className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-purple-600 hover:bg-purple-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500"
            >
              {t('attemptReviewPage.returnToDashboard', 'Return to Dashboard')}
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <ReviewNavigationProvider value={navigationContext}>
      <div className="min-h-screen bg-gray-50">
        <div className="sticky top-0 z-50">
          <StudentHeader
            user={user}
            onLogout={logout}
            resetToCurrentSemester={() => navigate('/student')}
          />
        </div>

        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <ReviewHeader
            testTitle={review.testTitle}
            score={review.score}
            aiScore={review.aiScore}
            totalScore={review.totalScore}
            percentage={percentage}
            showSummary={showSummary}
            currentPage={currentPage}
            totalPages={totalPages}
            status={review.status}
            onToggleSummary={toggleSummary}
            onBackToDashboard={handleBackToDashboard}
            questions={review.questions}
          />

          <div className={showSummary ? 'h-4' : 'h-4'} />

          {showSummary ? (
            <SummaryView
              review={review}
              percentage={percentage}
              goToQuestion={index => {
                setCurrentPage(index + 1);
                setShowSummary(false);
              }}
              onRefresh={handleRefresh}
            />
          ) : (
            currentQuestions.map((question, index) => {
              const absoluteIndex =
                (currentPage - 1) * questionsPerPage + index;

              return (
                <QuestionView
                  key={question.submissionId}
                  question={question}
                  questionNumber={absoluteIndex + 1}
                  onNavigateNext={goToNextPage}
                  onNavigatePrev={goToPrevPage}
                  isLastQuestion={currentPage === totalPages}
                  isFirstQuestion={currentPage === 1}
                  status={review.status}
                />
              );
            })
          )}
        </div>
      </div>
    </ReviewNavigationProvider>
  );
};

export default React.memo(AttemptReviewPage);
