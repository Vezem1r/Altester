import { BASE_API_URL, createAuthAxios, handleApiError } from './apiUtils';

const API_URL = BASE_API_URL;

export const QuestionService = {
  addQuestion: async (testId, questionData, image = null) => {
    try {
      const axiosInstance = createAuthAxios('multipart/form-data');
      const processedData = JSON.parse(JSON.stringify(questionData));

      if (
        (processedData.questionType === 'MULTIPLE_CHOICE' ||
          processedData.questionType === 'IMAGE_WITH_MULTIPLE_CHOICE') &&
        processedData.options
      ) {
        processedData.options = processedData.options.map(opt => {
          if ('isCorrect' in opt && !('correct' in opt)) {
            const { isCorrect, ...rest } = opt;
            return {
              ...rest,
              correct: Boolean(isCorrect),
            };
          }
          return opt;
        });

        const hasCorrectOption = processedData.options.some(
          opt => opt.correct === true
        );
        if (!hasCorrectOption && processedData.options.length > 0) {
          processedData.options[0].correct = true;
        }
      }

      const formData = new FormData();

      const questionBlob = new window.Blob([JSON.stringify(processedData)], {
        type: 'application/json',
      });
      formData.append('questionData', questionBlob);

      if (image) {
        formData.append('image', image);
      }

      const response = await axiosInstance.post(
        `${API_URL}/questions/tests/${testId}`,
        formData
      );

      return response.data;
    } catch (apiError) {
      throw handleApiError(apiError);
    }
  },

  updateQuestion: async (questionId, questionData, image = null) => {
    try {
      const axiosInstance = createAuthAxios('multipart/form-data');

      const processedData = JSON.parse(JSON.stringify(questionData));

      if (
        (processedData.questionType === 'MULTIPLE_CHOICE' ||
          processedData.questionType === 'IMAGE_WITH_MULTIPLE_CHOICE') &&
        processedData.options
      ) {
        processedData.options = processedData.options.map(opt => {
          if ('isCorrect' in opt && !('correct' in opt)) {
            const { isCorrect, ...rest } = opt;
            return {
              ...rest,
              correct: Boolean(isCorrect),
            };
          }
          return opt;
        });

        const hasCorrectOption = processedData.options.some(
          opt => opt.correct === true
        );
        if (!hasCorrectOption && processedData.options.length > 0) {
          processedData.options[0].correct = true;
        }
      }

      const formData = new FormData();

      formData.append(
        'questionData',
        new window.Blob([JSON.stringify(processedData)], {
          type: 'application/json',
        })
      );

      if (image) {
        formData.append('image', image);
      }

      const response = await axiosInstance.put(
        `${API_URL}/questions/${questionId}`,
        formData
      );

      return response.data;
    } catch (apiError) {
      throw handleApiError(apiError);
    }
  },

  deleteQuestion: async questionId => {
    try {
      const axiosInstance = createAuthAxios();

      const response = await axiosInstance.delete(
        `${API_URL}/questions/${questionId}`
      );

      return response.data;
    } catch (apiError) {
      throw handleApiError(apiError);
    }
  },

  getQuestion: async questionId => {
    try {
      const axiosInstance = createAuthAxios();

      const response = await axiosInstance.get(
        `${API_URL}/questions/${questionId}`
      );

      if (
        response.data &&
        (response.data.questionType === 'MULTIPLE_CHOICE' ||
          response.data.questionType === 'IMAGE_WITH_MULTIPLE_CHOICE') &&
        response.data.options
      ) {
        response.data.options = response.data.options.map(opt => {
          if ('correct' in opt) {
            const { correct, ...rest } = opt;
            return {
              ...rest,
              isCorrect: Boolean(correct),
            };
          }
          return opt;
        });
      }

      return response.data;
    } catch (apiError) {
      throw handleApiError(apiError);
    }
  },

  getQuestionImageUrl: imagePath => {
    if (!imagePath) return null;
    return `${API_URL}/question-images/${imagePath}`;
  },

  fetchQuestionImage: async imagePath => {
    if (!imagePath) return null;

    try {
      const axiosInstance = createAuthAxios();

      const response = await axiosInstance.get(
        `${API_URL}/question-images/${imagePath}`,
        {
          responseType: 'blob',
        }
      );

      return window.URL.createObjectURL(response.data);
    } catch (apiError) {
      throw handleApiError(apiError);
    }
  },
};
