import {
  createContext,
  useState,
  useContext,
  useEffect,
  useCallback,
} from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { NotificationService } from '@/services/NotificationService';
import { useTranslation } from 'react-i18next';
import { IS_DEMO_MODE } from '@/services/apiUtils';

const API_BASE_URL = import.meta.env.VITE_NOTIFICATION_URL;

const NotificationContext = createContext();

export const NotificationProvider = ({ children }) => {
  const { t } = useTranslation();

  const formatNotificationTime = timestamp => {
    const date = new Date(timestamp);
    const now = new Date();
    const diff = now - date;

    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);

    if (minutes < 1) return t('notificationContext.justNow', 'Just now');
    if (minutes < 60)
      return t('notificationContext.minAgo', '{{minutes}} min ago', {
        minutes,
      });
    if (hours < 24)
      return t('notificationContext.hoursAgo', '{{hours}} h ago', { hours });
    if (days < 7) {
      return t('notificationContext.daysAgo', '{{days}} day{{plural}}', {
        days,
        plural: days > 1 ? 's' : '',
      });
    }

    return date.toLocaleDateString();
  };

  const [client, setClient] = useState(null);
  const [connectionStatus, setConnectionStatus] = useState('disconnected');
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [isInitialized, setIsInitialized] = useState(false);

  const initializeWebSocket = useCallback(() => {
    const token = localStorage.getItem('token');

    if (!token) {
      setConnectionStatus('error');
      return null;
    }

    const socket = new SockJS(`${API_BASE_URL}/ws`);
    const stompClient = new Client({
      webSocketFactory: () => socket,
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      onConnect: () => {
        setConnectionStatus('connected');
        setIsInitialized(true);

        stompClient.subscribe(
          '/user/queue/notifications',
          message => {
            try {
              const data = JSON.parse(message.body);

              // Demo version: Only handle INITIAL_DATA
              if (data.type === 'INITIAL_DATA') {
                if (data.unreadNotifications) {
                  setNotifications(data.unreadNotifications);
                }
                if (data.unreadCount !== undefined) {
                  setUnreadCount(data.unreadCount);
                }
              }
            } catch (error) {}
          },
          { Authorization: `Bearer ${token}` }
        );

        stompClient.publish({
          destination: '/app/notifications.connect',
          headers: {
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify({}),
        });
      },
      onStompError: () => {
        setConnectionStatus('error');
      },
      onWebSocketClose: () => {
        setConnectionStatus('disconnected');
        setTimeout(() => reconnect(), 5000);
      },
      onWebSocketError: () => {
        setConnectionStatus('error');
        setTimeout(() => reconnect(), 5000);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    try {
      stompClient.activate();
    } catch (error) {
      setConnectionStatus('error');
      return null;
    }

    setClient(stompClient);
    return stompClient;
  }, [t]);

  const reconnect = useCallback(() => {
    if (client) {
      try {
        client.deactivate();
      } catch (error) {}
    }

    setConnectionStatus('disconnected');
    setIsInitialized(false);

    initializeWebSocket();
  }, [client, initializeWebSocket]);

  useEffect(() => {
    const wsClient = initializeWebSocket();

    return () => {
      if (wsClient) {
        wsClient.deactivate();
      }
    };
  }, [initializeWebSocket]);

  const markAsRead = useCallback(async notificationId => {
    try {
      await NotificationService.markAsRead(notificationId);

      setNotifications(prev =>
        prev.map(notification =>
          notification.id === notificationId
            ? { ...notification, read: true }
            : notification
        )
      );

      setUnreadCount(prev => Math.max(0, prev - 1));
    } catch (error) {
    }
  }, []);

  const markAllAsRead = useCallback(async () => {
    try {
      await NotificationService.markAllAsRead();

      setNotifications(prev =>
        prev.map(notification => ({ ...notification, read: true }))
      );

      setUnreadCount(0);
    } catch (error) {
    }
  }, []);

  const handleNotificationClick = useCallback(
    notification => {
      if (!notification.read) {
        markAsRead(notification.id);
      }
    },
    [markAsRead]
  );

  const contextValue = {
    notifications,
    unreadCount,
    connectionStatus,
    isInitialized,

    markAsRead,
    markAllAsRead,
    handleNotificationClick,
    reconnect,
    formatNotificationTime,
  };

  return (
    <NotificationContext.Provider value={contextValue}>
      {children}
    </NotificationContext.Provider>
  );
};

export const useNotifications = () => {
  const context = useContext(NotificationContext);
  const { t } = useTranslation();

  if (!context) {
    throw new Error(
      t(
        'notificationContext.errorUseNotifications',
        'useNotifications must be used within a NotificationProvider'
      )
    );
  }

  return context;
};

export default NotificationContext;