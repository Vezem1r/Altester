import { useState, useEffect, useCallback, useRef } from 'react';
import { StudentService } from '@/services/StudentService';
import { toast } from 'react-toastify';

export const useStudentDashboard = initialUser => {
  const [dashboardData, setDashboardData] = useState(null);
  const [availablePeriods, setAvailablePeriods] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [currentPeriod, setCurrentPeriod] = useState(null);
  const [isCurrentSemester, setIsCurrentSemester] = useState(true);
  const [displayedGroups, setDisplayedGroups] = useState([]);
  const [completeUserData, setCompleteUserData] = useState(initialUser);
  const [isInitialFetchDone, setIsInitialFetchDone] = useState(false);

  const dashboardCache = useRef({
    currentSemester: null,
    periodHistory: {},
    searchResults: {},
  });

  const searchTimeout = useRef(null);
  const searchPending = useRef(false);
  const abortControllerRef = useRef(null);
  const historyFetchInProgress = useRef(false);

  const initialFetchInProgressRef = useRef(false);

  const getPeriodCacheKey = useCallback((periodData, query = '') => {
    if (!periodData || periodData.currentSemester) {
      return `current${query ? `-${query}` : ''}`;
    }
    return `${periodData.academicYear}-${periodData.semester}${query ? `-${query}` : ''}`;
  }, []);

  const fetchAvailablePeriods = useCallback(async () => {
    try {
      const periodsData = await StudentService.getAvailableAcademicPeriods();
      setAvailablePeriods(periodsData.periods || []);
      return periodsData.periods || [];
    } catch {
      toast.error('Failed to load semester data');
      return [];
    }
  }, []);

  const fetchCurrentSemesterData = useCallback(async () => {
    try {
      if (dashboardCache.current.currentSemester) {
        return dashboardCache.current.currentSemester;
      }

      const dashboard = await StudentService.getStudentDashboard();

      dashboardCache.current.currentSemester = dashboard;

      return dashboard;
    } catch (error) {
      toast.error(error.message || 'Failed to load dashboard data');
      return null;
    }
  }, []);

  const fetchInitialData = useCallback(async () => {
    if (isInitialFetchDone || initialFetchInProgressRef.current) return;

    initialFetchInProgressRef.current = true;

    try {
      setLoading(true);

      const [dashboard] = await Promise.all([
        fetchCurrentSemesterData(),
        availablePeriods.length === 0
          ? fetchAvailablePeriods()
          : Promise.resolve(availablePeriods),
      ]);

      if (dashboard) {
        setDashboardData(dashboard);

        setCompleteUserData(prev => ({
          ...prev,
          ...dashboard,
          registered: dashboard.registered,
        }));

        if (dashboard.currentGroups && dashboard.currentGroups.length > 0) {
          setDisplayedGroups(dashboard.currentGroups);
        }

        if (dashboard.currentGroups && dashboard.currentGroups.length > 0) {
          const firstGroup = dashboard.currentGroups[0];
          setCurrentPeriod({
            academicYear: firstGroup.academicYear,
            semester: firstGroup.semester,
          });
        }
      }

      setIsInitialFetchDone(true);
    } catch {
      toast.error('Failed to load dashboard data');
    } finally {
      setLoading(false);
      initialFetchInProgressRef.current = false;
    }
  }, [fetchCurrentSemesterData, fetchAvailablePeriods, availablePeriods]);

  useEffect(() => {
    return () => {
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }
      if (searchTimeout.current) {
        clearTimeout(searchTimeout.current);
      }
    };
  }, []);

  const resetToCurrentSemester = useCallback(() => {
    if (isCurrentSemester && !loading) return;

    try {
      setLoading(true);
      setSearchQuery('');
      setIsCurrentSemester(true);

      if (dashboardCache.current.currentSemester) {
        setDashboardData(dashboardCache.current.currentSemester);
        setDisplayedGroups(
          dashboardCache.current.currentSemester.currentGroups || []
        );

        if (dashboardCache.current.currentSemester.currentGroups?.length > 0) {
          const firstGroup =
            dashboardCache.current.currentSemester.currentGroups[0];
          setCurrentPeriod({
            academicYear: firstGroup.academicYear,
            semester: firstGroup.semester,
          });
        }
      } else {
        fetchCurrentSemesterData().then(dashboard => {
          if (dashboard) {
            setDashboardData(dashboard);

            if (dashboard.currentGroups && dashboard.currentGroups.length > 0) {
              setDisplayedGroups(dashboard.currentGroups);

              const firstGroup = dashboard.currentGroups[0];
              setCurrentPeriod({
                academicYear: firstGroup.academicYear,
                semester: firstGroup.semester,
              });
            } else {
              setDisplayedGroups([]);
            }
          }
        });
      }
    } catch (error) {
      toast.error(error.message || 'Failed to reset to current semester');
    } finally {
      setLoading(false);
    }
  }, [isCurrentSemester, loading, fetchCurrentSemesterData]);

  const fetchAcademicHistory = useCallback(
    async (periodData, searchText = '') => {
      if (historyFetchInProgress.current) {
        return new Promise(resolve => {
          const checkInterval = setInterval(() => {
            if (!historyFetchInProgress.current) {
              clearInterval(checkInterval);

              const cacheKey = getPeriodCacheKey(periodData, searchText);
              if (
                searchText &&
                dashboardCache.current.searchResults[cacheKey]
              ) {
                resolve(dashboardCache.current.searchResults[cacheKey]);
              } else if (!searchText) {
                const originalCacheKey = getPeriodCacheKey(periodData);
                if (dashboardCache.current.periodHistory[originalCacheKey]) {
                  resolve(
                    dashboardCache.current.periodHistory[originalCacheKey]
                  );
                }
              }

              resolve(null);
            }
          }, 50);
        });
      }

      historyFetchInProgress.current = true;

      try {
        const history = await StudentService.getAcademicHistory(
          periodData.academicYear,
          periodData.semester,
          searchText
        );

        const historyPeriod = history.academicHistory?.find(
          period =>
            period.academicYear === periodData.academicYear &&
            period.semester === periodData.semester
        ) || { groups: [] };

        if (searchText) {
          const searchCacheKey = getPeriodCacheKey(periodData, searchText);
          dashboardCache.current.searchResults[searchCacheKey] = historyPeriod;
        } else {
          const originalCacheKey = getPeriodCacheKey(periodData);
          dashboardCache.current.periodHistory[originalCacheKey] =
            historyPeriod;
        }

        return historyPeriod;
      } catch (error) {
        toast.error(error.message || 'Failed to load academic history');
        return { groups: [] };
      } finally {
        historyFetchInProgress.current = false;
      }
    },
    [getPeriodCacheKey]
  );

  const handlePeriodChange = useCallback(
    async periodData => {
      if (
        currentPeriod &&
        periodData.academicYear === currentPeriod.academicYear &&
        periodData.semester === currentPeriod.semester &&
        periodData.currentSemester === isCurrentSemester
      ) {
        return;
      }

      try {
        setLoading(true);
        setSearchQuery('');

        if (periodData.currentSemester) {
          setIsCurrentSemester(true);

          if (dashboardCache.current.currentSemester) {
            setDashboardData(dashboardCache.current.currentSemester);
            setDisplayedGroups(
              dashboardCache.current.currentSemester.currentGroups || []
            );

            if (
              dashboardCache.current.currentSemester.currentGroups?.length > 0
            ) {
              const firstGroup =
                dashboardCache.current.currentSemester.currentGroups[0];
              setCurrentPeriod({
                academicYear: firstGroup.academicYear,
                semester: firstGroup.semester,
              });
            }
          } else {
            fetchCurrentSemesterData().then(dashboard => {
              if (dashboard) {
                setDashboardData(dashboard);

                if (
                  dashboard.currentGroups &&
                  dashboard.currentGroups.length > 0
                ) {
                  setDisplayedGroups(dashboard.currentGroups);

                  const firstGroup = dashboard.currentGroups[0];
                  setCurrentPeriod({
                    academicYear: firstGroup.academicYear,
                    semester: firstGroup.semester,
                  });
                } else {
                  setDisplayedGroups([]);
                }
              }
            });
          }
        } else {
          setIsCurrentSemester(false);
          setCurrentPeriod(periodData);

          const cacheKey = getPeriodCacheKey(periodData);
          if (dashboardCache.current.periodHistory[cacheKey]) {
            const cachedPeriod = dashboardCache.current.periodHistory[cacheKey];
            setDisplayedGroups(cachedPeriod.groups || []);
          } else {
            try {
              const historyPeriod = await fetchAcademicHistory(periodData);

              if (historyPeriod && historyPeriod.groups) {
                setDisplayedGroups(historyPeriod.groups);
              } else {
                setDisplayedGroups([]);
              }
            } catch (error) {
              toast.error(
                error.message || 'Failed to load data for selected period'
              );
              setDisplayedGroups([]);
            }
          }
        }
      } catch (error) {
        toast.error(error.message || 'Failed to load data for selected period');
      } finally {
        setLoading(false);
      }
    },
    [
      currentPeriod,
      isCurrentSemester,
      fetchCurrentSemesterData,
      getPeriodCacheKey,
      fetchAcademicHistory,
    ]
  );

  const handleSearch = useCallback(
    e => {
      if (e && e.preventDefault) {
        e.preventDefault();
      }

      if (searchPending.current) {
        if (searchTimeout.current) {
          clearTimeout(searchTimeout.current);
        }
      }

      if (searchQuery.length < 2 && searchQuery.length > 0) {
        return;
      }

      if (searchTimeout.current) {
        clearTimeout(searchTimeout.current);
      }

      searchTimeout.current = setTimeout(async () => {
        try {
          searchPending.current = true;
          setLoading(true);

          if (abortControllerRef.current) {
            abortControllerRef.current.abort();
          }
          abortControllerRef.current = new AbortController();

          const cacheKey = isCurrentSemester
            ? `current-${searchQuery}`
            : `${currentPeriod.academicYear}-${currentPeriod.semester}-${searchQuery}`;

          if (dashboardCache.current.searchResults[cacheKey]) {
            if (isCurrentSemester) {
              setDashboardData(dashboardCache.current.searchResults[cacheKey]);
              setDisplayedGroups(
                dashboardCache.current.searchResults[cacheKey].currentGroups ||
                  []
              );
            } else {
              setDisplayedGroups(
                dashboardCache.current.searchResults[cacheKey].groups || []
              );
            }
            setLoading(false);
            searchPending.current = false;
            return;
          }

          if (isCurrentSemester) {
            if (searchQuery === '' && dashboardCache.current.currentSemester) {
              setDashboardData(dashboardCache.current.currentSemester);
              setDisplayedGroups(
                dashboardCache.current.currentSemester.currentGroups || []
              );
            } else {
              const dashboard =
                await StudentService.getStudentDashboard(searchQuery);

              dashboardCache.current.searchResults[cacheKey] = dashboard;

              setDashboardData(dashboard);
              setDisplayedGroups(dashboard.currentGroups || []);
            }
          } else if (currentPeriod) {
            if (searchQuery === '') {
              const originalCacheKey = getPeriodCacheKey(currentPeriod);
              if (dashboardCache.current.periodHistory[originalCacheKey]) {
                const cachedPeriod =
                  dashboardCache.current.periodHistory[originalCacheKey];
                setDisplayedGroups(cachedPeriod.groups || []);
              } else {
                const historyPeriod = await fetchAcademicHistory(currentPeriod);

                if (historyPeriod && historyPeriod.groups) {
                  setDisplayedGroups(historyPeriod.groups);
                } else {
                  setDisplayedGroups([]);
                }
              }
            } else {
              const historyPeriod = await fetchAcademicHistory(
                currentPeriod,
                searchQuery
              );

              if (historyPeriod && historyPeriod.groups) {
                setDisplayedGroups(historyPeriod.groups);
              } else {
                setDisplayedGroups([]);
              }
            }
          }
        } catch (error) {
          if (error.name !== 'AbortError') {
            toast.error(error.message || 'Failed to search tests');
          }
        } finally {
          setLoading(false);
          searchPending.current = false;
        }
      }, 300);
    },
    [
      isCurrentSemester,
      currentPeriod,
      searchQuery,
      getPeriodCacheKey,
      fetchAcademicHistory,
    ]
  );

  const fetchTestAttempts = useCallback(async testId => {
    try {
      const response = await StudentService.getStudentTestAttempts(testId);
      return {
        attempts: response.attempts || [],
        testTitle: response.testTitle,
        totalScore: response.totalScore,
      };
    } catch (error) {
      toast.error(error.message || 'Failed to load test attempts');
      throw error;
    }
  }, []);

  return {
    dashboardData,
    availablePeriods,
    loading,
    searchQuery,
    setSearchQuery,
    currentPeriod,
    isCurrentSemester,
    displayedGroups,
    completeUserData,
    fetchInitialData,
    resetToCurrentSemester,
    handlePeriodChange,
    handleSearch,
    fetchTestAttempts,
  };
};
