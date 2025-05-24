import { useContext, useCallback } from 'react';
import ChatContext from '@/context/ChatContext';

const useChat = () => {
  const context = useContext(ChatContext);

  if (!context) {
    throw new Error('Error with Chat');
  }

  const { openChatWithUser, isModalOpen, setIsModalOpen, getTotalUnreadCount } =
    context;

  const startChatWithUser = useCallback(
    userId => {
      openChatWithUser(userId);
    },
    [openChatWithUser]
  );

  const toggleChatModal = useCallback(() => {
    setIsModalOpen(!isModalOpen);
  }, [isModalOpen, setIsModalOpen]);

  const getUnreadCount = useCallback(() => {
    return getTotalUnreadCount();
  }, [getTotalUnreadCount]);

  return {
    ...context,
    startChatWithUser,
    toggleChatModal,
    getUnreadCount,
  };
};

export default useChat;
