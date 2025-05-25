import React, { memo, useMemo } from 'react';

const QuestionSummaryItem = memo(({ question, index, onClick }) => {
  const { questionText, score, aiScore, maxScore } = question;

  const displayScore = score === -1 && aiScore !== undefined ? aiScore : score;

  const statusInfo = useMemo(() => {
    const questionPercentage = Math.round((displayScore / maxScore) * 100) || 0;

    if (questionPercentage === 100) {
      return { color: 'bg-green-100 text-green-800' };
    } else if (questionPercentage > 0) {
      return { color: 'bg-yellow-100 text-yellow-800' };
    }
    return { color: 'bg-red-100 text-red-800' };
  }, [displayScore, maxScore]);

  const plainQuestionText = useMemo(() => {
    return questionText.replace(/<[^>]*>?/gm, '');
  }, [questionText]);

  return (
    <div
      className="flex items-center justify-between p-2 rounded-md hover:bg-gray-100 cursor-pointer"
      onClick={onClick}
    >
      <div className="flex items-center">
        <span className="w-6 h-6 rounded-full bg-purple-100 text-purple-800 flex items-center justify-center text-xs font-medium mr-2">
          {index + 1}
        </span>
        <span className="text-sm text-gray-700 line-clamp-1">
          {plainQuestionText}
        </span>
      </div>
      <span
        className={`ml-2 px-2 py-0.5 rounded-full text-xs font-medium ${statusInfo.color}`}
      >
        {displayScore}/{maxScore}
      </span>
    </div>
  );
});

QuestionSummaryItem.displayName = 'QuestionSummaryItem';

export default QuestionSummaryItem;
