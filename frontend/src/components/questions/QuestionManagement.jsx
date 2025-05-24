import { useState, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import { toast } from 'react-toastify';
import { QuestionService } from '@/services/QuestionService';
import QuestionList from './QuestionList';
import QuestionForm, { QUESTION_DIFFICULTY } from './QuestionForm';

const QUESTION_TYPES = {
  TEXT_ONLY: 'TEXT_ONLY',
  IMAGE_ONLY: 'IMAGE_ONLY',
  TEXT_WITH_IMAGE: 'TEXT_WITH_IMAGE',
  MULTIPLE_CHOICE: 'MULTIPLE_CHOICE',
  IMAGE_WITH_MULTIPLE_CHOICE: 'IMAGE_WITH_MULTIPLE_CHOICE',
};

const QuestionManagement = ({
  testId,
  questions,
  onQuestionsUpdate,
  canEditTest,
}) => {
  const { t } = useTranslation();
  const [isAddingQuestion, setIsAddingQuestion] = useState(false);
  const [isEditingQuestion, setIsEditingQuestion] = useState(false);
  const [currentQuestion, setCurrentQuestion] = useState(null);
  const [expandedQuestion, setExpandedQuestion] = useState(null);

  const handleAddQuestion = useCallback(() => {
    setCurrentQuestion({
      questionText: '',
      score: 5,
      questionType: QUESTION_TYPES.TEXT_ONLY,
      difficulty: QUESTION_DIFFICULTY.MEDIUM,
      options: [],
    });
    setIsAddingQuestion(true);
    setIsEditingQuestion(false);
  }, []);

  const handleEditQuestion = useCallback(question => {
    setCurrentQuestion({
      ...question,
      removeImage: false,
    });
    setIsEditingQuestion(true);
    setIsAddingQuestion(false);
  }, []);

  const toggleQuestionDetails = useCallback(questionId => {
    setExpandedQuestion(prev => (prev === questionId ? null : questionId));
  }, []);

  const handleDeleteQuestion = useCallback(
    async questionId => {
      if (
        !window.confirm(
          t(
            'questionManagement.deleteConfirmation',
            'Are you sure you want to delete this question? This action cannot be undone.'
          )
        )
      ) {
        return;
      }

      try {
        await QuestionService.deleteQuestion(questionId);
        toast.success(
          t('questionManagement.deleteSuccess', 'Question deleted successfully')
        );

        if (onQuestionsUpdate) {
          onQuestionsUpdate();
        }
      } catch (error) {
        toast.error(
          error.message ||
            t('questionManagement.deleteError', 'Error deleting question')
        );
      }
    },
    [onQuestionsUpdate, t]
  );

  const closeQuestionForm = useCallback(() => {
    setIsAddingQuestion(false);
    setIsEditingQuestion(false);
    setCurrentQuestion(null);
  }, []);

  const handleQuestionSuccess = useCallback(() => {
    closeQuestionForm();
    if (onQuestionsUpdate) {
      onQuestionsUpdate();
    }
  }, [closeQuestionForm, onQuestionsUpdate]);

  const difficultyCount = {
    [QUESTION_DIFFICULTY.EASY]: questions.filter(
      q => q.difficulty === QUESTION_DIFFICULTY.EASY
    ).length,
    [QUESTION_DIFFICULTY.MEDIUM]: questions.filter(
      q => q.difficulty === QUESTION_DIFFICULTY.MEDIUM
    ).length,
    [QUESTION_DIFFICULTY.HARD]: questions.filter(
      q => q.difficulty === QUESTION_DIFFICULTY.HARD
    ).length,
    UNDEFINED: questions.filter(q => !q.difficulty).length,
  };

  return (
    <div className="mt-6">
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-lg font-semibold text-gray-900">
          {t('questionManagement.questions', 'Questions')} (
          {questions ? questions.length : 0})
        </h2>

        {canEditTest && (
          <button
            onClick={handleAddQuestion}
            className="inline-flex items-center px-3 py-2 border border-transparent text-sm leading-4 font-medium rounded-md shadow-sm text-white bg-purple-600 hover:bg-purple-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500"
            type="button"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              className="h-4 w-4 mr-1"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M12 4v16m8-8H4"
              />
            </svg>
            {t('questionManagement.addQuestion', 'Add Question')}
          </button>
        )}
      </div>

      {/* Question difficulty distribution */}
      <div className="mb-6 p-4 bg-gray-50 rounded-lg border border-gray-200">
        <h3 className="text-sm font-medium text-gray-700 mb-3">
          {t(
            'questionManagement.difficultyDistribution',
            'Question Difficulty Distribution'
          )}
        </h3>
        <div className="flex flex-wrap gap-3">
          <div className="flex items-center">
            <span className="h-3 w-3 bg-green-500 rounded-full mr-1" />
            <span className="text-sm text-gray-600">
              {t('questionManagement.easy', 'Easy')}:{' '}
              {difficultyCount[QUESTION_DIFFICULTY.EASY]}
            </span>
          </div>
          <div className="flex items-center">
            <span className="h-3 w-3 bg-yellow-500 rounded-full mr-1" />
            <span className="text-sm text-gray-600">
              {t('questionManagement.medium', 'Medium')}:{' '}
              {difficultyCount[QUESTION_DIFFICULTY.MEDIUM]}
            </span>
          </div>
          <div className="flex items-center">
            <span className="h-3 w-3 bg-red-500 rounded-full mr-1" />
            <span className="text-sm text-gray-600">
              {t('questionManagement.hard', 'Hard')}:{' '}
              {difficultyCount[QUESTION_DIFFICULTY.HARD]}
            </span>
          </div>
          {difficultyCount.UNDEFINED > 0 && (
            <div className="flex items-center">
              <span className="h-3 w-3 bg-gray-400 rounded-full mr-1" />
              <span className="text-sm text-gray-600">
                {t('questionManagement.unspecified', 'Unspecified')}:{' '}
                {difficultyCount.UNDEFINED}
              </span>
            </div>
          )}
        </div>
        <p className="mt-3 text-xs text-gray-500">
          {t(
            'questionManagement.scoreNote',
            "Note: It's highly recommended to use consistent scores for questions of the same difficulty level."
          )}
        </p>
      </div>

      <QuestionList
        questions={questions}
        canEditTest={canEditTest}
        expandedQuestion={expandedQuestion}
        onEdit={handleEditQuestion}
        onDelete={handleDeleteQuestion}
        onToggleDetails={toggleQuestionDetails}
      />

      {(isAddingQuestion || isEditingQuestion) && currentQuestion && (
        <QuestionForm
          testId={testId}
          question={currentQuestion}
          onCancel={closeQuestionForm}
          onSuccess={handleQuestionSuccess}
          isEditing={isEditingQuestion}
        />
      )}
    </div>
  );
};

export { QUESTION_TYPES };
export default QuestionManagement;
