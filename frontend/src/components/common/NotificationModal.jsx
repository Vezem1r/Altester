import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { useNotifications } from '@/context/NotificationContext';
import NotificationTypeIcon from './NotificationTypeIcon';
import { NotificationService } from '@/services/NotificationService';
import { useTranslation } from 'react-i18next';

const NotificationModal = ({ isOpen, onClose }) => {
  const { t } = useTranslation();
  const { formatNotificationTime, handleNotificationClick } =
    useNotifications();

  const [notifications, setNotifications] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [activeFilter, setActiveFilter] = useState('all');
  const [searchTerm, setSearchTerm] = useState('');

  const [page, setPage] = useState(0);
  const [size, _setSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  useEffect(() => {
    if (isOpen) {
      fetchNotifications();
    }
  }, [isOpen, page, size, activeFilter, searchTerm]);

  const fetchNotifications = async () => {
    if (!isOpen) return;

    setIsLoading(true);
    try {
      const params = {
        page,
        size,
        search: searchTerm.trim() || undefined,
      };

      if (activeFilter !== 'all') {
        params.read = activeFilter === 'read';
      }

      const data = await NotificationService.getPaginatedNotifications(params);
      setNotifications(data.content);
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);
    } finally {
      setIsLoading(false);
    }
  };

  const handleFilterChange = filter => {
    if (activeFilter !== filter) {
      setActiveFilter(filter);
      setPage(0);
    }
  };

  const handleSearchChange = e => {
    setSearchTerm(e.target.value);
    setPage(0);
  };

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

  const handleMarkAsRead = async (e, notification) => {
    e.stopPropagation();
    if (!notification.read) {
      await NotificationService.markAsRead(notification.id);

      setNotifications(prev =>
        prev.map(item =>
          item.id === notification.id ? { ...item, read: true } : item
        )
      );

      if (activeFilter === 'unread') {
        fetchNotifications();
      }
    }
  };

  const handleMarkAllAsRead = async () => {
    await NotificationService.markAllAsRead();

    if (activeFilter === 'unread') {
      setNotifications([]);
      setTotalElements(0);
      setTotalPages(0);
    } else {
      setNotifications(prev =>
        prev.map(notification => ({ ...notification, read: true }))
      );
      fetchNotifications();
    }
  };

  const getNotificationClasses = notification => {
    return notification.read
      ? 'bg-white hover:bg-gray-50'
      : 'bg-blue-50 hover:bg-blue-100 border-l-4 border-blue-500';
  };

  const hasNotifications = notifications.length > 0;

  const Pagination = () => {
    return (
      <div className="flex items-center justify-between px-4 py-3 bg-white border-t border-gray-200 sm:px-6">
        <div className="flex flex-1 justify-between sm:hidden">
          <button
            onClick={() => setPage(prev => Math.max(0, prev - 1))}
            disabled={page === 0}
            className={`relative inline-flex items-center rounded-md border ${
              page === 0
                ? 'border-gray-300 bg-gray-100 text-gray-400'
                : 'border-gray-300 bg-white text-gray-700'
            } px-4 py-2 text-sm font-medium ${page === 0 ? 'cursor-not-allowed' : 'hover:bg-gray-50'}`}
          >
            {t('notificationModal.previous', 'Previous')}
          </button>
          <button
            onClick={() => setPage(prev => Math.min(totalPages - 1, prev + 1))}
            disabled={page >= totalPages - 1}
            className={`relative ml-3 inline-flex items-center rounded-md border ${
              page >= totalPages - 1
                ? 'border-gray-300 bg-gray-100 text-gray-400'
                : 'border-gray-300 bg-white text-gray-700'
            } px-4 py-2 text-sm font-medium ${page >= totalPages - 1 ? 'cursor-not-allowed' : 'hover:bg-gray-50'}`}
          >
            {t('notificationModal.next', 'Next')}
          </button>
        </div>
        <div className="hidden sm:flex sm:flex-1 sm:items-center sm:justify-between">
          <div>
            <p className="text-sm text-gray-700">
              {t('notificationModal.showing', 'Showing')}{' '}
              <span className="font-medium">
                {hasNotifications ? page * size + 1 : 0}
              </span>{' '}
              {t('notificationModal.to', 'to')}{' '}
              <span className="font-medium">
                {Math.min((page + 1) * size, totalElements)}
              </span>{' '}
              {t('notificationModal.of', 'of')}{' '}
              <span className="font-medium">{totalElements}</span>{' '}
              {t('notificationModal.results', 'results')}
            </p>
          </div>
          <div>
            <nav
              className="isolate inline-flex -space-x-px rounded-md shadow-sm"
              aria-label="Pagination"
            >
              <button
                onClick={() => setPage(prev => Math.max(0, prev - 1))}
                disabled={page === 0}
                className={`relative inline-flex items-center rounded-l-md px-2 py-2 text-gray-400 ${
                  page === 0 ? 'cursor-not-allowed' : 'hover:bg-gray-50'
                } ring-1 ring-inset ring-gray-300 focus:z-20 focus:outline-offset-0`}
              >
                <span className="sr-only">
                  {t('notificationModal.previous', 'Previous')}
                </span>
                <svg
                  className="h-5 w-5"
                  viewBox="0 0 20 20"
                  fill="currentColor"
                  aria-hidden="true"
                >
                  <path
                    fillRule="evenodd"
                    d="M12.79 5.23a.75.75 0 01-.02 1.06L8.832 10l3.938 3.71a.75.75 0 11-1.04 1.08l-4.5-4.25a.75.75 0 010-1.08l4.5-4.25a.75.75 0 011.06.02z"
                    clipRule="evenodd"
                  />
                </svg>
              </button>

              {Array.from({ length: Math.min(5, totalPages) }).map((_, i) => {
                const pageToShow = Math.min(
                  Math.max(page - 2 + i, i),
                  Math.max(totalPages - 5 + i, i)
                );

                return (
                  <button
                    key={pageToShow}
                    onClick={() => setPage(pageToShow)}
                    className={`relative inline-flex items-center px-4 py-2 text-sm font-semibold ${
                      page === pageToShow
                        ? 'z-10 bg-purple-600 text-white focus:z-20 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-purple-600'
                        : 'text-gray-900 ring-1 ring-inset ring-gray-300 hover:bg-gray-50 focus:z-20 focus:outline-offset-0'
                    }`}
                  >
                    {pageToShow + 1}
                  </button>
                );
              })}

              <button
                onClick={() =>
                  setPage(prev => Math.min(totalPages - 1, prev + 1))
                }
                disabled={page >= totalPages - 1}
                className={`relative inline-flex items-center rounded-r-md px-2 py-2 text-gray-400 ${
                  page >= totalPages - 1
                    ? 'cursor-not-allowed'
                    : 'hover:bg-gray-50'
                } ring-1 ring-inset ring-gray-300 focus:z-20 focus:outline-offset-0`}
              >
                <span className="sr-only">
                  {t('notificationModal.next', 'Next')}
                </span>
                <svg
                  className="h-5 w-5"
                  viewBox="0 0 20 20"
                  fill="currentColor"
                  aria-hidden="true"
                >
                  <path
                    fillRule="evenodd"
                    d="M7.21 14.77a.75.75 0 01.02-1.06L11.168 10 7.23 6.29a.75.75 0 111.04-1.08l4.5 4.25a.75.75 0 010 1.08l-4.5 4.25a.75.75 0 01-1.06-.02z"
                    clipRule="evenodd"
                  />
                </svg>
              </button>
            </nav>
          </div>
        </div>
      </div>
    );
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-black bg-opacity-50 flex items-center justify-center">
      <motion.div
        initial={{ opacity: 0, scale: 0.95 }}
        animate={{ opacity: 1, scale: 1 }}
        exit={{ opacity: 0, scale: 0.95 }}
        transition={{ duration: 0.2 }}
        className="bg-white rounded-lg shadow-xl w-full max-w-4xl m-4 max-h-[90vh] flex flex-col"
      >
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200">
          <h2 className="text-xl font-semibold text-gray-800">
            {t('notificationModal.allNotifications', 'All Notifications')}
          </h2>
          <button
            onClick={onClose}
            className="text-gray-500 hover:text-gray-700 transition-colors duration-200"
          >
            <svg
              className="h-6 w-6"
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

        <div className="px-6 py-3 border-b border-gray-200 bg-gray-50">
          <div className="flex flex-col md:flex-row items-center space-y-2 md:space-y-0 md:space-x-4">
            <div className="relative flex-grow">
              <input
                type="text"
                placeholder={t(
                  'notificationModal.searchPlaceholder',
                  'Search notifications...'
                )}
                value={searchTerm}
                onChange={handleSearchChange}
                className="w-full px-4 py-2 pr-10 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent"
              />
              <div className="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none">
                <svg
                  className="h-5 w-5 text-gray-400"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth="2"
                    d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                  />
                </svg>
              </div>
            </div>

            <div className="flex space-x-2">
              <button
                onClick={() => handleFilterChange('all')}
                className={`px-4 py-2 rounded-md text-sm font-medium ${
                  activeFilter === 'all'
                    ? 'bg-purple-500 text-white'
                    : 'bg-white text-gray-700 hover:bg-gray-100'
                } transition-colors duration-200`}
              >
                {t('notificationModal.filterAll', 'All')}
              </button>
              <button
                onClick={() => handleFilterChange('unread')}
                className={`px-4 py-2 rounded-md text-sm font-medium ${
                  activeFilter === 'unread'
                    ? 'bg-purple-500 text-white'
                    : 'bg-white text-gray-700 hover:bg-gray-100'
                } transition-colors duration-200`}
              >
                {t('notificationModal.filterUnreadEle', 'Unread')}
              </button>
              <button
                onClick={() => handleFilterChange('read')}
                className={`px-4 py-2 rounded-md text-sm font-medium ${
                  activeFilter === 'read'
                    ? 'bg-purple-500 text-white'
                    : 'bg-white text-gray-700 hover:bg-gray-100'
                } transition-colors duration-200`}
              >
                {t('notificationModal.filterRead', 'Read')}
              </button>
            </div>

            <button
              onClick={fetchNotifications}
              disabled={isLoading}
              className={`ml-auto p-2 rounded-md ${
                isLoading
                  ? 'opacity-50 cursor-not-allowed'
                  : 'hover:bg-gray-200'
              } transition-colors duration-200`}
              title={t('notificationModal.refreshTooltip', 'Refresh')}
            >
              <svg
                className={`h-5 w-5 text-gray-600 ${isLoading ? 'animate-spin' : ''}`}
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
          </div>

          <div className="flex justify-between items-center mt-3 text-sm">
            <p className="text-gray-600">
              {totalElements}{' '}
              {t('notificationModal.notification', 'notification')}
              {totalElements !== 1 ? t('notificationModal.plural', 's') : ''}
              {activeFilter !== 'all'
                ? ` (${activeFilter === 'read' ? t('notificationModal.filterReadEle', 'read') : t('notificationModal.filterUnread', 'unread')})`
                : ''}
              {searchTerm
                ? ` ${t('notificationModal.matching', 'matching')} "${searchTerm}"`
                : ''}
            </p>
            <button
              onClick={handleMarkAllAsRead}
              className={`text-purple-600 hover:text-purple-800 hover:underline font-medium ${
                !hasNotifications || !notifications.some(n => !n.read)
                  ? 'opacity-50 cursor-not-allowed'
                  : ''
              }`}
              disabled={!hasNotifications || !notifications.some(n => !n.read)}
            >
              {t('notificationModal.markAllAsRead', 'Mark all as read')}
            </button>
          </div>
        </div>

        <div className="overflow-y-auto flex-grow">
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
                {t('notificationModal.loading', 'Loading notifications...')}
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
                  'notificationModal.noNotificationsFound',
                  'No notifications found'
                )}
              </p>
              <p className="mt-1 text-sm text-gray-500">
                {searchTerm
                  ? t(
                      'notificationModal.adjustSearchTerm',
                      'Try adjusting your search term.'
                    )
                  : activeFilter === 'unread'
                    ? t(
                        'notificationModal.noUnreadNotifications',
                        "You don't have any unread notifications."
                      )
                    : activeFilter === 'read'
                      ? t(
                          'notificationModal.noReadNotifications',
                          "You don't have any read notifications."
                        )
                      : t(
                          'notificationModal.noNotificationsYet',
                          "You don't have any notifications yet."
                        )}
              </p>
            </div>
          ) : (
            <div className="divide-y divide-gray-200">
              {notifications.map(notification => (
                <div
                  key={notification.id}
                  className={`${getNotificationClasses(notification)} transition-colors duration-200 relative`}
                >
                  <div
                    className="px-6 py-4 cursor-pointer"
                    onClick={() => handleNotificationClick(notification)}
                  >
                    <div className="flex items-start">
                      <div className="flex-shrink-0 mt-0.5">
                        <NotificationTypeIcon type={notification.type} />
                      </div>
                      <div className="ml-3 flex-1 pr-10">
                        <div className="flex justify-between items-start">
                          <p
                            className={`text-sm font-medium text-gray-900 ${!notification.read ? 'font-semibold' : ''}`}
                          >
                            {notification.title}
                          </p>
                          <p className="text-xs text-gray-500 ml-2">
                            {formatNotificationTime(notification.createdAt)}
                          </p>
                        </div>
                        <p className="text-sm text-gray-700 mt-1">
                          {notification.message}
                        </p>

                        {notification.actionUrl && (
                          <div className="mt-2">
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
                              className="px-3 py-1 text-xs bg-purple-100 text-purple-700 rounded-full hover:bg-purple-200 font-medium transition-colors duration-200"
                            >
                              {t(
                                'notificationModal.viewDetails',
                                'View details'
                              )}
                            </button>
                          </div>
                        )}
                      </div>

                      {!notification.read && (
                        <button
                          onClick={e => handleMarkAsRead(e, notification)}
                          className="absolute top-4 right-4 text-blue-600 hover:text-blue-800 p-1 rounded-full hover:bg-blue-100 transition-colors duration-200"
                          title={t(
                            'notificationModal.markAsRead',
                            'Mark as read'
                          )}
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
                              d="M5 13l4 4L19 7"
                            />
                          </svg>
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {totalPages > 1 && <Pagination />}

        <div className="px-6 py-3 bg-gray-50 border-t border-gray-200">
          <p className="text-sm text-gray-500 text-center">
            {t(
              'notificationModal.autoDeleteInfo',
              'Viewed notifications are automatically deleted after 14 days'
            )}
          </p>
        </div>
      </motion.div>
    </div>
  );
};

export default NotificationModal;
