import { useEffect, useRef, useState, useCallback } from 'react';
import { useChat } from '@/context/ChatContext';
import ChatList from './ChatList';
import ChatConversation from './ChatConversation';
import { useTranslation } from 'react-i18next';

const ChatModal = () => {
  const { t } = useTranslation();

  const {
    isModalOpen,
    setIsModalOpen,
    activeConversation,
    conversations,
    selectConversation,
    isConnected,
  } = useChat();

  const modalRef = useRef(null);
  const initializedRef = useRef(false);
  const [isMobileView, setIsMobileView] = useState(false);

  useEffect(() => {
    if (
      isModalOpen &&
      conversations.length > 0 &&
      !activeConversation &&
      !initializedRef.current
    ) {
      const sortedConversations = [...conversations].sort((a, b) => {
        if (!a.lastMessageTime) return 1;
        if (!b.lastMessageTime) return -1;
        return new Date(b.lastMessageTime) - new Date(a.lastMessageTime);
      });

      const conversationWithUnread = sortedConversations.find(
        c => c.unreadCount > 0
      );

      if (conversationWithUnread) {
        selectConversation(conversationWithUnread);
      } else if (sortedConversations.length > 0) {
        selectConversation(sortedConversations[0]);
      }

      initializedRef.current = true;
    }
  }, [isModalOpen, conversations, activeConversation, selectConversation]);

  useEffect(() => {
    if (!isModalOpen) {
      initializedRef.current = false;
    }
  }, [isModalOpen]);

  useEffect(() => {
    const handleClickOutside = event => {
      if (modalRef.current && !modalRef.current.contains(event.target)) {
        setIsModalOpen(false);
      }
    };

    if (isModalOpen) {
      document.addEventListener('mousedown', handleClickOutside);
      document.body.classList.add('overflow-hidden');
    } else {
      document.removeEventListener('mousedown', handleClickOutside);
      document.body.classList.remove('overflow-hidden');
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
      document.body.classList.remove('overflow-hidden');
    };
  }, [isModalOpen, setIsModalOpen]);

  useEffect(() => {
    const handleResize = () => {
      setIsMobileView(window.innerWidth < 768);
    };

    handleResize();
    window.addEventListener('resize', handleResize);

    return () => {
      window.removeEventListener('resize', handleResize);
    };
  }, []);

  const handleClose = useCallback(() => {
    setIsModalOpen(false);
  }, [setIsModalOpen]);

  const handleBackToList = useCallback(() => {
    selectConversation(null);
  }, [selectConversation]);

  if (!isModalOpen) {
    return null;
  }

  const getModalTitle = () => {
    if (isMobileView && activeConversation) {
      const participantName =
        activeConversation.participantName ||
        (activeConversation.participant1Id === activeConversation.senderId
          ? activeConversation.participant2Id
          : activeConversation.participant1Id);
      return t('chatModal.chatWith', 'Chat with {{name}}', {
        name: participantName,
      });
    }
    return t('chatModal.messages', 'Messages');
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-60 backdrop-blur-sm">
      <div
        ref={modalRef}
        className="bg-white rounded-xl shadow-2xl w-full max-w-4xl h-[80vh] max-h-[600px] flex flex-col overflow-hidden"
        style={{
          boxShadow:
            '0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)',
        }}
      >
        <div className="flex items-center justify-between p-4 border-b bg-gradient-to-r from-purple-500 to-purple-600 text-white">
          <div className="flex items-center">
            {isMobileView && activeConversation && (
              <button
                onClick={handleBackToList}
                className="mr-2 text-white hover:text-gray-200"
                aria-label={t(
                  'chatModal.backToConversations',
                  'Back to conversations list'
                )}
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  className="h-6 w-6"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M15 19l-7-7 7-7"
                  />
                </svg>
              </button>
            )}

            <h2 className="text-lg font-medium">{getModalTitle()}</h2>

            <div className="ml-2 flex items-center">
              <span
                className={`inline-block w-2 h-2 rounded-full mr-1 ${
                  isConnected ? 'bg-green-400' : 'bg-red-400'
                }`}
              />
              <span className="text-xs font-medium">
                {isConnected
                  ? t('chatModal.connected', 'Connected')
                  : t('chatModal.reconnecting', 'Reconnecting...')}
              </span>
            </div>
          </div>

          <button
            onClick={handleClose}
            className="text-white hover:text-gray-200 focus:outline-none transition-colors duration-200"
            aria-label={t('chatModal.closeChat', 'Close chat')}
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              className="h-6 w-6"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </button>
        </div>

        <div className="flex flex-1 overflow-hidden">
          <div
            className={`${
              isMobileView && activeConversation
                ? 'hidden'
                : isMobileView
                  ? 'w-full'
                  : 'w-1/3 border-r'
            }`}
          >
            <ChatList />
          </div>

          <div
            className={`${
              isMobileView && !activeConversation
                ? 'hidden'
                : isMobileView
                  ? 'w-full'
                  : 'w-2/3'
            } flex flex-col`}
          >
            <ChatConversation />
          </div>
        </div>
      </div>
    </div>
  );
};

export default ChatModal;
