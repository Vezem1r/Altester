import { useState, useRef, useEffect } from 'react';
import { useNotifications } from '../../context/NotificationContext';
import NotificationList from './NotificationList';
import NotificationModal from './NotificationModal';
import { motion, AnimatePresence } from 'framer-motion';
import { useTranslation } from 'react-i18next';

const NotificationIcon = () => {
  const { t } = useTranslation();
  const [isOpen, setIsOpen] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const { unreadCount, connectionStatus, reconnect } = useNotifications();
  const wrapperRef = useRef(null);
  const [isReconnecting, setIsReconnecting] = useState(false);

  useEffect(() => {
    function handleClickOutside(event) {
      if (wrapperRef.current && !wrapperRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    }

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [wrapperRef]);

  useEffect(() => {
    function handleEscKey(event) {
      if (event.key === 'Escape') {
        if (isOpen) {
          setIsOpen(false);
        }
        if (isModalOpen) {
          setIsModalOpen(false);
        }
      }
    }

    document.addEventListener('keydown', handleEscKey);
    return () => {
      document.removeEventListener('keydown', handleEscKey);
    };
  }, [isOpen, isModalOpen]);

  const toggleNotifications = () => {
    setIsOpen(!isOpen);
  };

  const openAllNotificationsModal = () => {
    setIsOpen(false);
    setIsModalOpen(true);
  };

  const handleReconnect = async e => {
    e.stopPropagation();
    setIsReconnecting(true);

    try {
      await reconnect();
    } catch {
    } finally {
      setIsReconnecting(false);
    }
  };

  return (
    <div className="relative" ref={wrapperRef}>
      <motion.button
        type="button"
        className="relative p-2 rounded-full text-gray-500 hover:text-purple-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500 transition-colors duration-200"
        onClick={toggleNotifications}
        aria-expanded={isOpen}
        title={
          unreadCount > 0
            ? t(
                'notificationIcon.unreadNotifications',
                'Notifications ({{count}} unread)',
                { count: unreadCount }
              )
            : t('notificationIcon.notifications', 'Notifications')
        }
        whileHover={{ scale: 1.05 }}
        whileTap={{ scale: 0.95 }}
      >
        <span className="sr-only">
          {t('notificationIcon.viewNotifications', 'View notifications')}
        </span>
        <svg
          className="h-6 w-6"
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth="2"
            d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"
          />
        </svg>

        <AnimatePresence>
          {unreadCount > 0 && (
            <motion.span
              initial={{ scale: 0.5, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.5, opacity: 0 }}
              className="absolute top-0 right-0 h-5 w-5 rounded-full bg-red-500 text-xs text-white flex items-center justify-center font-medium"
            >
              {unreadCount > 99 ? '99+' : unreadCount}
            </motion.span>
          )}
        </AnimatePresence>
      </motion.button>

      <AnimatePresence>
        {isOpen && (
          <motion.div
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -10 }}
            transition={{ duration: 0.2 }}
            className="origin-top-right absolute right-0 mt-2 w-80 md:w-96 rounded-md shadow-lg bg-white ring-1 ring-black ring-opacity-5 z-50 overflow-hidden"
          >
            {connectionStatus === 'error' && (
              <div className="px-4 py-2 text-sm bg-red-100 text-red-800 flex items-center justify-between">
                <span className="flex items-center">
                  <svg
                    className="mr-2 h-4 w-4 text-red-600"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth="2"
                      d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
                    />
                  </svg>
                  {t('notificationIcon.connectionIssue', 'Connection issue')}
                </span>

                <motion.button
                  onClick={handleReconnect}
                  disabled={isReconnecting}
                  className={`ml-2 px-2 py-1 text-xs font-medium bg-white rounded border border-current
                    ${isReconnecting ? 'opacity-50 cursor-not-allowed' : 'hover:bg-gray-100'} transition-colors duration-200`}
                  whileHover={!isReconnecting ? { scale: 1.05 } : {}}
                  whileTap={!isReconnecting ? { scale: 0.95 } : {}}
                >
                  {isReconnecting ? (
                    <span className="flex items-center">
                      <svg
                        className="animate-spin -ml-1 mr-1 h-3 w-3 text-red-600"
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
                      {t('notificationIcon.reconnecting', 'Reconnecting...')}
                    </span>
                  ) : (
                    t('notificationIcon.retry', 'Retry')
                  )}
                </motion.button>
              </div>
            )}

            <NotificationList
              onClose={() => setIsOpen(false)}
              onViewAllClick={openAllNotificationsModal}
            />
          </motion.div>
        )}
      </AnimatePresence>

      <NotificationModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
      />
    </div>
  );
};

export default NotificationIcon;
