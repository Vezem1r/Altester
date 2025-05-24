import React, { useMemo, useCallback } from 'react';
import { useTranslation } from 'react-i18next';

const QuestionNavigation = ({
  currentPage,
  totalPages,
  questions,
  answers,
  totalQuestions,
  onPageChange,
}) => {
  const { t } = useTranslation();
  const pages = useMemo(
    () => Array.from({ length: totalPages }, (_, i) => i + 1),
    [totalPages]
  );

  const getQuestionNumbers = useCallback(
    page => {
      const questionsPerPage = questions.length;
      const startNumber = (page - 1) * questionsPerPage + 1;
      return Array.from(
        {
          length:
            page === totalPages
              ? totalQuestions - startNumber + 1
              : questionsPerPage,
        },
        (_, i) => startNumber + i
      );
    },
    [questions.length, totalPages, totalQuestions]
  );

  const isQuestionAnswered = useCallback(
    questionNumber => {
      const questionIndex = (questionNumber - 1) % questions.length;

      if (questionIndex >= questions.length) {
        const allQuestionIds = Object.keys(answers).map(id => parseInt(id, 10));
        const targetQuestionId = allQuestionIds[questionNumber - 1];

        if (targetQuestionId && answers[targetQuestionId]) {
          return answers[targetQuestionId].answered;
        }
        return false;
      }

      const question = questions[questionIndex];

      if (!question || !answers[question.id]) {
        return false;
      }

      return answers[question.id].answered;
    },
    [questions, answers]
  );

  const allQuestionNumbers = useMemo(
    () => Array.from({ length: totalQuestions }, (_, i) => i + 1),
    [totalQuestions]
  );

  const visibleQuestions = useMemo(
    () => getQuestionNumbers(currentPage),
    [getQuestionNumbers, currentPage]
  );

  const handleQuestionClick = useCallback(
    questionNumber => {
      const questionsPerPage = questions.length;
      const targetPage = Math.ceil(questionNumber / questionsPerPage);
      if (targetPage !== currentPage) {
        onPageChange(targetPage);
      }
    },
    [questions.length, currentPage, onPageChange]
  );

  return (
    <div>
      <div className="mb-4">
        <p className="text-sm text-gray-500 mb-2">
          {t('questionNavigation.pages', 'Pages:')}
        </p>
        <div className="flex flex-wrap gap-2">
          {pages.map(page => (
            <button
              key={`page-${page}`}
              className={`w-8 h-8 rounded-full flex items-center justify-center text-sm ${
                page === currentPage
                  ? 'bg-purple-600 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
              onClick={() => onPageChange(page)}
              aria-label={t(
                'questionNavigation.goToPage',
                'Go to page {{page}}',
                { page }
              )}
              aria-current={page === currentPage ? 'page' : undefined}
            >
              {page}
            </button>
          ))}
        </div>
      </div>

      <div>
        <p className="text-sm text-gray-500 mb-2">
          {t('questionNavigation.questions', 'Questions:')}
        </p>
        <div className="grid grid-cols-3 sm:grid-cols-4 md:grid-cols-5 gap-2">
          {allQuestionNumbers.map(num => {
            const isAnswered = isQuestionAnswered(num);
            const isVisible = visibleQuestions.includes(num);

            return (
              <button
                key={`question-${num}`}
                className={`w-full h-9 rounded flex items-center justify-center text-sm ${
                  isAnswered
                    ? 'bg-green-100 text-green-800'
                    : 'bg-gray-100 text-gray-700'
                } ${isVisible ? 'ring-2 ring-purple-500' : ''}`}
                onClick={() => handleQuestionClick(num)}
                aria-label={t(
                  'questionNavigation.goToQuestion',
                  'Go to question {{num}}{{status}}',
                  {
                    num,
                    status: isAnswered
                      ? t('questionNavigation.answered', ' (answered)')
                      : t('questionNavigation.notAnswered', ' (not answered)'),
                  }
                )}
                aria-current={isVisible ? 'true' : undefined}
              >
                {num}
              </button>
            );
          })}
        </div>
      </div>
    </div>
  );
};

export default React.memo(QuestionNavigation);
