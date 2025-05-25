import { useChat } from '@/context/ChatContext';
import { useTranslation } from 'react-i18next';

const ChatButton = () => {
  const { t } = useTranslation();
  const { isModalOpen, setIsModalOpen, getTotalUnreadCount } = useChat();

  const unreadCount = getTotalUnreadCount();

  return (
    <button
      onClick={() => setIsModalOpen(!isModalOpen)}
      className="flex items-center bg-white px-3 py-1.5 text-sm text-gray-700 hover:bg-gray-100 rounded-md border border-gray-300 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500"
    >
      <svg
        xmlns="http://www.w3.org/2000/svg"
        className="mr-1.5 h-4 w-4 text-gray-500"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={2}
          d="M8 12h.01M12 12h.01M16 12h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z"
        />
      </svg>

      {t('chatButton.chat', 'Chat')}

      {unreadCount > 0 && (
        <span className="ml-1.5 bg-red-500 text-white text-xs rounded-full w-4 h-4 flex items-center justify-center">
          {unreadCount > 9 ? '9+' : unreadCount}
        </span>
      )}
    </button>
  );
};

export default ChatButton;
