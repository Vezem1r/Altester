import { useState, useEffect, memo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { useTranslation } from 'react-i18next';
import { TestService } from '@/services/TestService';
import { TeacherService } from '@/services/TeacherService';
import { useAuth } from '@/context/AuthContext';
import CreationLayout from '@/layouts/CreationLayout';
import FormSection from '@/components/form/FormSection';
import FormField from '@/components/form/FormField';
import StepIndicator from '@/components/form/StepIndicator';
import ActionButtons from '@/components/form/ActionButtons';
import SearchSelect from '@/components/form/SearchSelect';
import GroupCard from '@/components/cards/GroupCard';
import QuestionDistribution from '@/components/common/QuestionDistribution';
import {
  DocumentTextIcon,
  CalendarIcon,
  ClockIcon,
} from '@heroicons/react/outline';
import { motion } from 'framer-motion';

const TestFormPage = memo(() => {
  const { t } = useTranslation();
  const { id } = useParams();
  const navigate = useNavigate();
  const { userRole } = useAuth();
  const isEditing = !!id;

  const isTeacher = userRole === 'TEACHER';
  const isAdmin = userRole === 'ADMIN';

  const [currentStep, setCurrentStep] = useState(1);
  const [loading, setLoading] = useState(false);
  const [loadingTest, setLoadingTest] = useState(false);

  const [formData, setFormData] = useState({
    title: '',
    description: '',
    duration: 60,
    maxAttempts: null,
    easyQuestionsCount: 0,
    mediumQuestionsCount: 0,
    hardQuestionsCount: 0,
    easyQuestionScore: 5,
    mediumQuestionScore: 8,
    hardQuestionScore: 10,
    startTime: '',
    endTime: '',
    subjectId: '',
    groupIds: [],
    open: true,
  });

  const [subjects, setSubjects] = useState([]);
  const [groups, setGroups] = useState([]);
  const [errors, setErrors] = useState({});

  const [assignmentType, setAssignmentType] = useState(
    isTeacher ? 'groups' : 'subject'
  );
  const [activityFilter, setActivityFilter] = useState('active');

  const [subjectSearchQuery, setSubjectSearchQuery] = useState('');
  const [groupSearchQuery, setGroupSearchQuery] = useState('');
  const [subjectsLoading, setSubjectsLoading] = useState(false);
  const [groupsLoading, setGroupsLoading] = useState(false);

  const [subjectsPage, setSubjectsPage] = useState(0);
  const [groupsPage, setGroupsPage] = useState(0);
  const [subjectsTotalPages, setSubjectsTotalPages] = useState(1);
  const [groupsTotalPages, setGroupsTotalPages] = useState(1);
  const [subjectsTotalItems, setSubjectsTotalItems] = useState(0);
  const [groupsTotalItems, setGroupsTotalItems] = useState(0);

  const [preservedGroups, setPreservedGroups] = useState([]);
  const [showSelectedGroups, setShowSelectedGroups] = useState(true);

  const stepLabels = [
    t('testFormPageBasicInfo', 'Basic Info'),
    t('testFormPageTiming', 'Timing'),
    t('testFormPageAssignment', 'Assignment'),
  ];

  useEffect(() => {
    if (isEditing) {
      fetchTestDetails();
    }
  }, [id]);

  useEffect(() => {
    if (currentStep === 3) {
      if (!isTeacher && assignmentType === 'subject') {
        fetchSubjects();
      }
      fetchGroups();
    }
  }, [
    currentStep,
    subjectsPage,
    groupsPage,
    subjectSearchQuery,
    groupSearchQuery,
    activityFilter,
    assignmentType,
  ]);

  const fetchTestDetails = async () => {
    try {
      setLoadingTest(true);
      const testData = await TestService.getTestPreview(id);

      setFormData({
        ...testData,
        open: testData.open === undefined ? true : testData.open,
        groupIds: testData.associatedGroups
          ? testData.associatedGroups.map(g => g.id)
          : [],
        easyQuestionScore: testData.easyScore || 5,
        mediumQuestionScore: testData.mediumScore || 8,
        hardQuestionScore: testData.hardScore || 10,
      });

      if (isTeacher) {
        setAssignmentType('groups');
      } else {
        setAssignmentType(testData.subjectId ? 'subject' : 'groups');
      }
    } catch (error) {
      const errorRoute = isAdmin ? '/admin/tests' : '/teacher/tests';
      toast.error(
        t('testFormPageFailedToLoadTest', 'Failed to load test details')
      );
      navigate(errorRoute);
    } finally {
      setLoadingTest(false);
    }
  };

  const fetchSubjects = async () => {
    try {
      setSubjectsLoading(true);
      const response = await TestService.getSubjectsForTest(
        subjectsPage,
        subjectSearchQuery
      );
      setSubjects(response.content || []);
      setSubjectsTotalPages(response.totalPages || 1);
      setSubjectsTotalItems(response.totalElements || 0);
    } catch (error) {
      toast.error(
        t('testFormPageFailedToLoadSubjects', 'Failed to load subjects')
      );
    } finally {
      setSubjectsLoading(false);
    }
  };

  const fetchGroups = async () => {
    try {
      setGroupsLoading(true);
      let response;

      if (isTeacher) {
        response = await TeacherService.getTeacherGroups(
          groupsPage,
          12,
          groupSearchQuery,
          activityFilter
        );
      } else {
        response = await TestService.getGroupsForTest(
          groupsPage,
          groupSearchQuery,
          activityFilter
        );
      }

      const newGroups = response.content || [];
      setGroups(newGroups);

      const allGroupsMap = new Map();
      preservedGroups.forEach(g => allGroupsMap.set(g.id, g));
      newGroups.forEach(g => allGroupsMap.set(g.id, g));
      setPreservedGroups(Array.from(allGroupsMap.values()));

      setGroupsTotalPages(response.totalPages || 1);
      setGroupsTotalItems(response.totalElements || 0);
    } catch (error) {
      toast.error(t('testFormPageFailedToLoadGroups', 'Failed to load groups'));
    } finally {
      setGroupsLoading(false);
    }
  };

  const handleChange = e => {
    const { name, value, type, checked } = e.target;

    if (name.includes('QuestionsCount')) {
      const numValue = parseInt(value) || 0;
      setFormData({
        ...formData,
        [name]: numValue < 0 ? 0 : numValue,
      });
    } else {
      setFormData({
        ...formData,
        [name]: type === 'checkbox' ? checked : value,
      });
    }
  };

  const validateStep = step => {
    const newErrors = {};

    if (step === 1) {
      if (!formData.title.trim())
        newErrors.title = t('testFormPageTitleRequired', 'Title is required');
      if (formData.duration <= 0)
        newErrors.duration = t(
          'testFormPageDurationPositive',
          'Duration must be greater than 0'
        );

      const totalQuestions =
        parseInt(formData.easyQuestionsCount || 0) +
        parseInt(formData.mediumQuestionsCount || 0) +
        parseInt(formData.hardQuestionsCount || 0);

      if (totalQuestions <= 0) {
        newErrors.difficulty = t(
          'testFormPageNeedQuestions',
          'At least one question difficulty must have a count'
        );
      }

      if (
        formData.easyQuestionScore < 1 &&
        parseInt(formData.easyQuestionsCount || 0) > 0
      ) {
        newErrors.easyQuestionScore = t(
          'testFormPageEasyScoreMin',
          'Easy question score must be at least 1'
        );
      }

      if (
        formData.mediumQuestionScore < 1 &&
        parseInt(formData.mediumQuestionsCount || 0) > 0
      ) {
        newErrors.mediumQuestionScore = t(
          'testFormPageMediumScoreMin',
          'Medium question score must be at least 1'
        );
      }

      if (
        formData.hardQuestionScore < 1 &&
        parseInt(formData.hardQuestionsCount || 0) > 0
      ) {
        newErrors.hardQuestionScore = t(
          'testFormPageHardScoreMin',
          'Hard question score must be at least 1'
        );
      }
    }

    if (step === 2) {
      if (formData.startTime && formData.endTime) {
        const start = new Date(formData.startTime);
        const end = new Date(formData.endTime);
        if (end <= start)
          newErrors.endTime = t(
            'testFormPageEndTimeAfterStart',
            'End time must be after start time'
          );
      }
    }

    if (step === 3) {
      if (assignmentType === 'subject' && !formData.subjectId) {
        newErrors.subjectId = t(
          'testFormPageSelectSubjectRequired',
          'Please select a subject'
        );
      } else if (
        assignmentType === 'groups' &&
        formData.groupIds.length === 0
      ) {
        newErrors.groupIds = t(
          'testFormPageSelectGroup',
          'Please select at least one group'
        );
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async () => {
    if (!validateStep(1) || !validateStep(2) || !validateStep(3)) {
      toast.error(
        t('testFormPageFixErrors', 'Please fix all errors before submitting')
      );
      return;
    }

    try {
      setLoading(true);

      const testData = {
        ...formData,
        easyQuestionsCount: parseInt(formData.easyQuestionsCount) || 0,
        mediumQuestionsCount: parseInt(formData.mediumQuestionsCount) || 0,
        hardQuestionsCount: parseInt(formData.hardQuestionsCount) || 0,
        easyQuestionScore: parseInt(formData.easyQuestionScore) || 5,
        mediumQuestionScore: parseInt(formData.mediumQuestionScore) || 8,
        hardQuestionScore: parseInt(formData.hardQuestionScore) || 10,
      };

      if (assignmentType === 'groups') {
        delete testData.subjectId;
      } else {
        testData.groupIds = [];
      }

      const baseRoute = isAdmin ? '/admin/tests/' : '/teacher/tests/';

      if (isEditing) {
        await TestService.updateTest(id, testData);
        toast.success(
          t('testFormPageTestUpdated', 'Test updated successfully')
        );
        navigate(`${baseRoute}${id}`);
      } else {
        const result = await TestService.createTest(testData);
        toast.success(
          t('testFormPageTestCreated', 'Test created successfully')
        );
        navigate(`${baseRoute}${result.id}`);
      }
    } catch (error) {
      toast.error(
        error.message || t('testFormPageFailedToSave', 'Failed to save test')
      );
    } finally {
      setLoading(false);
    }
  };

  const formatDateTimeLocal = date => {
    if (!date) return '';
    const d = new Date(date);
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}T${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
  };

  const handleDateTimeChange = (name, value) => {
    setFormData({
      ...formData,
      [name]: value,
    });
  };

  const clearDateTime = name => {
    setFormData({
      ...formData,
      [name]: '',
    });
  };

  const getSubjectGroups = subjectId => {
    const subject = subjects.find(s => s.id === parseInt(subjectId));
    return subject?.groups || [];
  };

  const handleScoreChange = (difficulty, value) => {
    const intValue = parseInt(value) || 0;
    const scoreName = `${difficulty.toLowerCase()}QuestionScore`;
    setFormData({
      ...formData,
      [scoreName]: intValue < 1 ? 1 : intValue,
    });
  };

  const renderStepContent = () => {
    switch (currentStep) {
      case 1:
        return (
          <FormSection
            title={t('testFormPageBasicInformation', 'Basic Information')}
            description={t(
              'testFormPageBasicInfoDescription',
              'Enter the basic details about your test'
            )}
          >
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="md:col-span-2">
                <FormField
                  label={t('testFormPageTestTitle', 'Test Title')}
                  name="title"
                  value={formData.title}
                  onChange={handleChange}
                  error={errors.title}
                  required
                  placeholder={t(
                    'testFormPageEnterTestTitle',
                    'Enter test title'
                  )}
                />
              </div>

              <div className="md:col-span-2">
                <FormField
                  label={t('testFormPageDescription', 'Description')}
                  name="description"
                  type="textarea"
                  value={formData.description}
                  onChange={handleChange}
                  placeholder={t(
                    'testFormPageEnterDescription',
                    'Enter test description (optional)'
                  )}
                  rows={4}
                />
              </div>

              <FormField
                label={t('testFormPageDuration', 'Duration (minutes)')}
                name="duration"
                type="number"
                value={formData.duration}
                onChange={handleChange}
                error={errors.duration}
                required
                min="1"
              />

              <FormField
                label={t('testFormPageMaxAttempts', 'Maximum Attempts')}
                name="maxAttempts"
                type="number"
                value={formData.maxAttempts || ''}
                onChange={e =>
                  setFormData({
                    ...formData,
                    maxAttempts: e.target.value
                      ? parseInt(e.target.value)
                      : null,
                  })
                }
                placeholder={t(
                  'testFormPageLeaveEmptyUnlimited',
                  'Leave empty for unlimited'
                )}
                min="1"
              />
            </div>

            <QuestionDistribution
              easyCount={formData.easyQuestionsCount}
              mediumCount={formData.mediumQuestionsCount}
              hardCount={formData.hardQuestionsCount}
              onChange={handleChange}
              error={errors.difficulty}
            />

            <div className="mt-6 p-4 border border-gray-200 rounded-lg bg-gray-50">
              <h3 className="text-sm font-medium text-gray-700 mb-3">
                {t(
                  'testFormPageQuestionScoresByDifficulty',
                  'Question Scores by Difficulty'
                )}
              </h3>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div
                  className={
                    parseInt(formData.easyQuestionsCount || 0) > 0
                      ? ''
                      : 'opacity-50'
                  }
                >
                  <label className="text-sm font-medium text-gray-700 mb-1 flex items-center">
                    <span className="h-3 w-3 bg-green-500 rounded-full mr-2" />
                    {t('testFormPageEasyQuestionScore', 'Easy Question Score')}
                  </label>
                  <div className="flex items-center">
                    <input
                      type="number"
                      min="1"
                      value={formData.easyQuestionScore}
                      onChange={e => handleScoreChange('easy', e.target.value)}
                      disabled={
                        parseInt(formData.easyQuestionsCount || 0) === 0
                      }
                      className={`w-20 px-2 py-1 border rounded-md shadow-sm ${errors.easyQuestionScore ? 'border-red-300' : 'border-gray-300'} focus:ring-purple-500 focus:border-purple-500`}
                    />
                    <span className="ml-2 text-sm text-gray-500">
                      {t(
                        'testFormPagePointsPerQuestion',
                        'points per question'
                      )}
                    </span>
                  </div>
                  {errors.easyQuestionScore && (
                    <p className="mt-1 text-sm text-red-600">
                      {errors.easyQuestionScore}
                    </p>
                  )}
                </div>

                <div
                  className={
                    parseInt(formData.mediumQuestionsCount || 0) > 0
                      ? ''
                      : 'opacity-50'
                  }
                >
                  <label className="text-sm font-medium text-gray-700 mb-1 flex items-center">
                    <span className="h-3 w-3 bg-yellow-500 rounded-full mr-2" />
                    {t(
                      'testFormPageMediumQuestionScore',
                      'Medium Question Score'
                    )}
                  </label>
                  <div className="flex items-center">
                    <input
                      type="number"
                      min="1"
                      value={formData.mediumQuestionScore}
                      onChange={e =>
                        handleScoreChange('medium', e.target.value)
                      }
                      disabled={
                        parseInt(formData.mediumQuestionsCount || 0) === 0
                      }
                      className={`w-20 px-2 py-1 border rounded-md shadow-sm ${errors.mediumQuestionScore ? 'border-red-300' : 'border-gray-300'} focus:ring-purple-500 focus:border-purple-500`}
                    />
                    <span className="ml-2 text-sm text-gray-500">
                      {t(
                        'testFormPagePointsPerQuestion',
                        'points per question'
                      )}
                    </span>
                  </div>
                  {errors.mediumQuestionScore && (
                    <p className="mt-1 text-sm text-red-600">
                      {errors.mediumQuestionScore}
                    </p>
                  )}
                </div>

                <div
                  className={
                    parseInt(formData.hardQuestionsCount || 0) > 0
                      ? ''
                      : 'opacity-50'
                  }
                >
                  <label className="text-sm font-medium text-gray-700 mb-1 flex items-center">
                    <span className="h-3 w-3 bg-red-500 rounded-full mr-2" />
                    {t('testFormPageHardQuestionScore', 'Hard Question Score')}
                  </label>
                  <div className="flex items-center">
                    <input
                      type="number"
                      min="1"
                      value={formData.hardQuestionScore}
                      onChange={e => handleScoreChange('hard', e.target.value)}
                      disabled={
                        parseInt(formData.hardQuestionsCount || 0) === 0
                      }
                      className={`w-20 px-2 py-1 border rounded-md shadow-sm ${errors.hardQuestionScore ? 'border-red-300' : 'border-gray-300'} focus:ring-purple-500 focus:border-purple-500`}
                    />
                    <span className="ml-2 text-sm text-gray-500">
                      {t(
                        'testFormPagePointsPerQuestion',
                        'points per question'
                      )}
                    </span>
                  </div>
                  {errors.hardQuestionScore && (
                    <p className="mt-1 text-sm text-red-600">
                      {errors.hardQuestionScore}
                    </p>
                  )}
                </div>
              </div>

              <div className="mt-3 bg-blue-50 p-2 rounded-md border border-blue-100">
                <p className="text-xs text-blue-700">
                  {t(
                    'testFormPageScoreNoteMessage',
                    'All questions of the same difficulty level will have the same score. Individual question scores cannot be modified.'
                  )}
                </p>
              </div>
            </div>
          </FormSection>
        );

      case 2:
        return (
          <FormSection
            title={t('testFormPageTimeSettings', 'Time Settings')}
            description={t(
              'testFormPageTimeSettingsDescription',
              'Configure when students can take this test'
            )}
          >
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  {t('testFormPageStartTime', 'Start Time')}
                </label>
                <div className="flex items-center space-x-2">
                  <input
                    type="datetime-local"
                    value={formatDateTimeLocal(formData.startTime)}
                    onChange={e =>
                      handleDateTimeChange('startTime', e.target.value)
                    }
                    className="flex-1 px-4 py-2 border border-gray-300 rounded-lg shadow-sm focus:ring-purple-500 focus:border-purple-500"
                  />
                  {formData.startTime && (
                    <button
                      type="button"
                      onClick={() => clearDateTime('startTime')}
                      className="p-2 text-gray-500 hover:text-red-500 transition-colors"
                    >
                      <svg
                        className="w-5 h-5"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M6 18L18 6M6 6l12 12"
                        />
                      </svg>
                    </button>
                  )}
                </div>
                <p className="text-xs text-gray-500 mt-1">
                  {t(
                    'testFormPageLeaveBlankNoStartRestriction',
                    'Leave blank for no start time restriction'
                  )}
                </p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  {t('testFormPageEndTime', 'End Time')}
                </label>
                <div className="flex items-center space-x-2">
                  <input
                    type="datetime-local"
                    value={formatDateTimeLocal(formData.endTime)}
                    onChange={e =>
                      handleDateTimeChange('endTime', e.target.value)
                    }
                    className={`flex-1 px-4 py-2 border rounded-lg shadow-sm focus:ring-purple-500 focus:border-purple-500 ${
                      errors.endTime ? 'border-red-300' : 'border-gray-300'
                    }`}
                  />
                  {formData.endTime && (
                    <button
                      type="button"
                      onClick={() => clearDateTime('endTime')}
                      className="p-2 text-gray-500 hover:text-red-500 transition-colors"
                    >
                      <svg
                        className="w-5 h-5"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M6 18L18 6M6 6l12 12"
                        />
                      </svg>
                    </button>
                  )}
                </div>
                {errors.endTime && (
                  <p className="text-sm text-red-600 mt-1">{errors.endTime}</p>
                )}
                <p className="text-xs text-gray-500 mt-1">
                  {t(
                    'testFormPageLeaveBlankNoEndRestriction',
                    'Leave blank for no end time restriction'
                  )}
                </p>
              </div>
            </div>

            {(formData.startTime || formData.endTime) && (
              <div className="mt-6 p-4 bg-purple-50 border border-purple-200 rounded-lg">
                <h5 className="text-sm font-medium text-purple-800 mb-2">
                  {t(
                    'testFormPageCurrentTimeSettings',
                    'Current Time Settings'
                  )}
                </h5>
                <ul className="text-sm text-purple-700 space-y-1">
                  {formData.startTime && (
                    <li className="flex items-center">
                      <CalendarIcon className="w-4 h-4 mr-2" />
                      <span>
                        {t(
                          'testFormPageStudentsStartFrom',
                          'Students can start from'
                        )}
                        : {new Date(formData.startTime).toLocaleString()}
                      </span>
                    </li>
                  )}
                  {formData.endTime && (
                    <li className="flex items-center">
                      <ClockIcon className="w-4 h-4 mr-2" />
                      <span>
                        {t('testFormPageTestCloses', 'Test closes at')}:{' '}
                        {new Date(formData.endTime).toLocaleString()}
                      </span>
                    </li>
                  )}
                </ul>
              </div>
            )}
          </FormSection>
        );

      case 3:
        return (
          <FormSection
            title={t('testFormPageTestAssignment', 'Test Assignment')}
            description={t(
              'testFormPageTestAssignmentDescription',
              'Choose how to assign this test'
            )}
          >
            {!isTeacher && (
              <div className="mb-6">
                <label className="block text-sm font-semibold text-gray-800 mb-3">
                  {t('testFormPageAssignmentType', 'Assignment Type')}
                </label>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div
                    onClick={() => setAssignmentType('subject')}
                    className={`p-4 rounded-xl cursor-pointer transition-all duration-200 ${
                      assignmentType === 'subject'
                        ? 'bg-gradient-to-br from-purple-500 to-indigo-600 text-white shadow-xl transform scale-[1.02]'
                        : 'bg-white border-2 border-gray-200 hover:border-purple-300'
                    }`}
                  >
                    <div className="flex items-center">
                      <div
                        className={`w-12 h-12 rounded-full flex items-center justify-center mr-3 ${
                          assignmentType === 'subject'
                            ? 'bg-white/20'
                            : 'bg-purple-100'
                        }`}
                      >
                        <svg
                          className={`w-6 h-6 ${assignmentType === 'subject' ? 'text-white' : 'text-purple-600'}`}
                          fill="none"
                          stroke="currentColor"
                          viewBox="0 0 24 24"
                        >
                          <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={2}
                            d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"
                          />
                        </svg>
                      </div>
                      <div>
                        <h4
                          className={`font-semibold text-lg ${assignmentType === 'subject' ? 'text-white' : 'text-gray-900'}`}
                        >
                          {t(
                            'testFormPageAssignToSubject',
                            'Assign to Subject'
                          )}
                        </h4>
                        <p
                          className={`text-sm ${assignmentType === 'subject' ? 'text-purple-100' : 'text-gray-500'}`}
                        >
                          {t(
                            'testFormPageTestAvailableToAllGroups',
                            'Test will be available to all groups in subject'
                          )}
                        </p>
                      </div>
                    </div>
                  </div>

                  <div
                    onClick={() => setAssignmentType('groups')}
                    className={`p-4 rounded-xl cursor-pointer transition-all duration-200 ${
                      assignmentType === 'groups'
                        ? 'bg-gradient-to-br from-purple-500 to-indigo-600 text-white shadow-xl transform scale-[1.02]'
                        : 'bg-white border-2 border-gray-200 hover:border-purple-300'
                    }`}
                  >
                    <div className="flex items-center">
                      <div
                        className={`w-12 h-12 rounded-full flex items-center justify-center mr-3 ${
                          assignmentType === 'groups'
                            ? 'bg-white/20'
                            : 'bg-purple-100'
                        }`}
                      >
                        <svg
                          className={`w-6 h-6 ${assignmentType === 'groups' ? 'text-white' : 'text-purple-600'}`}
                          fill="none"
                          stroke="currentColor"
                          viewBox="0 0 24 24"
                        >
                          <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={2}
                            d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
                          />
                        </svg>
                      </div>
                      <div>
                        <h4
                          className={`font-semibold text-lg ${assignmentType === 'groups' ? 'text-white' : 'text-gray-900'}`}
                        >
                          {t('testFormPageAssignToGroups', 'Assign to Groups')}
                        </h4>
                        <p
                          className={`text-sm ${assignmentType === 'groups' ? 'text-purple-100' : 'text-gray-500'}`}
                        >
                          {t(
                            'testFormPageSelectSpecificGroups',
                            'Select specific groups for this test'
                          )}
                        </p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            )}

            {assignmentType === 'subject' && !isTeacher ? (
              <div>
                <SearchSelect
                  label={t('testFormPageSelectSubject', 'Select Subject')}
                  placeholder={t(
                    'testFormPageSearchSubjects',
                    'Search subjects...'
                  )}
                  items={subjects}
                  selectedItem={subjects.find(
                    s => s.id === parseInt(formData.subjectId)
                  )}
                  onSelect={subject =>
                    setFormData({ ...formData, subjectId: subject.id })
                  }
                  onSearchChange={setSubjectSearchQuery}
                  searchQuery={subjectSearchQuery}
                  loading={subjectsLoading}
                  error={errors.subjectId}
                  required
                  currentPage={subjectsPage}
                  totalPages={subjectsTotalPages}
                  totalItems={subjectsTotalItems}
                  onPageChange={setSubjectsPage}
                  renderItem={(subject, isSelected) => (
                    <div className="flex flex-col h-full justify-between">
                      <div className="mb-2">
                        <div className="flex items-center mb-2">
                          <div
                            className={`w-14 h-14 rounded-xl flex items-center justify-center font-bold text-sm mr-3 shadow-md transition-all duration-300 ${
                              isSelected
                                ? 'bg-gradient-to-br from-blue-100 to-blue-200 text-blue-700'
                                : 'bg-gradient-to-br from-blue-500 to-indigo-600 text-white'
                            }`}
                          >
                            {subject.shortName?.substring(0, 3).toUpperCase() ||
                              'SUB'}
                          </div>
                          <div className="flex-1">
                            <p
                              className={`font-semibold text-sm ${isSelected ? 'text-purple-600' : 'text-gray-900'}`}
                            >
                              {subject.name}
                            </p>
                            {subject.shortName && (
                              <p
                                className={`text-xs ${isSelected ? 'text-purple-500' : 'text-gray-600'}`}
                              >
                                {t('testFormPageCode', 'Code')}:{' '}
                                {subject.shortName}
                              </p>
                            )}
                          </div>
                        </div>
                      </div>
                      <div className="space-y-1">
                        <div
                          className={`flex items-center justify-between px-3 py-1.5 rounded-lg ${
                            isSelected ? 'bg-purple-500' : 'bg-gray-100'
                          }`}
                        >
                          <span
                            className={`text-xs ${isSelected ? 'text-purple-100' : 'text-gray-600'}`}
                          >
                            {t('testFormPageTotalGroups', 'Total Groups')}
                          </span>
                          <span
                            className={`text-sm font-bold ${isSelected ? 'text-white' : 'text-gray-700'}`}
                          >
                            {subject.groups?.length || 0}
                          </span>
                        </div>
                        {isSelected && subject.groups?.length > 0 && (
                          <div className="px-3 py-2 bg-white/10 rounded-lg">
                            <p className="text-xs text-white mb-1">
                              {t(
                                'testFormPageTestWillBeAssignedTo',
                                'This test will be assigned to:'
                              )}
                            </p>
                            <div className="flex flex-wrap gap-1">
                              {subject.groups.slice(0, 3).map((group, idx) => (
                                <span
                                  key={idx}
                                  className="text-xs bg-white/20 text-white px-2 py-0.5 rounded"
                                >
                                  {group.name}
                                </span>
                              ))}
                              {subject.groups.length > 3 && (
                                <span className="text-xs text-purple-100">
                                  +{subject.groups.length - 3}{' '}
                                  {t('testFormPageMore', 'more')}
                                </span>
                              )}
                            </div>
                          </div>
                        )}
                      </div>
                    </div>
                  )}
                />

                {formData.subjectId && (
                  <motion.div
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    className="mt-4 p-4 bg-gradient-to-br from-blue-50 to-indigo-50 border border-blue-200 rounded-xl"
                  >
                    <h4 className="text-sm font-semibold text-blue-900 mb-2">
                      {t(
                        'testFormPageGroupsReceiveTest',
                        'Groups that will receive this test'
                      )}
                    </h4>
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-2">
                      {getSubjectGroups(formData.subjectId).map(group => (
                        <div
                          key={group.id}
                          className="flex items-center bg-white px-3 py-2 rounded-lg border border-blue-200"
                        >
                          <div
                            className={`w-2 h-2 rounded-full mr-2 ${
                              group.status === 'Active'
                                ? 'bg-green-500'
                                : 'bg-yellow-500'
                            }`}
                          />
                          <span className="text-sm font-medium text-gray-800">
                            {group.name}
                          </span>
                          <span className="ml-auto text-xs text-gray-500">
                            {group.status}
                          </span>
                        </div>
                      ))}
                    </div>
                  </motion.div>
                )}
              </div>
            ) : (
              <>
                <div className="mb-6">
                  <div className="p-4 bg-gradient-to-r from-purple-50 to-indigo-50 rounded-xl border border-purple-200 shadow-sm">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div
                        onClick={() => {
                          setActivityFilter('active');
                          setGroupsPage(0);
                        }}
                        className={`p-3 rounded-lg cursor-pointer transition-all duration-200 ${
                          activityFilter === 'active'
                            ? 'bg-white shadow-lg transform scale-[1.02] border-2 border-purple-500'
                            : 'bg-white/50 hover:bg-white/70 border-2 border-transparent'
                        }`}
                      >
                        <div className="flex items-center">
                          <div className="w-10 h-10 rounded-full bg-green-100 flex items-center justify-center mr-3">
                            <div className="w-2.5 h-2.5 bg-green-500 rounded-full animate-pulse" />
                          </div>
                          <div>
                            <h4 className="font-semibold text-gray-900 text-sm">
                              {t('testFormPageActiveGroups', 'Active Groups')}
                            </h4>
                            <p className="text-xs text-gray-500">
                              {t(
                                'testFormPageCurrentlyRunning',
                                'Currently running'
                              )}
                            </p>
                          </div>
                        </div>
                      </div>

                      <div
                        onClick={() => {
                          setActivityFilter('future');
                          setGroupsPage(0);
                        }}
                        className={`p-3 rounded-lg cursor-pointer transition-all duration-200 ${
                          activityFilter === 'future'
                            ? 'bg-white shadow-lg transform scale-[1.02] border-2 border-purple-500'
                            : 'bg-white/50 hover:bg-white/70 border-2 border-transparent'
                        }`}
                      >
                        <div className="flex items-center">
                          <div className="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center mr-3">
                            <div className="w-2.5 h-2.5 bg-blue-500 rounded-full" />
                          </div>
                          <div>
                            <h4 className="font-semibold text-gray-900 text-sm">
                              {t('testFormPageFutureGroups', 'Future Groups')}
                            </h4>
                            <p className="text-xs text-gray-500">
                              {t('testFormPageStartingLater', 'Starting later')}
                            </p>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <SearchSelect
                  label={t('testFormPageSelectGroups', 'Select Groups')}
                  placeholder={t(
                    'testFormPageSearchGroups',
                    'Search groups...'
                  )}
                  items={groups}
                  multiple
                  selectedItems={preservedGroups.filter(g =>
                    formData.groupIds.includes(g.id)
                  )}
                  selectedIds={formData.groupIds}
                  onSelect={group => {
                    const newGroupIds = formData.groupIds.includes(group.id)
                      ? formData.groupIds.filter(id => id !== group.id)
                      : [...formData.groupIds, group.id];
                    setFormData({ ...formData, groupIds: newGroupIds });
                  }}
                  onSearchChange={setGroupSearchQuery}
                  searchQuery={groupSearchQuery}
                  loading={groupsLoading}
                  error={errors.groupIds}
                  required
                  currentPage={groupsPage}
                  totalPages={groupsTotalPages}
                  totalItems={groupsTotalItems}
                  onPageChange={setGroupsPage}
                  preservedSelectedItems={preservedGroups.filter(g =>
                    formData.groupIds.includes(g.id)
                  )}
                  showSelectedPanel={showSelectedGroups}
                  onCloseSelected={() =>
                    setShowSelectedGroups(!showSelectedGroups)
                  }
                  renderItem={(group, isSelected) => (
                    <GroupCard
                      group={group}
                      isSelected={isSelected}
                      showStatus
                    />
                  )}
                />
              </>
            )}
          </FormSection>
        );

      default:
        return null;
    }
  };

  const renderActionButtons = () => (
    <ActionButtons
      primaryAction={
        currentStep < 3
          ? {
              label: t('testFormPageNext', 'Next'),
              onClick: () => {
                if (validateStep(currentStep)) {
                  setCurrentStep(currentStep + 1);
                }
              },
            }
          : {
              label: isEditing
                ? t('testFormPageUpdateTest', 'Update Test')
                : t('testFormPageCreateTest', 'Create Test'),
              loadingLabel: isEditing
                ? t('testFormPageUpdating', 'Updating...')
                : t('testFormPageCreating', 'Creating...'),
              onClick: handleSubmit,
            }
      }
      secondaryAction={
        currentStep > 1
          ? {
              label: t('testFormPagePrevious', 'Previous'),
              onClick: () => setCurrentStep(currentStep - 1),
            }
          : null
      }
      cancelAction={{
        label: t('testFormPageCancel', 'Cancel'),
        onClick: () => {
          if (isEditing) {
            navigate(isAdmin ? `/admin/tests/${id}` : `/teacher/tests/${id}`);
          } else {
            navigate(isAdmin ? '/admin/tests' : '/teacher/tests');
          }
        },
      }}
      loading={loading}
      compact
    />
  );

  return (
    <CreationLayout
      title={
        isEditing
          ? t('testFormPageEditTest', 'Edit Test')
          : t('testFormPageCreateNewTest', 'Create New Test')
      }
      subtitle={
        isEditing
          ? t(
              'testFormPageUpdateTestSubtitle',
              'Update test details and configuration'
            )
          : t(
              'testFormPageCreateTestSubtitle',
              'Create a new test for your students'
            )
      }
      icon={<DocumentTextIcon className="h-8 w-8" />}
      headerActions={!loadingTest && renderActionButtons()}
    >
      {loadingTest ? (
        <div className="flex justify-center items-center py-16">
          <div className="relative">
            <div className="animate-spin rounded-full h-16 w-16 border-4 border-purple-600 border-t-transparent" />
            <div className="absolute inset-0 flex items-center justify-center">
              <div className="h-6 w-6 bg-purple-600 rounded-full animate-pulse" />
            </div>
          </div>
        </div>
      ) : (
        <>
          <StepIndicator
            currentStep={currentStep}
            totalSteps={3}
            stepLabels={stepLabels}
            onStepClick={setCurrentStep}
          />

          <div className="mt-8">{renderStepContent()}</div>
        </>
      )}
    </CreationLayout>
  );
});

TestFormPage.displayName = 'TestFormPage';

export default TestFormPage;
