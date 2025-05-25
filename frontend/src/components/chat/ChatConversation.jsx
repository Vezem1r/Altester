import { useEffect, useRef, useState, useCallback } from 'react';
import { useChat } from '@/context/ChatContext';
import { useAuth } from '@/context/AuthContext';
import ChatMessage from './ChatMessage';
import ChatInput from './ChatInput';
import { ChatService } from '@/services/ChatService';
import { useTranslation } from 'react-i18next';

const ChatConversation = () => {
  const { t } = useTranslation();

  const {
    messages,
    setMessages,
    activeConversation,
    isTyping,
    sendMessage,
    sendTypingIndicator,
    markConversationAsRead,
    isLoading,
    firstUnreadMessageId,
    isUserOnline,
  } = useChat();

  const { user } = useAuth();

  const messagesEndRef = useRef(null);
  const unreadMessageRef = useRef(null);
  const messagesContainerRef = useRef(null);

  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);

  const initialScrollCompleteRef = useRef(false);
  const scrollPositionRef = useRef(0);
  const shouldScrollToBottomRef = useRef(true);
  const isNearBottomRef = useRef(true);
  const messagesRef = useRef(messages);

  useEffect(() => {
    messagesRef.current = messages;
  }, [messages]);

  useEffect(() => {
    if (activeConversation) {
      initialScrollCompleteRef.current = false;
      shouldScrollToBottomRef.current = true;
      isNearBottomRef.current = true;
      setPage(0);
      setHasMore(true);
    }
  }, [activeConversation?.id]);

  useEffect(() => {
    if (!messagesContainerRef.current || messages.length === 0) return;

    const isNewMessage = messages.length > messagesRef.current.length;

    if (isNewMessage && isNearBottomRef.current) {
      shouldScrollToBottomRef.current = true;
    }

    if (!initialScrollCompleteRef.current) {
      window.setTimeout(() => {
        if (firstUnreadMessageId && unreadMessageRef.current) {
          unreadMessageRef.current.scrollIntoView({ behavior: 'auto' });
        } else if (messagesEndRef.current) {
          messagesEndRef.current.scrollIntoView({ behavior: 'auto' });
        }
        initialScrollCompleteRef.current = true;
      }, 100);
    } else if (shouldScrollToBottomRef.current) {
      window.setTimeout(() => {
        if (messagesEndRef.current) {
          messagesEndRef.current.scrollIntoView({ behavior: 'smooth' });
        }
        shouldScrollToBottomRef.current = false;
      }, 100);
    }

    if (isNearBottomRef.current && activeConversation?.id) {
      const hasUnread = messages.some(
        msg => !msg.read && msg.senderId !== user?.username
      );
      if (hasUnread) {
        window.setTimeout(() => {
          markConversationAsRead(activeConversation.id);
        }, 300);
      }
    }
  }, [
    messages,
    firstUnreadMessageId,
    activeConversation,
    user?.username,
    markConversationAsRead,
  ]);

  const checkIfNearBottom = useCallback(() => {
    if (!messagesContainerRef.current) return true;

    const container = messagesContainerRef.current;
    const threshold = 150;

    const isNearBottom =
      container.scrollHeight - container.scrollTop - container.clientHeight <=
      threshold;

    isNearBottomRef.current = isNearBottom;
    return isNearBottom;
  }, []);

  const handleScroll = useCallback(
    async e => {
      const container = e.target;

      scrollPositionRef.current = container.scrollTop;

      const isAtBottom = checkIfNearBottom();

      if (isAtBottom && activeConversation?.id) {
        const hasUnread = messagesRef.current.some(
          msg => !msg.read && msg.senderId !== user?.username
        );
        if (hasUnread) {
          markConversationAsRead(activeConversation.id);
        }
      }

      if (
        container.scrollTop < 50 &&
        !isLoadingMore &&
        hasMore &&
        activeConversation?.id
      ) {
        setIsLoadingMore(true);

        try {
          const nextPage = page + 1;
          const response = await ChatService.getConversationMessages(
            activeConversation.id,
            nextPage,
            20
          );

          if (response && response.content && response.content.length > 0) {
            const scrollPos = container.scrollHeight - container.scrollTop;

            setMessages(prevMessages => {
              const newMessages = response.content.filter(
                newMsg => !prevMessages.some(oldMsg => oldMsg.id === newMsg.id)
              );

              return [...newMessages.reverse(), ...prevMessages];
            });

            setPage(nextPage);

            window.setTimeout(() => {
              if (messagesContainerRef.current) {
                messagesContainerRef.current.scrollTop =
                  messagesContainerRef.current.scrollHeight - scrollPos;
              }
            }, 100);
          } else {
            setHasMore(false);
          }
        } finally {
          setIsLoadingMore(false);
        }
      }
    },
    [
      activeConversation,
      checkIfNearBottom,
      hasMore,
      isLoadingMore,
      markConversationAsRead,
      page,
      setMessages,
      user?.username,
    ]
  );

  const handleSendMessage = useCallback(
    content => {
      if (!activeConversation) return;

      const receiverId =
        activeConversation.participant1Id === user?.username
          ? activeConversation.participant2Id
          : activeConversation.participant1Id;

      shouldScrollToBottomRef.current = true;

      sendMessage(receiverId, content, activeConversation.id);
    },
    [activeConversation, sendMessage, user?.username]
  );

  const handleTyping = useCallback(
    (isTyping = true) => {
      if (!activeConversation) return;

      const receiverId =
        activeConversation.participant1Id === user?.username
          ? activeConversation.participant2Id
          : activeConversation.participant1Id;

      sendTypingIndicator(receiverId, activeConversation.id, isTyping);
    },
    [activeConversation, sendTypingIndicator, user?.username]
  );

  const getOtherParticipant = useCallback(() => {
    if (!activeConversation || !user) return '';

    return activeConversation.participant1Id === user.username
      ? activeConversation.participant2Id
      : activeConversation.participant1Id;
  }, [activeConversation, user]);

  const otherUserIsTyping =
    activeConversation &&
    isTyping[activeConversation.id] &&
    isTyping[activeConversation.id].username !== user?.username &&
    isTyping[activeConversation.id].isTyping;

  const otherParticipant = getOtherParticipant();
  const isOtherParticipantOnline =
    otherParticipant && isUserOnline(otherParticipant);

  if (!activeConversation) {
    return (
      <div className="flex flex-col items-center justify-center h-full text-gray-500 p-4 text-center">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          className="h-16 w-16 text-purple-200 mb-2"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={1.5}
            d="M8 12h.01M12 12h.01M16 12h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z"
          />
        </svg>
        <p className="text-lg font-medium mb-1">
          {t('chatConversation.selectConversation', 'Select a conversation')}
        </p>
        <p className="text-sm text-gray-400">
          {t(
            'chatConversation.chooseContactInstruction',
            'Choose a contact to start chatting'
          )}
        </p>
      </div>
    );
  }

  return (
    <div className="flex flex-col h-full bg-gray-50">
      <div className="p-3 bg-white border-b flex items-center justify-between">
        <div className="flex items-center">
          <div className="w-9 h-9 rounded-full flex items-center justify-center text-white font-medium mr-3 shadow-sm bg-gradient-to-br from-purple-500 to-purple-700">
            {otherParticipant.charAt(0).toUpperCase()}
          </div>
          <div>
            <h3 className="font-medium text-gray-900">{otherParticipant}</h3>
            <div className="flex items-center text-xs">
              <span
                className={`inline-block w-2 h-2 rounded-full mr-1 ${
                  isOtherParticipantOnline ? 'bg-green-500' : 'bg-gray-400'
                }`}
              />
              <span className="text-gray-500">
                {isOtherParticipantOnline
                  ? t('chatConversation.online', 'Online')
                  : t('chatConversation.offline', 'Offline')}
              </span>
            </div>
          </div>
        </div>
      </div>

      <div
        ref={messagesContainerRef}
        className="flex-1 overflow-y-auto p-4 bg-white"
        onScroll={handleScroll}
      >
        {isLoadingMore && (
          <div className="flex justify-center py-2">
            <div className="animate-pulse flex space-x-2">
              <div className="h-2 w-2 bg-purple-400 rounded-full" />
              <div className="h-2 w-2 bg-purple-400 rounded-full" />
              <div className="h-2 w-2 bg-purple-400 rounded-full" />
            </div>
          </div>
        )}

        {isLoading && !isLoadingMore ? (
          <div className="flex flex-col items-center justify-center h-full">
            <div className="animate-pulse flex space-x-2 mb-2">
              <div className="h-3 w-3 bg-purple-400 rounded-full" />
              <div className="h-3 w-3 bg-purple-400 rounded-full" />
              <div className="h-3 w-3 bg-purple-400 rounded-full" />
            </div>
            <p className="text-sm text-gray-500">
              {t('chatConversation.loadingMessages', 'Loading messages...')}
            </p>
          </div>
        ) : messages.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-full text-gray-500 p-4 text-center">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              className="h-16 w-16 text-purple-200 mb-2"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={1.5}
                d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z"
              />
            </svg>
            <p className="text-lg font-medium mb-1">
              {t('chatConversation.startConversation', 'Start a conversation')}
            </p>
            <p className="text-sm text-gray-400">
              {t(
                'chatConversation.sendMessageInstruction',
                'Send a message to begin chatting'
              )}
            </p>
          </div>
        ) : (
          <>
            {messages.map((message, index) => (
              <div
                key={message.id || `temp-${index}`}
                ref={
                  message.id === firstUnreadMessageId ? unreadMessageRef : null
                }
              >
                {index > 0 &&
                  new Date(message.timestamp).toDateString() !==
                    new Date(messages[index - 1].timestamp).toDateString() && (
                    <div className="flex items-center justify-center my-3">
                      <div className="border-t border-gray-200 flex-grow" />
                      <span className="mx-2 text-xs font-medium text-gray-500 bg-gray-100 px-2 py-1 rounded">
                        {new Date(message.timestamp).toLocaleDateString()}
                      </span>
                      <div className="border-t border-gray-200 flex-grow" />
                    </div>
                  )}

                {message.id === firstUnreadMessageId && (
                  <div className="flex items-center justify-center my-2">
                    <div className="border-t border-purple-300 flex-grow" />
                    <span className="mx-2 text-xs font-medium text-purple-600 bg-purple-100 px-2 py-1 rounded">
                      {t('chatConversation.newMessages', 'New Messages')}
                    </span>
                    <div className="border-t border-purple-300 flex-grow" />
                  </div>
                )}

                <ChatMessage
                  message={message}
                  isOwnMessage={message.senderId === user?.username}
                />
              </div>
            ))}

            {otherUserIsTyping && (
              <div className="flex items-center text-gray-500 text-sm ml-2 mb-2">
                <div className="typing-indicator mr-2">
                  <span />
                  <span />
                  <span />
                </div>
                <span className="text-purple-600 font-medium">
                  {t('chatConversation.typing', 'Typing...')}
                </span>
              </div>
            )}

            <div ref={messagesEndRef} />
          </>
        )}
      </div>

      <div className="border-t p-3 bg-white">
        <ChatInput onSendMessage={handleSendMessage} onTyping={handleTyping} />
      </div>

      <style
        dangerouslySetInnerHTML={{
          __html: `
        .typing-indicator {
          display: inline-flex;
          align-items: center;
        }
        
        .typing-indicator span {
          height: 8px;
          width: 8px;
          margin-right: 4px;
          background-color: #a855f7;
          border-radius: 50%;
          display: inline-block;
          opacity: 0.6;
          animation: typing 1.4s infinite both;
        }
        
        .typing-indicator span:nth-child(2) {
          animation-delay: 0.2s;
        }
        
        .typing-indicator span:nth-child(3) {
          animation-delay: 0.4s;
        }
        
        @keyframes typing {
          0% {
            transform: translateY(0);
          }
          50% {
            transform: translateY(-5px);
          }
          100% {
            transform: translateY(0);
          }
        }
      `,
        }}
      />
    </div>
  );
};

export default ChatConversation;
