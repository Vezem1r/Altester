import React, { useState, useMemo, useCallback } from 'react';
import { useChat } from '@/context/ChatContext';
import { useAuth } from '@/context/AuthContext';
import { useTranslation } from 'react-i18next';

const ChatList = () => {
  const { t } = useTranslation();

  const {
    conversations,
    availableUsers,
    selectConversation,
    activeConversation,
    formatMessageTime,
    openChatWithUser,
    isUserOnline,
  } = useChat();

  const { user } = useAuth();
  const [searchQuery, setSearchQuery] = useState('');
  const [showNewChat, setShowNewChat] = useState(false);

  const filteredConversations = useMemo(() => {
    if (!searchQuery) return conversations;

    const query = searchQuery.toLowerCase();
    return conversations.filter(conversation => {
      const participantName =
        conversation.participantName ||
        (conversation.participant1Id === user?.username
          ? conversation.participant2Id
          : conversation.participant1Id);

      return participantName.toLowerCase().includes(query);
    });
  }, [conversations, searchQuery, user]);

  const filteredAvailableUsers = useMemo(() => {
    if (!showNewChat) return [];

    const query = searchQuery.toLowerCase();
    const existingUserIds = conversations.map(c =>
      c.participant1Id === user?.username ? c.participant2Id : c.participant1Id
    );

    return availableUsers.filter(availableUser => {
      return (
        !existingUserIds.includes(availableUser.username) &&
        availableUser.username.toLowerCase().includes(query)
      );
    });
  }, [availableUsers, conversations, searchQuery, showNewChat, user]);

  const handleSearchChange = useCallback(e => {
    setSearchQuery(e.target.value);
  }, []);

  const toggleNewChat = useCallback(() => {
    setShowNewChat(prev => !prev);
  }, []);

  const handleStartNewChat = useCallback(
    username => {
      openChatWithUser(username);
      setShowNewChat(false);
    },
    [openChatWithUser]
  );

  const getParticipantName = useCallback(
    conversation => {
      const participantName =
        conversation.participantName ||
        (conversation.participant1Id === user?.username
          ? conversation.participant2Id
          : conversation.participant1Id);

      return participantName;
    },
    [user]
  );

  return (
    <div className="flex flex-col h-full">
      <div className="p-4 border-b bg-gray-50">
        <div className="relative">
          <input
            type="text"
            placeholder={
              showNewChat
                ? t('chatList.searchUsers', 'Search users...')
                : t('chatList.searchConversations', 'Search conversations...')
            }
            value={searchQuery}
            onChange={handleSearchChange}
            className="w-full p-2 pl-9 pr-4 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent shadow-sm transition-all duration-200"
            aria-label="Search"
          />
          <svg
            className="absolute left-3 top-2.5 h-4 w-4 text-gray-400"
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
            />
          </svg>
        </div>

        <button
          onClick={toggleNewChat}
          className="mt-3 w-full py-2 px-4 bg-gradient-to-r from-purple-600 to-purple-700 text-white rounded-lg hover:from-purple-700 hover:to-purple-800 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-offset-2 shadow-sm transition-all duration-200 flex items-center justify-center"
        >
          {showNewChat ? (
            <>
              <svg
                xmlns="http://www.w3.org/2000/svg"
                className="h-4 w-4 mr-2"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M10 19l-7-7m0 0l7-7m-7 7h18"
                />
              </svg>
              {t('chatList.backToConversations', 'Back to Conversations')}
            </>
          ) : (
            <>
              <svg
                xmlns="http://www.w3.org/2000/svg"
                className="h-4 w-4 mr-2"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 4v16m8-8H4"
                />
              </svg>
              {t('chatList.newChat', 'New Chat')}
            </>
          )}
        </button>
      </div>

      {showNewChat && (
        <div className="overflow-y-auto">
          <h3 className="p-3 text-sm font-medium text-gray-500 bg-gray-50 sticky top-0">
            {t('chatList.newConversation', 'New Conversation')}
          </h3>
          {filteredAvailableUsers.length === 0 ? (
            <div className="px-4 py-6 text-center">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                className="h-10 w-10 mx-auto text-gray-300 mb-2"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={1.5}
                  d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
                />
              </svg>
              <p className="text-sm text-gray-500">
                {searchQuery
                  ? t('chatList.noMatchingUsers', 'No matching users found')
                  : t(
                      'chatList.noAvailableContacts',
                      'No available contacts found'
                    )}
              </p>
            </div>
          ) : (
            filteredAvailableUsers.map(availableUser => (
              <div
                key={availableUser.username}
                onClick={() => handleStartNewChat(availableUser.username)}
                className="flex items-center px-4 py-3 hover:bg-purple-50 cursor-pointer transition-colors duration-200 border-b border-gray-100"
              >
                <div className="w-10 h-10 bg-gradient-to-br from-purple-500 to-purple-700 rounded-full flex items-center justify-center text-white font-medium mr-3 shadow-sm">
                  {availableUser.username.charAt(0).toUpperCase()}
                </div>
                <div className="flex-1">
                  <div className="flex items-center">
                    <h4 className="font-medium text-gray-900">
                      {availableUser.username}
                    </h4>
                    {isUserOnline(availableUser.username) && (
                      <span className="ml-2 inline-block w-2 h-2 bg-green-500 rounded-full" />
                    )}
                  </div>
                  <p className="text-xs text-gray-500">{availableUser.role}</p>
                </div>
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  className="h-5 w-5 text-gray-400"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M9 5l7 7-7 7"
                  />
                </svg>
              </div>
            ))
          )}
        </div>
      )}

      <div className={`flex-1 overflow-y-auto ${showNewChat ? 'hidden' : ''}`}>
        <h3 className="p-3 text-sm font-medium text-gray-500 bg-gray-50 sticky top-0 border-b">
          {t('chatList.conversations', 'Conversations')}
        </h3>
        {filteredConversations.length === 0 ? (
          <div className="px-4 py-6 text-center">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              className="h-10 w-10 mx-auto text-gray-300 mb-2"
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
            <p className="text-sm text-gray-500">
              {searchQuery
                ? t(
                    'chatList.noMatchingConversations',
                    'No matching conversations'
                  )
                : t('chatList.noConversations', 'No conversations yet')}
            </p>
            <p className="text-xs text-gray-400 mt-1">
              {t(
                'chatList.startChatInstruction',
                'Start a new chat to begin messaging'
              )}
            </p>
          </div>
        ) : (
          filteredConversations.map(conversation => {
            const isActive = activeConversation?.id === conversation.id;
            const participantName = getParticipantName(conversation);
            const isOnline = isUserOnline(participantName);

            return (
              <div
                key={conversation.id}
                onClick={() => selectConversation(conversation)}
                className={`flex items-center px-4 py-3 cursor-pointer border-b border-gray-100 transition-colors duration-200 ${
                  isActive ? 'bg-purple-50' : 'hover:bg-gray-50'
                }`}
              >
                <div
                  className={`w-10 h-10 rounded-full flex items-center justify-center text-white font-medium mr-3 shadow-sm ${
                    isActive
                      ? 'bg-gradient-to-br from-purple-600 to-purple-800'
                      : 'bg-gradient-to-br from-purple-500 to-purple-700'
                  }`}
                >
                  {participantName.charAt(0).toUpperCase()}
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex justify-between items-center">
                    <div className="flex items-center">
                      <h4
                        className={`font-medium truncate ${isActive ? 'text-purple-900' : 'text-gray-900'}`}
                      >
                        {participantName}
                      </h4>
                      {isOnline && (
                        <span className="ml-2 inline-block w-2 h-2 bg-green-500 rounded-full" />
                      )}
                    </div>
                    <span className="text-xs text-gray-500 ml-2 whitespace-nowrap">
                      {conversation.lastMessageTime
                        ? formatMessageTime(conversation.lastMessageTime)
                        : ''}
                    </span>
                  </div>
                  <p
                    className={`text-sm truncate ${
                      isActive ? 'text-purple-700' : 'text-gray-500'
                    }`}
                  >
                    {conversation.lastMessageContent ||
                      t('chatList.noMessagesYet', 'No messages yet')}
                  </p>
                </div>
                {conversation.unreadCount > 0 && (
                  <div className="ml-2 bg-purple-600 text-white text-xs font-medium rounded-full min-w-5 h-5 flex items-center justify-center px-1.5">
                    {conversation.unreadCount > 99
                      ? '99+'
                      : conversation.unreadCount}
                  </div>
                )}
              </div>
            );
          })
        )}
      </div>
    </div>
  );
};

export default React.memo(ChatList);
