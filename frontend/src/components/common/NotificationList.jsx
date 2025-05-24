import { useEffect, useState } from 'react';
import { useNotifications } from '@/context/NotificationContext';
import { motion, AnimatePresence } from 'framer-motion';
import NotificationTypeIcon from './NotificationTypeIcon';
import { NotificationService } from '@/services/NotificationService';
import { useTranslation } from 'react-i18next';

const NotificationList = ({ onClose, onViewAllClick }) => {
  const { t } = useTranslation();
  const {
    notifications,
    unreadCount,
    markAllAsRead,
    markAsRead,
    handleNotificationClick,
    formatNotificationTime,
  } = useNotifications();

  const [filteredNotifications, setFilteredNotifications] = useState([]);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    const notificationsArray = Array.isArray(notifications)
      ? notifications
      : [];
    const unreadNotifications = notificationsArray.filter(
      notification => !notification.read
    );
    setFilteredNotifications(unreadNotifications);
  }, [notifications]);

  const getAdjustedUrl = notification => {
    if (
      notification.actionUrl === '/api-keys' &&
      notification.type.startsWith('API_KEY_')
    ) {
      if (notification.username === 'ADMIN') {
        return '/admin/api-keys';
      }
      return '/teacher/api-keys';
    }
    return notification.actionUrl;
  };

  const getNotificationClasses = notification => {
    return notification.read
      ? 'bg-white hover:bg-gray-50'
      : 'bg-blue-50 hover:bg-blue-100 border-l-4 border-blue-500';
  };

  const handleMarkAsRead = async (e, notification) => {
    e.stopPropagation();
    if (!notification.read) {
      try {
        await NotificationService.markAsRead(notification.id);
        await markAsRead(notification.id);
      } catch {}
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      await NotificationService.markAllAsRead();
      await markAllAsRead();
    } catch {}
  };

  const hasNotifications =
    Array.isArray(filteredNotifications) && filteredNotifications.length > 0;

  const handleRefresh = async () => {
    setIsLoading(true);
    try {
      setTimeout(() => setIsLoading(false), 500);
    } catch {
      setIsLoading(false);
    }
  };

  return (
    <div className="max-h-[calc(100vh-12rem)] overflow-hidden flex flex-col shadow-lg rounded-lg">
      <div className="flex items-center justify-between px-4 py-3 bg-white border-b border-gray-200">
        <h3 className="text-lg font-medium text-gray-800">
          {t('notificationList.notifications', 'Notifications')}
        </h3>
        <div className="flex space-x-2">
          <button
            onClick={handleRefresh}
            disabled={isLoading}
            className={`text-gray-500 hover:text-gray-700 transition-colors duration-200 ${isLoading ? 'opacity-50 cursor-not-allowed' : ''}`}
            title={t('notificationList.refresh', 'Refresh')}
          >
            <svg
              className={`h-5 w-5 ${isLoading ? 'animate-spin' : ''}`}
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="2"
                d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
              />
            </svg>
          </button>

          {unreadCount > 0 && (
            <button
              onClick={handleMarkAllAsRead}
              className="text-sm text-blue-600 hover:text-blue-800 transition-colors duration-200"
            >
              {t('notificationList.markAllAsRead', 'Mark all as read')}
            </button>
          )}

          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors duration-200"
            aria-label={t('notificationList.close', 'Close')}
          >
            <svg
              className="h-5 w-5"
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="2"
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </button>
        </div>
      </div>

      <div className="px-4 py-2 bg-gray-50 border-b border-gray-200">
        <div className="flex justify-between items-center">
          <h4 className="text-sm font-medium text-gray-700">
            {t('notificationList.unreadNotifications', 'Unread notifications')}
          </h4>
          {unreadCount > 0 && (
            <span className="px-2 py-1 text-xs bg-purple-100 text-purple-800 rounded-full font-medium">
              {unreadCount}
            </span>
          )}
        </div>
      </div>

      <div className="overflow-y-auto flex-grow bg-gray-50">
        {isLoading ? (
          <div className="px-4 py-8 text-center text-gray-500">
            <svg
              className="animate-spin mx-auto h-8 w-8 text-purple-500"
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
            >
              <circle
                className="opacity-25"
                cx="12"
                cy="12"
                r="10"
                stroke="currentColor"
                strokeWidth="4"
              />
              <path
                className="opacity-75"
                fill="currentColor"
                d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
              />
            </svg>
            <p className="mt-2 text-sm">
              {t('notificationList.loading', 'Loading notifications...')}
            </p>
          </div>
        ) : !hasNotifications ? (
          <div className="px-4 py-8 text-center text-gray-500">
            <svg
              className="mx-auto h-12 w-12 text-gray-400"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="2"
                d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"
              />
            </svg>
            <p className="mt-2 text-sm font-medium">
              {t(
                'notificationList.noUnreadNotifications',
                'No unread notifications'
              )}
            </p>
            <p className="mt-1 text-sm text-gray-500">
              {t(
                'notificationList.allCaughtUp',
                "All caught up! You don't have any unread notifications."
              )}
            </p>
          </div>
        ) : (
          <AnimatePresence>
            <div className="divide-y divide-gray-200">
              {filteredNotifications.map(notification => (
                <motion.div
                  key={notification.id}
                  initial={{ opacity: 0, y: -10 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, x: -10 }}
                  transition={{ duration: 0.2 }}
                  className={`relative ${getNotificationClasses(notification)} transition-colors duration-200`}
                >
                  <div
                    className="px-4 py-3 cursor-pointer"
                    onClick={() => handleNotificationClick(notification)}
                  >
                    <div className="flex items-start">
                      <div className="flex-shrink-0 mt-0.5">
                        <NotificationTypeIcon type={notification.type} />
                      </div>
                      <div className="ml-3 flex-1 pr-8">
                        <p
                          className={`text-sm font-medium text-gray-900 ${!notification.read ? 'font-semibold' : ''}`}
                        >
                          {notification.title}
                        </p>
                        <p className="text-sm text-gray-700 mt-1">
                          {notification.message}
                        </p>
                        <div className="flex items-center mt-1 space-x-2">
                          <p className="text-xs text-gray-500">
                            {formatNotificationTime(notification.createdAt)}
                          </p>
                          {notification.actionUrl && (
                            <button
                              onClick={e => {
                                e.stopPropagation();
                                const adjustedUrl =
                                  getAdjustedUrl(notification);
                                window.open(
                                  adjustedUrl.startsWith('/')
                                    ? window.location.origin + adjustedUrl
                                    : adjustedUrl,
                                  '_blank'
                                );
                              }}
                              className="text-xs text-purple-600 hover:text-purple-800 hover:underline font-medium"
                            >
                              {t(
                                'notificationList.viewDetails',
                                'View details'
                              )}
                            </button>
                          )}
                        </div>
                      </div>

                      {!notification.read && (
                        <motion.button
                          initial={{ scale: 0.8, opacity: 0.5 }}
                          animate={{ scale: 1, opacity: 1 }}
                          whileHover={{ scale: 1.1 }}
                          onClick={e => handleMarkAsRead(e, notification)}
                          className="absolute top-3 right-4 text-blue-600 hover:text-blue-800 p-1 rounded-full hover:bg-blue-100 transition-colors duration-200"
                          title={t(
                            'notificationList.markAsRead',
                            'Mark as read'
                          )}
                        >
                          <svg
                            className="h-4 w-4"
                            xmlns="http://www.w3.org/2000/svg"
                            fill="none"
                            viewBox="0 0 24 24"
                            stroke="currentColor"
                          >
                            <path
                              strokeLinecap="round"
                              strokeLinejoin="round"
                              strokeWidth="2"
                              d="M5 13l4 4L19 7"
                            />
                          </svg>
                        </motion.button>
                      )}
                    </div>
                  </div>
                </motion.div>
              ))}
            </div>
          </AnimatePresence>
        )}
      </div>

      <div className="px-4 py-3 bg-gray-50 border-t border-gray-200 text-center">
        <button
          className="text-sm text-purple-600 hover:text-purple-800 font-medium hover:underline"
          onClick={onViewAllClick}
        >
          {t('notificationList.viewAllNotifications', 'View all notifications')}
        </button>
        <p className="text-xs text-gray-500 mt-1">
          {t(
            'notificationList.autoDeleteInfo',
            'Viewed notifications are automatically deleted after 14 days'
          )}
        </p>
      </div>
    </div>
  );
};

export default NotificationList;
