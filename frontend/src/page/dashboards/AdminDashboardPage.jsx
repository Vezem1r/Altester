import { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import AdminLayout from '@/layouts/AdminLayout';
import { AdminService } from '@/services/AdminService';
import StatsCardGrid from '@/components/admin/StatsCardGrid';
import AIModelComponent from '@/components/admin/AIModelComponent';
import { useTranslation } from 'react-i18next';

const DashboardHome = () => {
  const { t } = useTranslation();
  const [stats, setStats] = useState({
    studentsCount: 0,
    teachersCount: 0,
    groupsCount: 0,
    subjectsCount: 0,
    testsCount: 0,
    aiAccuracy: 98.7,
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        setLoading(true);
        const data = await AdminService.getAdminStats();
        setStats({
          ...data,
          aiAccuracy: data.aiAccuracy || 98.7,
        });
      } catch (error) {
        toast.error(
          error.message ||
            t(
              'adminDashboardPage.errorLoadingStats',
              'Error loading dashboard statistics'
            )
        );
      } finally {
        setLoading(false);
      }
    };

    fetchStats();
  }, [t]);

  return (
    <AdminLayout>
      <div className="bg-white shadow overflow-hidden sm:rounded-lg mb-6">
        <div className="px-4 py-5 sm:px-6">
          <h1 className="text-lg leading-6 font-medium text-gray-900">
            {t('adminDashboardPage.title', 'Admin Dashboard')}
          </h1>
          <p className="mt-1 max-w-2xl text-sm text-gray-500">
            {t(
              'adminDashboardPage.welcome',
              'Welcome to the AITester Admin Dashboard'
            )}
          </p>
        </div>

        <div className="border-t border-gray-200">
          {loading ? (
            <div className="flex justify-center items-center py-12">
              <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-purple-600" />
            </div>
          ) : (
            <div className="px-4 py-5 sm:p-6">
              <StatsCardGrid stats={stats} />
            </div>
          )}
        </div>
      </div>

      <div className="mt-6">
        <AIModelComponent />
      </div>
    </AdminLayout>
  );
};

export default DashboardHome;
