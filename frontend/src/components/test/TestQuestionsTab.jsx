import { useState, useEffect, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import InfoCard from '@/components/shared/InfoCard';
import QuestionManagement from '@/components/questions/QuestionManagement';
import { TestService } from '@/services/TestService';
import SharedPagination from '@/components/common/SharedPagination';
import { toast } from 'react-toastify';

const TestQuestionsTab = ({
  testId,
  onQuestionsUpdate,
  canEditTest,
  aiEvaluationEnabled,
}) => {
  const { t } = useTranslation();
  const [loading, setLoading] = useState(false);
  const [questionData, setQuestionData] = useState([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalItems, setTotalItems] = useState(0);
  const [pageSize] = useState(10);
  const [selectedDifficulty, setSelectedDifficulty] = useState(null);

  const fetchQuestions = useCallback(async () => {
    setLoading(true);
    try {
      const response = await TestService.getTestQuestions(
        testId,
        currentPage,
        pageSize,
        selectedDifficulty
      );
      setQuestionData(response.content);
      setTotalPages(response.totalPages);
      setTotalItems(response.totalElements);
    } catch {
      toast.error(
        t('testQuestionsTab.failedToLoad', 'Failed to load questions')
      );
    } finally {
      setLoading(false);
    }
  }, [testId, currentPage, pageSize, selectedDifficulty, t]);

  useEffect(() => {
    fetchQuestions();
  }, [fetchQuestions]);

  const handlePageChange = newPage => {
    setCurrentPage(newPage);
  };

  const handleDifficultyChange = difficulty => {
    setSelectedDifficulty(difficulty);
    setCurrentPage(0);
  };

  const handleQuestionUpdate = () => {
    fetchQuestions();
    if (onQuestionsUpdate) {
      onQuestionsUpdate();
    }
  };

  return (
    <InfoCard
      title={t('testQuestionsTab.title', 'Questions Management')}
      description={t(
        'testQuestionsTab.description',
        'Add, edit, and manage questions for this test'
      )}
    >
      <div className="mb-4 flex flex-wrap gap-2">
        <button
          onClick={() => handleDifficultyChange(null)}
          className={`px-3 py-1.5 text-sm font-medium rounded-lg ${
            selectedDifficulty === null
              ? 'bg-purple-600 text-white'
              : 'bg-gray-100 text-gray-800 hover:bg-gray-200'
          }`}
        >
          {t('testQuestionsTab.all', 'All')}
        </button>
        <button
          onClick={() => handleDifficultyChange('EASY')}
          className={`px-3 py-1.5 text-sm font-medium rounded-lg ${
            selectedDifficulty === 'EASY'
              ? 'bg-green-600 text-white'
              : 'bg-green-100 text-green-800 hover:bg-green-200'
          }`}
        >
          {t('testQuestionsTab.easy', 'Easy')}
        </button>
        <button
          onClick={() => handleDifficultyChange('MEDIUM')}
          className={`px-3 py-1.5 text-sm font-medium rounded-lg ${
            selectedDifficulty === 'MEDIUM'
              ? 'bg-yellow-600 text-white'
              : 'bg-yellow-100 text-yellow-800 hover:bg-yellow-200'
          }`}
        >
          {t('testQuestionsTab.medium', 'Medium')}
        </button>
        <button
          onClick={() => handleDifficultyChange('HARD')}
          className={`px-3 py-1.5 text-sm font-medium rounded-lg ${
            selectedDifficulty === 'HARD'
              ? 'bg-red-600 text-white'
              : 'bg-red-100 text-red-800 hover:bg-red-200'
          }`}
        >
          {t('testQuestionsTab.hard', 'Hard')}
        </button>
      </div>

      {loading ? (
        <div className="flex justify-center items-center py-12">
          <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-purple-600" />
          <p className="ml-3 text-gray-600">
            {t('testQuestionsTab.loadingQuestions', 'Loading questions...')}
          </p>
        </div>
      ) : (
        <>
          <QuestionManagement
            testId={testId}
            questions={questionData}
            onQuestionsUpdate={handleQuestionUpdate}
            canEditTest={canEditTest}
            aiEvaluationEnabled={aiEvaluationEnabled}
          />

          <div className="mt-4">
            <SharedPagination
              currentPage={currentPage}
              totalPages={totalPages}
              totalItems={totalItems}
              itemsPerPage={pageSize}
              onPageChange={handlePageChange}
              itemName={t('testQuestionsTab.questions', 'questions')}
            />
          </div>
        </>
      )}
    </InfoCard>
  );
};

export default TestQuestionsTab;
