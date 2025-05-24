import { useQuery, useMutation, useQueryClient } from 'react-query';
import { throttleRequest } from './apiUtils';
import { StudentService } from './StudentService';
import { TestService } from './TestService';
import { QuestionService } from './QuestionService';

export const useStudentDashboard = (searchQuery = '', groupId = null) => {
  const queryKey = ['studentDashboard', searchQuery, groupId];

  return useQuery(
    queryKey,
    () =>
      throttleRequest(`studentDashboard-${searchQuery}-${groupId}`, () =>
        StudentService.getStudentDashboard(searchQuery, groupId)
      ),
    {
      staleTime: 60000,
      keepPreviousData: true,
      refetchOnWindowFocus: false,
    }
  );
};

export const useAcademicHistory = (
  academicYear,
  semester,
  searchQuery = ''
) => {
  const queryKey = ['academicHistory', academicYear, semester, searchQuery];

  return useQuery(
    queryKey,
    () =>
      throttleRequest(
        `academicHistory-${academicYear}-${semester}-${searchQuery}`,
        () =>
          StudentService.getAcademicHistory(academicYear, semester, searchQuery)
      ),
    {
      staleTime: 300000,
      keepPreviousData: true,
      refetchOnWindowFocus: false,
    }
  );
};

export const useAvailableAcademicPeriods = () => {
  return useQuery(
    'availablePeriods',
    () =>
      throttleRequest('availablePeriods', () =>
        StudentService.getAvailableAcademicPeriods()
      ),
    {
      staleTime: 3600000,
      refetchOnWindowFocus: false,
    }
  );
};

export const useTestsList = (
  page = 0,
  size = 10,
  sort = 'id,desc',
  searchQuery = '',
  isActive = null
) => {
  const queryKey = ['tests', page, size, sort, searchQuery, isActive];

  return useQuery(
    queryKey,
    () =>
      throttleRequest(`tests-${searchQuery}-${isActive}-${page}`, () =>
        TestService.getAllTests(page, size, sort, searchQuery, isActive)
      ),
    {
      staleTime: 60000,
      keepPreviousData: true,
    }
  );
};

export const useTeacherTestsList = (
  page = 0,
  size = 10,
  sort = 'id,desc',
  searchQuery = '',
  isActive = null,
  allowTeacherEdit = null
) => {
  const queryKey = [
    'teacherTests',
    page,
    size,
    sort,
    searchQuery,
    isActive,
    allowTeacherEdit,
  ];

  return useQuery(
    queryKey,
    () =>
      throttleRequest(
        `teacherTests-${searchQuery}-${isActive}-${allowTeacherEdit}-${page}`,
        () =>
          TestService.getTeacherTests(
            page,
            size,
            sort,
            searchQuery,
            isActive,
            allowTeacherEdit
          )
      ),
    {
      staleTime: 60000,
      keepPreviousData: true,
    }
  );
};

export const useTestPreview = testId => {
  return useQuery(
    ['testPreview', testId],
    () =>
      throttleRequest(`testPreview-${testId}`, () =>
        TestService.getTestPreview(testId)
      ),
    {
      staleTime: 300000,
      enabled: !!testId,
    }
  );
};

export const useTestSummary = testId => {
  return useQuery(
    ['testSummary', testId],
    () =>
      throttleRequest(`testSummary-${testId}`, () =>
        TestService.getTestSummary(testId)
      ),
    {
      staleTime: 120000,
      enabled: !!testId,
    }
  );
};

export const useCreateTest = () => {
  const queryClient = useQueryClient();

  return useMutation(testData => TestService.createTest(testData), {
    onSuccess: () => {
      queryClient.invalidateQueries('tests');
      queryClient.invalidateQueries('teacherTests');
    },
  });
};

export const useUpdateTest = testId => {
  const queryClient = useQueryClient();

  return useMutation(testData => TestService.updateTest(testId, testData), {
    onSuccess: () => {
      queryClient.invalidateQueries('tests');
      queryClient.invalidateQueries('teacherTests');
      queryClient.invalidateQueries(['testPreview', testId]);
      queryClient.invalidateQueries(['testSummary', testId]);
    },
  });
};

export const useDeleteTest = () => {
  const queryClient = useQueryClient();

  return useMutation(testId => TestService.deleteTest(testId), {
    onSuccess: () => {
      queryClient.invalidateQueries('tests');
      queryClient.invalidateQueries('teacherTests');
    },
  });
};

export const useToggleTestActivity = () => {
  const queryClient = useQueryClient();

  return useMutation(testId => TestService.toggleTestActivity(testId), {
    onSuccess: (data, testId) => {
      queryClient.invalidateQueries('tests');
      queryClient.invalidateQueries('teacherTests');
      queryClient.invalidateQueries(['testPreview', testId]);
    },
  });
};

export const useQuestion = questionId => {
  return useQuery(
    ['question', questionId],
    () =>
      throttleRequest(`question-${questionId}`, () =>
        QuestionService.getQuestion(questionId)
      ),
    {
      staleTime: 300000,
      enabled: !!questionId,
    }
  );
};

export const useAddQuestion = testId => {
  const queryClient = useQueryClient();

  return useMutation(
    ({ questionData, image }) =>
      QuestionService.addQuestion(testId, questionData, image),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['testPreview', testId]);
      },
    }
  );
};

export const useUpdateQuestion = () => {
  const queryClient = useQueryClient();

  return useMutation(
    ({ questionId, questionData, image }) =>
      QuestionService.updateQuestion(questionId, questionData, image),
    {
      onSuccess: (data, { questionId }) => {
        queryClient.invalidateQueries(['question', questionId]);
        queryClient.invalidateQueries('testPreview');
      },
    }
  );
};

export const useDeleteQuestion = () => {
  const queryClient = useQueryClient();

  return useMutation(questionId => QuestionService.deleteQuestion(questionId), {
    onSuccess: () => {
      queryClient.invalidateQueries('testPreview');
    },
  });
};

export const useQuestionImageUrl = imagePath => {
  return {
    imageUrl: imagePath ? QuestionService.getQuestionImageUrl(imagePath) : null,
  };
};

export const useQuestionImage = imagePath => {
  return useQuery(
    ['questionImage', imagePath],
    () =>
      throttleRequest(`questionImage-${imagePath}`, () =>
        QuestionService.fetchQuestionImage(imagePath)
      ),
    {
      staleTime: 600000,
      enabled: !!imagePath,
    }
  );
};
