import React, {
  useState,
  useEffect,
  useCallback,
  useMemo,
  useRef,
} from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import { useStudentDashboard } from '@/hooks/useStudentDashboard';
import { useTranslation } from 'react-i18next';

import StudentHeader from '@/components/header/StudentHeader';
import CurrentSemester from '@/components/student/CurrentSemester';
import GroupsList from '@/components/student/GroupsList';
import SearchBar from '@/components/student/SearchBar';
import TestConfirmModal from '@/components/studentTest/TestConfirmModal';
import TestAttemptsModal from '@/components/student/TestAttemptsModal';

const initialModalState = {
  showConfirmModal: false,
  showAttemptsModal: false,
  currentTest: null,
  attemptsData: null,
  loadingAttempts: false,
};

const StudentDashboardPage = () => {
  const { t } = useTranslation();
  const { user: authUser, logout } = useAuth();
  const navigate = useNavigate();

  const fetchingPeriodRef = useRef(false);
  const fetchingAttemptsRef = useRef(false);

  const {
    loading,
    searchQuery,
    setSearchQuery,
    isCurrentSemester,
    displayedGroups,
    availablePeriods,
    currentPeriod,
    completeUserData,
    fetchInitialData,
    resetToCurrentSemester,
    handlePeriodChange: originalHandlePeriodChange,
    handleSearch,
    fetchTestAttempts: originalFetchTestAttempts,
  } = useStudentDashboard(authUser);

  const [modalState, setModalState] = useState(initialModalState);

  const { showConfirmModal, showAttemptsModal, currentTest, attemptsData } =
    modalState;

  useEffect(() => {
    if (showConfirmModal || showAttemptsModal) {
      document.body.classList.add('overflow-hidden');
    } else {
      document.body.classList.remove('overflow-hidden');
    }

    return () => {
      document.body.classList.remove('overflow-hidden');
    };
  }, [showConfirmModal, showAttemptsModal]);

  const initialFetchRef = useRef(false);

  useEffect(() => {
    if (initialFetchRef.current) return;

    initialFetchRef.current = true;
    fetchInitialData();

    return () => {
      initialFetchRef.current = false;
    };
  }, [fetchInitialData]);

  const handleLogout = useCallback(() => {
    logout();
    navigate('/');
  }, [logout, navigate]);

  const handleShowTestConfirmModal = useCallback(test => {
    setModalState(prev => ({
      ...prev,
      currentTest: test,
      showConfirmModal: true,
    }));
  }, []);

  const handleConfirmTest = useCallback(() => {
    if (currentTest) {
      setModalState(prev => ({
        ...prev,
        showConfirmModal: false,
      }));

      navigate(`/student/tests/${currentTest.id}`, {
        state: {
          confirmed: true,
          test: currentTest,
        },
      });
    }
  }, [currentTest, navigate]);

  const handleCancelTest = useCallback(() => {
    setModalState(prev => ({
      ...prev,
      showConfirmModal: false,
      currentTest: null,
    }));
  }, []);

  const handlePeriodChange = useCallback(
    periodId => {
      if (fetchingPeriodRef.current) return;

      fetchingPeriodRef.current = true;
      originalHandlePeriodChange(periodId).finally(() => {
        fetchingPeriodRef.current = false;
      });
    },
    [originalHandlePeriodChange]
  );

  const fetchTestAttempts = useCallback(
    async test => {
      if (fetchingAttemptsRef.current) return;

      try {
        fetchingAttemptsRef.current = true;
        return await originalFetchTestAttempts(test.id);
      } finally {
        fetchingAttemptsRef.current = false;
      }
    },
    [originalFetchTestAttempts]
  );

  const handleShowAttemptsModal = useCallback(
    async test => {
      try {
        setModalState(prev => ({
          ...prev,
          loadingAttempts: true,
        }));

        const response = await fetchTestAttempts(test);

        setModalState(prev => ({
          ...prev,
          attemptsData: response,
          showAttemptsModal: true,
          loadingAttempts: false,
        }));
      } catch {
        setModalState(prev => ({
          ...prev,
          loadingAttempts: false,
        }));
      }
    },
    [fetchTestAttempts]
  );

  const handleCloseAttemptsModal = useCallback(() => {
    setModalState(prev => ({
      ...prev,
      showAttemptsModal: false,
      attemptsData: null,
    }));
  }, []);

  const memoizedGroupsList = useMemo(
    () => (
      <GroupsList
        groups={displayedGroups}
        loading={loading}
        onStartTest={handleShowTestConfirmModal}
        onViewAttempts={handleShowAttemptsModal}
        isCurrentSemester={isCurrentSemester}
        searchQuery={searchQuery}
      />
    ),
    [
      displayedGroups,
      loading,
      handleShowTestConfirmModal,
      handleShowAttemptsModal,
      isCurrentSemester,
      searchQuery,
    ]
  );

  const handleMemoizedResetToCurrentSemester = useCallback(() => {
    if (fetchingPeriodRef.current) return;

    fetchingPeriodRef.current = true;

    Promise.resolve(resetToCurrentSemester()).finally(() => {
      fetchingPeriodRef.current = false;
    });
  }, [resetToCurrentSemester]);

  return (
    <div className="flex flex-col min-h-screen bg-gradient-to-br from-gray-50 to-purple-50">
      <div className="sticky top-0 z-10 w-full">
        <StudentHeader
          user={completeUserData}
          onLogout={handleLogout}
          resetToCurrentSemester={handleMemoizedResetToCurrentSemester}
        />
      </div>

      <main className="flex-1 py-6 pt-4">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex flex-col md:flex-row md:justify-between md:items-center gap-4 mb-6">
            <div className="w-full md:max-w-lg">
              <SearchBar
                searchQuery={searchQuery}
                setSearchQuery={setSearchQuery}
                handleSearch={handleSearch}
                loading={loading}
                placeholder={t(
                  'studentDashboardPage.searchPlaceholder',
                  'Search for groups or subjects...'
                )}
              />
            </div>

            <div className="ml-auto">
              <CurrentSemester
                currentPeriod={currentPeriod}
                availablePeriods={availablePeriods}
                onPeriodChange={handlePeriodChange}
                loading={loading}
                isCurrentSemester={isCurrentSemester}
              />
            </div>
          </div>

          {memoizedGroupsList}
        </div>
      </main>

      {showConfirmModal && currentTest && (
        <TestConfirmModal
          test={currentTest}
          onConfirm={handleConfirmTest}
          onCancel={handleCancelTest}
        />
      )}

      {showAttemptsModal && attemptsData && (
        <TestAttemptsModal
          attempts={attemptsData.attempts}
          testTitle={attemptsData.testTitle}
          totalScore={attemptsData.totalScore}
          onClose={handleCloseAttemptsModal}
        />
      )}
    </div>
  );
};

export default React.memo(StudentDashboardPage);
