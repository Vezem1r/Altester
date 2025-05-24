import { memo } from 'react';
import { useTranslation } from 'react-i18next';
import { formatDate, formatDurationBetween } from '@/utils/formatters';
import QuestionSummaryItem from '@/components/review/QuestionSummaryItem';
import ScoreCircle from '@/components/review/ScoreCircle';
import RegradeRequestComponent from '@/components/review/RegradeRequestComponent';

const SummaryView = memo(({ review, percentage, goToQuestion, onRefresh }) => {
  const { t } = useTranslation();

  if (!review) return null;

  const isAiReviewed = review.status === 'AI_REVIEWED';
  const hasAiScore = review.aiScore !== undefined;
  const aiScorePercentage = hasAiScore
    ? Math.round((review.aiScore / review.totalScore) * 100)
    : 0;

  return (
    <div>
      <div className="bg-white shadow-md rounded-xl overflow-hidden mb-6 mt-4">
        <div className="bg-gradient-to-r from-purple-600 to-indigo-600 px-6 py-5 text-white">
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
                d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
              />
            </svg>
            {t('summaryView.testSummary', 'Test Summary')}
          </h2>
          <div className="flex items-center mt-1 relative z-10">
            <p className="text-purple-100 text-sm">{review.testTitle}</p>
            {review.status && (
              <span
                className={`ml-3 text-xs font-semibold px-2 py-1 rounded-full ${
                  isAiReviewed
                    ? 'bg-green-200 text-green-800'
                    : 'bg-blue-400 text-blue-900'
                }`}
              >
                {isAiReviewed
                  ? t('summaryView.aiReviewed', 'AI Reviewed')
                  : t('summaryView.teacherReviewed', 'Teacher Reviewed')}
              </span>
            )}
          </div>
        </div>

        <div className="px-6 py-5 bg-white">
          <div className="flex justify-center items-center gap-8">
            <div className="flex flex-col items-center">
              <ScoreCircle percentage={percentage} />
              <div className="text-center">
                <div className="text-xl font-bold text-gray-900">
                  {review.score} / {review.totalScore}
                </div>
                <div className="text-sm text-gray-500">
                  {t('summaryView.teacherScore', 'Teacher Score')}
                </div>
              </div>
            </div>

            {hasAiScore && (
              <div className="flex flex-col items-center">
                <ScoreCircle percentage={aiScorePercentage} />
                <div className="text-center">
                  <div className="text-xl font-bold text-gray-900">
                    {review.aiScore} / {review.totalScore}
                  </div>
                  <div className="text-sm text-gray-500">
                    {t('summaryView.aiScore', 'AI Score')}
                  </div>
                </div>
              </div>
            )}
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-6">
            <TestInfoCard review={review} />
            <QuestionSummaryCard
              questions={review.questions}
              goToQuestion={goToQuestion}
            />
          </div>

          <div className="mt-6 flex justify-center">
            <button
              onClick={() => goToQuestion(0)}
              className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-purple-600 hover:bg-purple-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500"
            >
              <svg
                className="mr-2 h-4 w-4"
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
              {t('summaryView.viewQuestions', 'View Questions')}
            </button>
          </div>
        </div>
      </div>

      {review.questions.some(q => q.aiGraded) && (
        <RegradeRequestComponent
          questions={review.questions}
          onRefresh={onRefresh}
        />
      )}
    </div>
  );
});

const TestInfoCard = memo(({ review }) => {
  const { t } = useTranslation();

  const hasAiScore = review.aiScore !== undefined;
  const aiScorePercentage = hasAiScore
    ? Math.round((review.aiScore / review.totalScore) * 100)
    : 0;
  const teacherScorePercentage =
    Math.round((review.score / review.totalScore) * 100) || 0;

  return (
    <div className="bg-gray-50 p-4 rounded-lg">
      <h3 className="text-sm font-medium text-gray-500 mb-3">
        {t('summaryView.testInformation', 'Test Information')}
      </h3>
      <div className="space-y-3">
        <div className="flex justify-between">
          <span className="text-sm text-gray-500">
            {t('summaryView.duration', 'Duration:')}
          </span>
          <span className="text-sm font-medium">
            {formatDurationBetween(review.startTime, review.endTime)}
          </span>
        </div>
        <div className="flex justify-between">
          <span className="text-sm text-gray-500">
            {t('summaryView.questions', 'Questions:')}
          </span>
          <span className="text-sm font-medium">{review.questions.length}</span>
        </div>
        <div className="flex justify-between">
          <span className="text-sm text-gray-500">
            {t('summaryView.startTime', 'Start Time:')}
          </span>
          <span className="text-sm font-medium">
            {formatDate(review.startTime)}
          </span>
        </div>
        <div className="flex justify-between">
          <span className="text-sm text-gray-500">
            {t('summaryView.endTime', 'End Time:')}
          </span>
          <span className="text-sm font-medium">
            {formatDate(review.endTime)}
          </span>
        </div>
        <div className="flex justify-between">
          <span className="text-sm text-gray-500">
            {t('summaryView.teacherScoreColon', 'Teacher Score:')}
          </span>
          <span className="text-sm font-medium">
            {review.score} / {review.totalScore} ({teacherScorePercentage}%)
          </span>
        </div>
        {hasAiScore && (
          <div className="flex justify-between">
            <span className="text-sm text-gray-500">
              {t('summaryView.aiScoreColon', 'AI Score:')}
            </span>
            <span className="text-sm font-medium">
              {review.aiScore} / {review.totalScore} ({aiScorePercentage}%)
            </span>
          </div>
        )}
      </div>
    </div>
  );
});

const QuestionSummaryCard = memo(({ questions, goToQuestion }) => {
  const { t } = useTranslation();

  return (
    <div className="bg-gray-50 p-4 rounded-lg">
      <h3 className="text-sm font-medium text-gray-500 mb-3">
        {t('summaryView.questionSummary', 'Question Summary')}
      </h3>
      <div className="space-y-2 max-h-60 overflow-y-auto">
        {questions.map((question, index) => (
          <QuestionSummaryItem
            key={question.questionId || question.submissionId}
            question={question}
            index={index}
            onClick={() => goToQuestion(index)}
          />
        ))}
      </div>
    </div>
  );
});

TestInfoCard.displayName = 'TestInfoCard';
QuestionSummaryCard.displayName = 'QuestionSummaryCard';
SummaryView.displayName = 'SummaryView';

export default SummaryView;
