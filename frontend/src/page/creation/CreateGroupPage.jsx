import { useState, useEffect, memo } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { useTranslation } from 'react-i18next';
import { AdminService } from '@/services/AdminService';
import CreationLayout from '@/layouts/CreationLayout';
import FormSection from '@/components/form/FormSection';
import FormField from '@/components/form/FormField';
import StepIndicator from '@/components/form/StepIndicator';
import ActionButtons from '@/components/form/ActionButtons';
import SearchSelect from '@/components/form/SearchSelect';
import PersonCard from '@/components/cards/PersonCard';
import SubjectCard from '@/components/cards/SubjectCard';
import SelectedItemsPanel from '@/components/common/SelectedItemsPanel';
import SemesterInfo from '@/components/common/SemesterInfo';
import { UserGroupIcon } from '@heroicons/react/outline';

const CreateGroupPage = memo(() => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [currentStep, setCurrentStep] = useState(1);
  const [loading, setLoading] = useState(false);

  const [formData, setFormData] = useState({
    groupName: '',
    teacherId: '',
    semester: '',
    academicYear: new Date().getFullYear(),
    active: true,
    subjectId: null,
  });

  const [teachers, setTeachers] = useState([]);
  const [subjects, setSubjects] = useState([]);
  const [students, setStudents] = useState([]);
  const [selectedStudents, setSelectedStudents] = useState(new Set());
  const [createdGroupId, setCreatedGroupId] = useState(null);

  const [teacherSearchQuery, setTeacherSearchQuery] = useState('');
  const [subjectSearchQuery, setSubjectSearchQuery] = useState('');
  const [studentSearchQuery, setStudentSearchQuery] = useState('');

  const [loadingTeachers, setLoadingTeachers] = useState(false);
  const [loadingSubjects, setLoadingSubjects] = useState(false);
  const [loadingStudents, setLoadingStudents] = useState(false);

  const [teachersPage, setTeachersPage] = useState(0);
  const [subjectsPage, setSubjectsPage] = useState(0);
  const [studentsPage, setStudentsPage] = useState(0);

  const [teachersTotalPages, setTeachersTotalPages] = useState(1);
  const [subjectsTotalPages, setSubjectsTotalPages] = useState(1);
  const [studentsTotalPages, setStudentsTotalPages] = useState(1);

  const [teachersTotalItems, setTeachersTotalItems] = useState(0);
  const [subjectsTotalItems, setSubjectsTotalItems] = useState(0);
  const [studentsTotalItems, setStudentsTotalItems] = useState(0);

  const [preservedTeachers, setPreservedTeachers] = useState([]);
  const [preservedStudents, setPreservedStudents] = useState([]);

  const [errors, setErrors] = useState({});
  const [showSelectedStudents, setShowSelectedStudents] = useState(true);

  const stepLabels = [
    t('createGroupPageBasicInfo', 'Basic Info'),
    t('createGroupPageTeacher', 'Teacher'),
    t('createGroupPageSubject', 'Subject'),
    t('createGroupPageStudents', 'Students'),
  ];

  const [currentSemester, setCurrentSemester] = useState('WINTER');
  const [currentAcademicYear, setCurrentAcademicYear] = useState(
    new Date().getFullYear()
  );

  useEffect(() => {
    calculateCurrentSemester();
  }, []);

  useEffect(() => {
    if (currentStep === 2) {
      fetchTeachers();
    }
  }, [currentStep, teachersPage, teacherSearchQuery]);

  useEffect(() => {
    if (currentStep === 3) {
      fetchSubjects();
    }
  }, [currentStep, subjectsPage, subjectSearchQuery]);

  useEffect(() => {
    if (currentStep === 4 && createdGroupId) {
      fetchStudents();
    }
  }, [currentStep, studentsPage, studentSearchQuery, createdGroupId]);

  const calculateCurrentSemester = () => {
    const now = new Date();
    const month = now.getMonth() + 1;
    const day = now.getDate();

    let semester;
    if (
      (month === 2 && day >= 17) ||
      (month > 2 && month <= 6) ||
      (month === 7 && day <= 31)
    ) {
      semester = 'SUMMER';
    } else {
      semester = 'WINTER';
    }

    const year = now.getFullYear();
    const academicYear = month >= 9 ? year : year - 1;

    setCurrentSemester(semester);
    setCurrentAcademicYear(academicYear);

    setFormData(prev => ({
      ...prev,
      semester,
      academicYear,
    }));
  };

  const fetchTeachers = async () => {
    try {
      setLoadingTeachers(true);
      const response = await AdminService.getGroupTeachers(
        teachersPage,
        teacherSearchQuery,
        20
      );
      const newTeachers = response.content || [];
      setTeachers(newTeachers);

      const allTeachersMap = new Map();
      preservedTeachers.forEach(t => allTeachersMap.set(t.userId, t));
      newTeachers.forEach(t => allTeachersMap.set(t.userId, t));
      setPreservedTeachers(Array.from(allTeachersMap.values()));

      setTeachersTotalPages(response.totalPages || 1);
      setTeachersTotalItems(response.totalElements || 0);
    } catch (error) {
      toast.error(
        t('createGroupPageFailedToLoadTeachers', 'Failed to load teachers')
      );
    } finally {
      setLoadingTeachers(false);
    }
  };

  const fetchSubjects = async () => {
    try {
      setLoadingSubjects(true);
      const response = await AdminService.getAllSubjects(
        subjectsPage,
        subjectSearchQuery
      );
      setSubjects(response.content || []);
      setSubjectsTotalPages(response.totalPages || 1);
      setSubjectsTotalItems(response.totalElements || 0);
    } catch (error) {
      toast.error(
        t('createGroupPageFailedToLoadSubjects', 'Failed to load subjects')
      );
    } finally {
      setLoadingSubjects(false);
    }
  };

  const fetchStudents = async () => {
    try {
      setLoadingStudents(true);
      const response = await AdminService.getGroupStudents(
        studentsPage,
        createdGroupId,
        studentSearchQuery,
        false,
        20
      );

      let newStudents = [];
      if (response.availableStudents) {
        newStudents = response.availableStudents.content || [];
        setStudentsTotalPages(response.availableStudents.totalPages || 1);
        setStudentsTotalItems(response.availableStudents.totalElements || 0);
      } else {
        newStudents = response.content || [];
        setStudentsTotalPages(response.totalPages || 1);
        setStudentsTotalItems(response.totalElements || 0);
      }

      setStudents(newStudents);

      const allStudentsMap = new Map();
      preservedStudents.forEach(s => allStudentsMap.set(s.userId, s));
      newStudents.forEach(s => allStudentsMap.set(s.userId, s));
      setPreservedStudents(Array.from(allStudentsMap.values()));
    } catch (error) {
      toast.error(
        t('createGroupPageFailedToLoadStudents', 'Failed to load students')
      );
    } finally {
      setLoadingStudents(false);
    }
  };

  const handleChange = e => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value,
    });
  };

  const validateStep = step => {
    const newErrors = {};

    if (step === 1) {
      if (!formData.groupName.trim())
        newErrors.groupName = t(
          'createGroupPageGroupNameRequired',
          'Group name is required'
        );
      if (!formData.semester)
        newErrors.semester = t(
          'createGroupPageSemesterRequired',
          'Semester is required'
        );
      if (!formData.academicYear)
        newErrors.academicYear = t(
          'createGroupPageAcademicYearRequired',
          'Academic year is required'
        );
      else if (formData.academicYear < 2000 || formData.academicYear > 2100) {
        newErrors.academicYear = t(
          'createGroupPageInvalidYear',
          'Please enter a valid year (2000-2100)'
        );
      }
    }

    if (step === 2) {
      if (!formData.teacherId)
        newErrors.teacherId = t(
          'createGroupPageSelectTeacher',
          'Please select a teacher'
        );
    }

    if (step === 3) {
      if (!formData.subjectId)
        newErrors.subjectId = t(
          'createGroupPageSelectSubject',
          'Please select a subject'
        );
    }

    if (step === 4) {
      if (selectedStudents.size === 0)
        newErrors.students = t(
          'createGroupPageSelectStudents',
          'Please select at least one student'
        );
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const createGroupWithSubject = async () => {
    try {
      setLoading(true);

      const isActive =
        formData.semester === currentSemester &&
        Number(formData.academicYear) === currentAcademicYear;

      const groupData = {
        groupName: formData.groupName.trim(),
        teacherId: formData.teacherId,
        semester: formData.semester,
        academicYear: Number(formData.academicYear),
        active: isActive,
        subjectId: formData.subjectId,
      };

      const groupId = await AdminService.createGroup(groupData);
      setCreatedGroupId(groupId);

      toast.success(
        t('createGroupPageGroupCreatedSuccess', 'Group created successfully')
      );
      setCurrentStep(4);
    } catch (error) {
      toast.error(
        error.message ||
          t('createGroupPageErrorCreatingGroup', 'Error creating group')
      );
    } finally {
      setLoading(false);
    }
  };

  const finalizeGroup = async () => {
    try {
      setLoading(true);

      const updateData = {
        groupName: formData.groupName.trim(),
        teacherId: formData.teacherId,
        studentsIds: Array.from(selectedStudents),
        semester: formData.semester,
        academicYear: Number(formData.academicYear),
        active: formData.active,
      };

      await AdminService.updateGroup(createdGroupId, updateData);
      toast.success(
        t(
          'createGroupPageStudentsAddedSuccess',
          'Students added to group successfully'
        )
      );
      navigate('/admin/groups');
    } catch (error) {
      toast.error(
        error.message ||
          t('createGroupPageErrorFinalizingGroup', 'Error finalizing group')
      );
    } finally {
      setLoading(false);
    }
  };

  const renderStepContent = () => {
    switch (currentStep) {
      case 1:
        return (
          <FormSection
            title={t('createGroupPageBasicInformation', 'Basic Information')}
            description={t(
              'createGroupPageBasicInfoDescription',
              'Enter the basic group details'
            )}
          >
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="md:col-span-2">
                <FormField
                  label={t('createGroupPageGroupName', 'Group Name')}
                  name="groupName"
                  value={formData.groupName}
                  onChange={handleChange}
                  error={errors.groupName}
                  required
                  placeholder={t(
                    'createGroupPageEnterGroupName',
                    'Enter group name'
                  )}
                />
              </div>

              <FormField
                label={t('createGroupPageSemester', 'Semester')}
                name="semester"
                type="select"
                value={formData.semester}
                onChange={handleChange}
                error={errors.semester}
                required
                options={[
                  {
                    value: '',
                    label: t(
                      'createGroupPageSelectSemester',
                      'Select semester'
                    ),
                  },
                  {
                    value: 'WINTER',
                    label: t(
                      'createGroupPageWinterSemester',
                      'Winter Semester'
                    ),
                  },
                  {
                    value: 'SUMMER',
                    label: t(
                      'createGroupPageSummerSemester',
                      'Summer Semester'
                    ),
                  },
                ]}
              />

              <FormField
                label={t('createGroupPageAcademicYear', 'Academic Year')}
                name="academicYear"
                type="number"
                value={formData.academicYear}
                onChange={handleChange}
                error={errors.academicYear}
                required
                min="2000"
                max="2100"
                description={t(
                  'createGroupPageAcademicYearDescription',
                  'Enter the first year (e.g., 2024 for 2024-2025)'
                )}
              />
            </div>

            <SemesterInfo
              semester={formData.semester}
              academicYear={formData.academicYear}
              currentSemester={currentSemester}
              currentAcademicYear={currentAcademicYear}
            />
          </FormSection>
        );

      case 2:
        return (
          <FormSection
            title={t('createGroupPageSelectTeacherTitle', 'Select Teacher')}
            description={t(
              'createGroupPageSelectTeacherDescription',
              'Choose a teacher to manage this group'
            )}
          >
            <SearchSelect
              label={t('createGroupPageTeacher', 'Teacher')}
              placeholder={t(
                'createGroupPageSearchTeachers',
                'Search teachers...'
              )}
              items={teachers}
              selectedItem={teachers.find(
                t => t.userId.toString() === formData.teacherId
              )}
              onSelect={teacher =>
                setFormData({
                  ...formData,
                  teacherId: teacher.userId.toString(),
                })
              }
              onSearchChange={setTeacherSearchQuery}
              searchQuery={teacherSearchQuery}
              loading={loadingTeachers}
              error={errors.teacherId}
              required
              currentPage={teachersPage}
              totalPages={teachersTotalPages}
              totalItems={teachersTotalItems}
              onPageChange={setTeachersPage}
              itemsPerPage={20}
              renderItem={(teacher, isSelected) => (
                <PersonCard
                  person={teacher}
                  isSelected={isSelected}
                  showUsername
                  showId={false}
                  selectedGradientFrom="purple-600"
                  selectedGradientTo="indigo-700"
                  selectedTextColor="white"
                />
              )}
              valueKey="userId"
              labelKey="name"
            />
          </FormSection>
        );

      case 3:
        return (
          <FormSection
            title={t('createGroupPageSelectSubjectTitle', 'Select Subject')}
            description={t(
              'createGroupPageSelectSubjectDescription',
              'Choose a subject for this group'
            )}
          >
            <SearchSelect
              label={t('createGroupPageSubject', 'Subject')}
              placeholder={t(
                'createGroupPageSearchSubjects',
                'Search subjects...'
              )}
              items={subjects}
              selectedItem={subjects.find(s => s.id === formData.subjectId)}
              onSelect={subject =>
                setFormData({ ...formData, subjectId: subject.id })
              }
              onSearchChange={setSubjectSearchQuery}
              searchQuery={subjectSearchQuery}
              loading={loadingSubjects}
              error={errors.subjectId}
              required
              currentPage={subjectsPage}
              totalPages={subjectsTotalPages}
              totalItems={subjectsTotalItems}
              onPageChange={setSubjectsPage}
              renderItem={(subject, isSelected) => (
                <SubjectCard
                  subject={subject}
                  isSelected={isSelected}
                  showGroups
                />
              )}
            />
          </FormSection>
        );

      case 4:
        return (
          <FormSection
            title={t('createGroupPageAddStudents', 'Add Students')}
            description={t(
              'createGroupPageAddStudentsDescription',
              'Select students to add to this group'
            )}
          >
            <div className="mb-6">
              <div className="bg-gradient-to-r from-purple-600 to-indigo-600 text-white rounded-2xl shadow-xl p-6">
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="text-xl font-bold">
                      {t(
                        'createGroupPageSelectedStudents',
                        'Selected Students'
                      )}
                    </h3>
                    <p className="text-purple-100 mt-1">
                      {selectedStudents.size}{' '}
                      {selectedStudents.size !== 1
                        ? t(
                            'createGroupPageStudentsSelected',
                            'students selected'
                          )
                        : t(
                            'createGroupPageStudentSelected',
                            'student selected'
                          )}
                    </p>
                  </div>
                  <div className="flex items-center gap-4">
                    {!showSelectedStudents && selectedStudents.size > 0 && (
                      <button
                        type="button"
                        onClick={() => setShowSelectedStudents(true)}
                        className="bg-white/20 backdrop-blur-sm rounded-lg px-4 py-2 text-sm font-medium hover:bg-white/30 transition-colors"
                      >
                        {t('createGroupPageShowSelected', 'Show Selected')}
                      </button>
                    )}
                    <div className="bg-white/20 backdrop-blur-sm rounded-2xl px-6 py-3">
                      <div className="text-3xl font-bold">
                        {selectedStudents.size}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <SelectedItemsPanel
              items={preservedStudents}
              selectedIds={Array.from(selectedStudents)}
              label={t('createGroupPageStudents', 'Students')}
              onRemove={student => {
                const newSelected = new Set(selectedStudents);
                newSelected.delete(student.userId);
                setSelectedStudents(newSelected);
              }}
              onClearAll={() => setSelectedStudents(new Set())}
              onToggleVisibility={() => setShowSelectedStudents(false)}
              isVisible={showSelectedStudents}
              itemKey="userId"
              itemLabel="name"
              renderItem={student => (
                <div className="flex items-center">
                  <div className="w-8 h-8 rounded-full bg-gradient-to-br from-purple-500 to-indigo-600 text-white flex items-center justify-center text-xs font-bold mr-2">
                    {student.name?.[0]}
                    {student.surname?.[0]}
                  </div>
                  <div className="text-sm">
                    <p className="font-medium text-gray-800 truncate max-w-[120px]">
                      {student.name} {student.surname}
                    </p>
                    <p className="text-xs text-gray-500">@{student.username}</p>
                  </div>
                </div>
              )}
            />

            <SearchSelect
              label={t(
                'createGroupPageAvailableStudents',
                'Available Students'
              )}
              placeholder={t(
                'createGroupPageSearchStudents',
                'Search students...'
              )}
              items={students}
              selectedItem={null}
              multiple
              selectedItems={preservedStudents.filter(s =>
                selectedStudents.has(s.userId)
              )}
              selectedIds={Array.from(selectedStudents)}
              onSelect={student => {
                const newSelected = new Set(selectedStudents);
                if (newSelected.has(student.userId)) {
                  newSelected.delete(student.userId);
                } else {
                  newSelected.add(student.userId);
                }
                setSelectedStudents(newSelected);
              }}
              onSearchChange={setStudentSearchQuery}
              searchQuery={studentSearchQuery}
              loading={loadingStudents}
              error={errors.students}
              currentPage={studentsPage}
              totalPages={studentsTotalPages}
              totalItems={studentsTotalItems}
              onPageChange={setStudentsPage}
              itemsPerPage={20}
              preservedSelectedItems={preservedStudents.filter(s =>
                selectedStudents.has(s.userId)
              )}
              showSelectedPanel={false}
              renderItem={(student, isSelected) => (
                <PersonCard
                  person={student}
                  isSelected={isSelected}
                  showUsername
                  showId={false}
                  gradientFrom="gray-500"
                  gradientTo="gray-600"
                  selectedGradientFrom="purple-600"
                  selectedGradientTo="indigo-700"
                  selectedTextColor="white"
                />
              )}
              valueKey="userId"
              labelKey="name"
            />
          </FormSection>
        );

      default:
        return null;
    }
  };

  const handleStepAction = async () => {
    if (!validateStep(currentStep)) return;

    if (currentStep === 3) {
      await createGroupWithSubject();
    } else if (currentStep === 4) {
      await finalizeGroup();
    } else {
      setCurrentStep(currentStep + 1);
    }
  };

  const renderActionButtons = () => (
    <ActionButtons
      primaryAction={{
        label:
          currentStep === 3
            ? t('createGroupPageCreateGroup', 'Create Group')
            : currentStep === 4
              ? t('createGroupPageFinish', 'Finish')
              : t('createGroupPageNext', 'Next'),
        loadingLabel:
          currentStep === 3
            ? t('createGroupPageCreating', 'Creating...')
            : t('createGroupPageProcessing', 'Processing...'),
        onClick: handleStepAction,
      }}
      secondaryAction={
        currentStep > 1
          ? {
              label: t('createGroupPagePrevious', 'Previous'),
              onClick: () => {
                if (currentStep === 4 && createdGroupId) {
                  return;
                }
                setCurrentStep(currentStep - 1);
              },
              disabled: currentStep === 4 && createdGroupId !== null,
            }
          : null
      }
      cancelAction={{
        label: t('createGroupPageCancel', 'Cancel'),
        onClick: () => navigate('/admin/groups'),
      }}
      loading={loading}
      compact
    />
  );

  return (
    <CreationLayout
      title={t('createGroupPageCreateNewGroup', 'Create New Group')}
      subtitle={t(
        'createGroupPageCreateNewGroupSubtitle',
        'Create a new student group by following the steps below'
      )}
      icon={<UserGroupIcon className="h-8 w-8" />}
      headerActions={renderActionButtons()}
    >
      <StepIndicator
        currentStep={currentStep}
        totalSteps={4}
        stepLabels={stepLabels}
        onStepClick={step => {
          if (
            step < currentStep ||
            (step === currentStep + 1 && validateStep(currentStep))
          ) {
            setCurrentStep(step);
          }
        }}
      />

      <div className="mt-8">{renderStepContent()}</div>
    </CreationLayout>
  );
});

CreateGroupPage.displayName = 'CreateGroupPage';

export default CreateGroupPage;
