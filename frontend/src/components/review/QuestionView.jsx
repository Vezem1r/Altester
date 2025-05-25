import { memo, useMemo, useContext } from 'react';
import { useTranslation } from 'react-i18next';
import SimpleImageDisplay from '@/components/review/SimpleImageDisplay';
import QuestionHeader from '@/components/review/QuestionHeader';
import OptionsList from '@/components/review/OptionsList';
import TextAnswer from '@/components/review/TextAnswer';
import TeacherFeedback from '@/components/review/TeacherFeedback';
import MobileNavigation from '@/components/review/MobileNavigation';
import { ReviewNavigationContext } from '@/components/review/ReviewNavigationContext';

const QuestionView = memo(
  ({
    question,
    questionNumber,
    onNavigateNext: propNavigateNext,
    onNavigatePrev: propNavigatePrev,
    isLastQuestion: propIsLastQuestion,
    isFirstQuestion: propIsFirstQuestion,
    status,
  }) => {
    const { t } = useTranslation();
    const navigation = useContext(ReviewNavigationContext);

    const onNavigateNext = propNavigateNext || navigation.goToNextPage;
    const onNavigatePrev = propNavigatePrev || navigation.goToPrevPage;
    const isLastQuestion =
      propIsLastQuestion !== undefined
        ? propIsLastQuestion
        : navigation.currentPage === navigation.totalPages;
    const isFirstQuestion =
      propIsFirstQuestion !== undefined
        ? propIsFirstQuestion
        : navigation.currentPage === 1;

    const questionPercentage = useMemo(() => {
      return Math.round((question.score / question.maxScore) * 100) || 0;
    }, [question.score, question.maxScore]);

    return (
      <div className="bg-white shadow-lg rounded-xl overflow-hidden mb-8 transition-all duration-300 transform hover:shadow-xl mt-4">
        <QuestionHeader
          question={question}
          questionNumber={questionNumber}
          questionPercentage={questionPercentage}
          aiGraded={question.aiGraded}
        />

        {question.imagePath && (
          <div className="px-6 pt-4 flex justify-center">
            <div className="border rounded-lg overflow-hidden max-w-2xl shadow-sm">
              <SimpleImageDisplay
                imagePath={question.imagePath}
                alt={t('questionView.questionImage', 'Question')}
                className="max-w-full h-auto mx-auto"
              />
            </div>
          </div>
        )}

        {question.options && question.options.length > 0 && (
          <OptionsList options={question.options} />
        )}

        {question.studentAnswer !== null && (
          <TextAnswer
            studentAnswer={question.studentAnswer}
            score={question.score}
            aiScore={question.aiScore}
            maxScore={question.maxScore}
            status={status}
          />
        )}

        <TeacherFeedback
          feedback={question.teacherFeedback}
          aiFeedback={question.aiFeedback}
          status={status}
        />

        <MobileNavigation
          onPrevious={onNavigatePrev}
          onNext={onNavigateNext}
          isFirstQuestion={isFirstQuestion}
          isLastQuestion={isLastQuestion}
        />
      </div>
    );
  }
);

QuestionView.displayName = 'QuestionView';

export default QuestionView;
