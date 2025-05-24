import { useState, useCallback } from 'react';
import { toast } from 'react-toastify';
import { StudentService } from '@/services/StudentService';

export const useTestAttempt = testId => {
  const [attemptId, setAttemptId] = useState(null);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [currentQuestion, setCurrentQuestion] = useState(null);
  const [currentQuestionNumber, setCurrentQuestionNumber] = useState(1);
  const [totalQuestions, setTotalQuestions] = useState(0);
  const [timeRemaining, setTimeRemaining] = useState(null);
  const [currentAnswer, setCurrentAnswer] = useState(null);
  const [answeredQuestionsMap, setAnsweredQuestionsMap] = useState({});

  const hasAnswerContent = useCallback(answer => {
    if (!answer) return false;

    if (answer.selectedOptionIds && answer.selectedOptionIds.length > 0) {
      return true;
    }

    if (answer.answerText && answer.answerText.trim() !== '') {
      return true;
    }

    return false;
  }, []);

  const startAttempt = useCallback(async () => {
    try {
      setLoading(true);
      const startResponse = await StudentService.startAttempt(testId);
      setAttemptId(startResponse.attemptId);
      localStorage.setItem(`test_${testId}_attempt`, startResponse.attemptId);

      if (startResponse.question) {
        setCurrentQuestion(startResponse.question);
        setCurrentQuestionNumber(startResponse.currentQuestionNumber || 1);
        setTotalQuestions(startResponse.totalQuestions || 1);

        if (startResponse.currentAnswer) {
          setCurrentAnswer(startResponse.currentAnswer);

          if (hasAnswerContent(startResponse.currentAnswer)) {
            setAnsweredQuestionsMap(prev => ({
              ...prev,
              [startResponse.currentQuestionNumber]: true,
            }));
          }
        } else {
          setCurrentAnswer({
            questionId: startResponse.question.id,
            selectedOptionIds: [],
            answerText: '',
          });
        }
      }

      if (startResponse.timeRemainingSeconds) {
        setTimeRemaining(startResponse.timeRemainingSeconds);
      } else if (startResponse.duration) {
        setTimeRemaining(startResponse.duration * 60);
      }

      return startResponse;
    } catch (error) {
      toast.error(error.message || 'Failed to start test');
      throw error;
    } finally {
      setLoading(false);
    }
  }, [testId, hasAnswerContent]);

  const saveAnswer = useCallback(async () => {
    if (!currentAnswer || !attemptId) return;

    try {
      setSaving(true);
      await StudentService.saveAnswer(attemptId, currentAnswer);
    } catch {
      toast.error('Failed to save your answer');
    } finally {
      setSaving(false);
    }
  }, [attemptId, currentAnswer]);

  const navigateToQuestion = useCallback(
    async questionNumber => {
      try {
        setLoading(true);

        if (currentAnswer) {
          await saveAnswer();
        }

        const response = await StudentService.getQuestion(
          attemptId,
          questionNumber
        );

        setCurrentQuestion(response.question);
        setCurrentQuestionNumber(response.currentQuestionNumber);

        if (response.currentAnswer) {
          setCurrentAnswer(response.currentAnswer);

          if (hasAnswerContent(response.currentAnswer)) {
            setAnsweredQuestionsMap(prev => ({
              ...prev,
              [response.currentQuestionNumber]: true,
            }));
          }
        } else {
          setCurrentAnswer({
            questionId: response.question.id,
            selectedOptionIds: [],
            answerText: '',
          });
        }

        if (response.timeRemainingSeconds) {
          setTimeRemaining(response.timeRemainingSeconds);
        }

        return response;
      } catch (error) {
        toast.error(error.message || 'Failed to load question');
      } finally {
        setLoading(false);
      }
    },
    [attemptId, currentAnswer, hasAnswerContent, saveAnswer]
  );

  const nextQuestion = useCallback(async () => {
    if (currentQuestionNumber >= totalQuestions) return;

    try {
      setLoading(true);
      const response = await StudentService.nextQuestion(
        attemptId,
        currentQuestionNumber,
        currentAnswer
      );

      setCurrentQuestion(response.question);
      setCurrentQuestionNumber(response.currentQuestionNumber);

      if (response.currentAnswer) {
        setCurrentAnswer(response.currentAnswer);

        if (hasAnswerContent(response.currentAnswer)) {
          setAnsweredQuestionsMap(prev => ({
            ...prev,
            [response.currentQuestionNumber]: true,
          }));
        }
      } else {
        setCurrentAnswer({
          questionId: response.question.id,
          selectedOptionIds: [],
          answerText: '',
        });
      }

      if (response.timeRemainingSeconds) {
        setTimeRemaining(response.timeRemainingSeconds);
      }

      return response;
    } catch (error) {
      toast.error(error.message || 'Failed to load next question');
    } finally {
      setLoading(false);
    }
  }, [
    attemptId,
    currentAnswer,
    currentQuestionNumber,
    hasAnswerContent,
    totalQuestions,
  ]);

  const previousQuestion = useCallback(async () => {
    if (currentQuestionNumber <= 1) return;

    try {
      setLoading(true);
      const response = await StudentService.previousQuestion(
        attemptId,
        currentQuestionNumber,
        currentAnswer
      );

      setCurrentQuestion(response.question);
      setCurrentQuestionNumber(response.currentQuestionNumber);

      if (response.currentAnswer) {
        setCurrentAnswer(response.currentAnswer);

        if (hasAnswerContent(response.currentAnswer)) {
          setAnsweredQuestionsMap(prev => ({
            ...prev,
            [response.currentQuestionNumber]: true,
          }));
        }
      } else {
        setCurrentAnswer({
          questionId: response.question.id,
          selectedOptionIds: [],
          answerText: '',
        });
      }

      if (response.timeRemainingSeconds) {
        setTimeRemaining(response.timeRemainingSeconds);
      }

      return response;
    } catch (error) {
      toast.error(error.message || 'Failed to load previous question');
    } finally {
      setLoading(false);
    }
  }, [attemptId, currentAnswer, currentQuestionNumber, hasAnswerContent]);

  const handleOptionSelect = useCallback(
    (questionId, optionId, isMultiple) => {
      setCurrentAnswer(prev => {
        let selectedOptionIds;

        if (isMultiple) {
          selectedOptionIds = prev ? [...(prev.selectedOptionIds || [])] : [];

          if (selectedOptionIds.includes(optionId)) {
            selectedOptionIds = selectedOptionIds.filter(id => id !== optionId);
          } else {
            selectedOptionIds.push(optionId);
          }
        } else {
          selectedOptionIds = [optionId];
        }

        const newAnswer = {
          questionId,
          selectedOptionIds,
          answerText: prev ? prev.answerText || '' : '',
        };

        if (hasAnswerContent(newAnswer)) {
          setAnsweredQuestionsMap(prev => ({
            ...prev,
            [currentQuestionNumber]: true,
          }));
        } else {
          setAnsweredQuestionsMap(prev => {
            const updatedMap = { ...prev };
            delete updatedMap[currentQuestionNumber];
            return updatedMap;
          });
        }

        return newAnswer;
      });
    },
    [currentQuestionNumber, hasAnswerContent]
  );

  const handleTextAnswer = useCallback(
    (questionId, text) => {
      const newAnswer = {
        questionId,
        selectedOptionIds: [],
        answerText: text,
      };

      setCurrentAnswer(newAnswer);

      if (hasAnswerContent(newAnswer)) {
        setAnsweredQuestionsMap(prev => ({
          ...prev,
          [currentQuestionNumber]: true,
        }));
      } else {
        setAnsweredQuestionsMap(prev => {
          const updatedMap = { ...prev };
          delete updatedMap[currentQuestionNumber];
          return updatedMap;
        });
      }
    },
    [currentQuestionNumber, hasAnswerContent]
  );

  const submitAttempt = useCallback(async () => {
    try {
      if (!attemptId) return;

      setSubmitting(true);

      await saveAnswer();

      const result = await StudentService.completeAttempt(attemptId);

      localStorage.removeItem(`test_${testId}_attempt`);

      return result;
    } catch (error) {
      toast.error(error.message || 'Failed to submit test');
      throw error;
    } finally {
      setSubmitting(false);
    }
  }, [attemptId, saveAnswer, testId]);

  const getAttemptStatus = useCallback(async () => {
    if (!attemptId) return null;

    try {
      return await StudentService.getAttemptStatus(attemptId);
    } catch {
      return null;
    }
  }, [attemptId]);

  return {
    attemptId,
    loading,
    saving,
    submitting,
    currentQuestion,
    currentQuestionNumber,
    totalQuestions,
    timeRemaining,
    currentAnswer,
    answeredQuestionsMap,
    totalAnsweredQuestions: Object.keys(answeredQuestionsMap).length,
    startAttempt,
    saveAnswer,
    navigateToQuestion,
    nextQuestion,
    previousQuestion,
    handleOptionSelect,
    handleTextAnswer,
    submitAttempt,
    getAttemptStatus,
  };
};
