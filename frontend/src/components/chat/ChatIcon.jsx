import React, { useEffect, useState, useCallback } from 'react';
import { useChat } from '@/context/ChatContext';
import { useTranslation } from 'react-i18next';

const ChatIcon = () => {
  const { t } = useTranslation();

  const { openChat, totalUnreadCount, isConnected } = useChat();

  const [animateCount, setAnimateCount] = useState(false);
  const [prevCount, setPrevCount] = useState(0);

  useEffect(() => {
    if (totalUnreadCount > prevCount) {
      setAnimateCount(true);
      const timer = window.setTimeout(() => {
        setAnimateCount(false);
      }, 1000);

      return () => window.clearTimeout(timer);
    }

    setPrevCount(totalUnreadCount);
  }, [totalUnreadCount, prevCount]);

  const handleClick = useCallback(() => {
    openChat();
  }, [openChat]);

  const getBadgeSize = () => {
    if (totalUnreadCount > 99) return 'h-6 w-auto min-w-6 px-1';
    if (totalUnreadCount > 9) return 'h-5 w-5';
    return 'h-5 w-5';
  };

  const getFontSize = () => {
    if (totalUnreadCount > 99) return 'text-xs';
    return 'text-xs';
  };

  return (
    <button
      onClick={handleClick}
      className="relative rounded-full text-gray-500 hover:text-purple-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500 transition-colors duration-200 bg-white p-2 shadow-md"
      title={
        totalUnreadCount > 0
          ? t('chatIcon.chatWithUnread', 'Chat ({{count}} unread)', {
              count: totalUnreadCount,
            })
          : t('chatIcon.chat', 'Chat')
      }
      aria-label={
        totalUnreadCount > 0
          ? t('chatIcon.openChatWithUnread', 'Open chat ({{count}} unread)', {
              count: totalUnreadCount,
            })
          : t('chatIcon.openChat', 'Open chat')
      }
    >
      <div
        className="absolute top-0 left-0 w-2 h-2 rounded-full border border-white"
        style={{
          backgroundColor: isConnected ? '#10B981' : '#F87171',
          transform: 'translate(-25%, -25%)',
        }}
      />

      <svg
        xmlns="http://www.w3.org/2000/svg"
        className="h-6 w-6"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
        aria-hidden="true"
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={2}
          d="M8 12h.01M12 12h.01M16 12h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z"
        />
      </svg>

      {totalUnreadCount > 0 && (
        <span
          className={`absolute -top-1 -right-1 flex ${getBadgeSize()} ${
            animateCount ? 'scale-125' : ''
          } transition-transform duration-300`}
        >
          <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-purple-400 opacity-75" />
          <span
            className={`relative inline-flex rounded-full ${getBadgeSize()} bg-purple-600 ${getFontSize()} text-white justify-center items-center font-medium`}
          >
            {totalUnreadCount > 99 ? '99+' : totalUnreadCount}
          </span>
        </span>
      )}
    </button>
  );
};

export default React.memo(ChatIcon);
