import {
  createContext,
  useContext,
  useState,
  useEffect,
  useCallback,
  useRef,
} from 'react';
import { useAuth } from './AuthContext';
import { ChatService } from '../services/ChatService';
import { format, isToday, isYesterday } from 'date-fns';
import { useTranslation } from 'react-i18next';

const ChatContext = createContext({});

export const ChatProvider = ({ children }) => {
  const { t } = useTranslation();
  const { user, isAuthenticated, getToken } = useAuth();

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [conversations, setConversations] = useState([]);
  const [activeConversation, setActiveConversation] = useState(null);
  const [messages, setMessages] = useState([]);
  const [isTyping, setIsTyping] = useState({});
  const [availableUsers, setAvailableUsers] = useState([]);
  const [onlineUsers, setOnlineUsers] = useState([]);
  const [isConnected, setIsConnected] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [totalUnreadCount, setTotalUnreadCount] = useState(0);
  const [firstUnreadMessageId, setFirstUnreadMessageId] = useState(null);

  const typingTimeoutRef = useRef({});
  const isComponentMountedRef = useRef(true);
  const activeConversationRef = useRef(null);
  const messagesRef = useRef([]);
  const userRef = useRef(null);
  const isNearBottomRef = useRef(true);

  const functionRefs = useRef({
    markConversationAsRead: async () => {},
    calculateTotalUnreadCount: () => {},
  });

  useEffect(() => {
    isComponentMountedRef.current = true;
    return () => {
      isComponentMountedRef.current = false;
    };
  }, []);

  useEffect(() => {
    activeConversationRef.current = activeConversation;
  }, [activeConversation]);

  useEffect(() => {
    messagesRef.current = messages;
  }, [messages]);

  useEffect(() => {
    userRef.current = user;
  }, [user]);

  useEffect(() => {
    let wsConnected = false;

    const setupConnection = async () => {
      if (
          isAuthenticated &&
          user &&
          !wsConnected &&
          isComponentMountedRef.current
      ) {
        wsConnected = true;
        await connectWebSocket();

        fetchInitialData();
      }
    };

    if (isAuthenticated && user) {
      setupConnection();
    } else {
      disconnectWebSocket();
    }
  }, [isAuthenticated, user]);

  useEffect(() => {
    return () => {
      if (isComponentMountedRef.current === false) {
        disconnectWebSocket();
      }
    };
  }, []);

  const fetchInitialData = useCallback(async () => {
    if (!isComponentMountedRef.current) return;

    try {
      const conversationsData = await ChatService.getUserConversations();
      if (isComponentMountedRef.current) {
        setConversations(conversationsData.content || []);
      }

      const unreadCount = await ChatService.getUnreadCount();
      if (isComponentMountedRef.current) {
        setTotalUnreadCount(unreadCount);
      }

      const onlineUsersData = await ChatService.getOnlineUsers();
      if (isComponentMountedRef.current) {
        setOnlineUsers(onlineUsersData || []);
      }
    } catch {}
  }, []);

  const connectWebSocket = useCallback(async () => {
    if (!isComponentMountedRef.current) return;

    try {
      const token = await getToken();

      await ChatService.connect(
          token,
          handleConnect,
          handleWebSocketMessage,
          handleError,
          status => {
            if (isComponentMountedRef.current) {
              setIsConnected(status);
            }
          }
      );
    } catch {}
  }, [getToken]);

  const disconnectWebSocket = useCallback(() => {
    ChatService.disconnect();
    if (isComponentMountedRef.current) {
      setIsConnected(false);
    }
  }, []);

  const handleConnect = useCallback(() => {}, []);

  const handleError = useCallback(error => {}, []);

  const handleNewMessage = useCallback(message => {
    if (!isComponentMountedRef.current || !message) return;

    const currentActiveConversation = activeConversationRef.current;
    const currentUser = userRef.current;

    if (currentActiveConversation) {
      const messageConvId = String(message.conversationId || '');
      const activeConvId = String(currentActiveConversation.id || '');

      const isForActiveConversation = messageConvId === activeConvId;

      const isForTempConversation =
          !currentActiveConversation.id &&
          ((message.senderId === currentActiveConversation.participant1Id &&
                  message.receiverId === currentActiveConversation.participant2Id) ||
              (message.senderId === currentActiveConversation.participant2Id &&
                  message.receiverId === currentActiveConversation.participant1Id));

      if (isForActiveConversation || isForTempConversation) {
        setMessages(prevMessages => {
          const isFromCurrentUser = message.senderId === currentUser?.username;
          if (isFromCurrentUser) {
            const filteredMessages = prevMessages.filter(
                m => !String(m.id).startsWith('temp-')
            );
            const exists = filteredMessages.some(m => String(m.id) === String(message.id));
            if (exists) return filteredMessages;
            return [...filteredMessages, message].sort(
                (a, b) => new Date(a.timestamp) - new Date(b.timestamp)
            );
          }

          const exists = prevMessages.some(m => String(m.id) === String(message.id));
          if (exists) return prevMessages;

          return [...prevMessages, message].sort(
              (a, b) => new Date(a.timestamp) - new Date(b.timestamp)
          );
        });

        if (message.senderId !== currentUser?.username) {
          const messageConvId = String(message.conversationId || '');
          const activeConvId = String(currentActiveConversation.id || '');

          if (messageConvId === activeConvId && isNearBottomRef.current) {
            setTimeout(() => {
              if (message.conversationId && isComponentMountedRef.current) {
                functionRefs.current.markConversationAsRead(
                    message.conversationId
                );
              }
            }, 1000);
          }
        }

        if (
            isForTempConversation &&
            !currentActiveConversation.id &&
            message.conversationId
        ) {
          setActiveConversation(prev => ({
            ...prev,
            id: message.conversationId,
          }));
        }
      }
    }

    setConversations(prevConversations => {
      const conversationExists = prevConversations.some(
          c => String(c.id) === String(message.conversationId)
      );

      if (conversationExists) {
        return prevConversations.map(c => {
          if (String(c.id) === String(message.conversationId)) {
            const isIncoming = message.senderId !== currentUser?.username;

            const isActiveConversation =
                currentActiveConversation &&
                String(currentActiveConversation.id) ===
                String(message.conversationId) &&
                isNearBottomRef.current;

            const newUnreadCount =
                isIncoming && !isActiveConversation
                    ? (c.unreadCount || 0) + 1
                    : c.unreadCount || 0;

            const updatedMessages = c.messages ? [...c.messages] : [];

            if (message.senderId === currentUser?.username) {
              const tempIndex = updatedMessages.findIndex(
                  m => String(m.id).startsWith('temp-')
              );
              if (tempIndex !== -1) {
                updatedMessages.splice(tempIndex, 1);
              }
            }

            const messageExists = updatedMessages.some(
                m => String(m.id) === String(message.id)
            );
            if (!messageExists) {
              updatedMessages.push(message);
            }

            return {
              ...c,
              lastMessageContent: message.content,
              lastMessageTime: message.timestamp,
              unreadCount: newUnreadCount,
              messages: updatedMessages,
            };
          }
          return c;
        });
      } else if (message.conversationId) {
        const otherParticipantId =
            message.senderId !== currentUser?.username
                ? message.senderId
                : message.receiverId;

        const isIncoming = message.senderId !== currentUser?.username;

        const newConversation = {
          id: message.conversationId,
          participant1Id: currentUser?.username,
          participant2Id: otherParticipantId,
          lastMessageContent: message.content,
          lastMessageTime: message.timestamp,
          unreadCount: isIncoming ? 1 : 0,
          messages: [message],
        };

        return [...prevConversations, newConversation];
      }

      return prevConversations;
    });

    if (message.senderId !== currentUser?.username) {
      const isViewingConversation =
          activeConversationRef.current &&
          String(activeConversationRef.current.id) ===
          String(message.conversationId) &&
          isNearBottomRef.current;

      if (!isViewingConversation) {
        setTotalUnreadCount(prev => prev + 1);
      }
    }
  }, []);

  const handleWebSocketMessage = useCallback(
      data => {
        if (!isComponentMountedRef.current) return;

        if (!data || !data.type) {
          return;
        }

        switch (data.type) {
          case 'INITIAL_DATA':
            handleInitialData(data);
            break;

          case 'NEW_MESSAGE':
            handleNewMessage(data.message);
            break;

          case 'UNREAD_COUNT':
            handleUnreadCountUpdate(data);
            break;

          case 'TYPING_INDICATOR':
            handleTypingIndicator(data);
            break;

          case 'MESSAGE_READ_STATUS':
            handleMessageReadStatus(data);
            break;

          case 'USER_STATUS_CHANGE':
            handleUserStatusChange(data);
            break;

          case 'ONLINE_USERS':
            handleOnlineUsers(data.users);
            break;

          case 'CONVERSATION_UPDATE':
            handleConversationUpdate(data);
            break;

          case 'MESSAGE_SENT':
            if (data.message) {
              handleNewMessage(data.message);
            }
            break;

          case 'MESSAGES_MARKED_READ':
            break;

          default:
        }
      },
      [handleNewMessage]
  );

  const handleInitialData = useCallback(data => {
    if (!isComponentMountedRef.current) return;

    if (data.conversations) {
      setConversations(data.conversations);
    }

    if (data.unreadMessages) {
      setTotalUnreadCount(data.unreadMessages.length);
    }

    if (data.availableUsers && data.availableUsers.users) {
      setAvailableUsers(data.availableUsers.users);
    }

    if (data.onlineUsers) {
      setOnlineUsers(data.onlineUsers);
    }
  }, []);

  const handleConversationUpdate = useCallback(data => {
    if (!isComponentMountedRef.current) return;

    if (!data.conversationId) return;

    const conversationId = String(data.conversationId);

    if (isComponentMountedRef.current) {
      ChatService.getConversationById(conversationId)
          .then(updatedConversation => {
            setConversations(prevConversations => {
              return prevConversations.map(c =>
                  String(c.id) === conversationId ? updatedConversation : c
              );
            });
          })
          .catch(error => {});
    }
  }, []);

  const handleUnreadCountUpdate = useCallback(data => {
    if (!isComponentMountedRef.current) return;

    if (data.conversationId === null) {
      setTotalUnreadCount(data.unreadCount || 0);

      if (data.conversationBreakdown) {
        setConversations(prevConversations =>
            prevConversations.map(c => {
              const conversationId = String(c.id);
              if (data.conversationBreakdown[conversationId] !== undefined) {
                return {
                  ...c,
                  unreadCount: data.conversationBreakdown[conversationId],
                };
              }
              return c;
            })
        );
      }
      return;
    }

    const conversationId = String(data.conversationId);

    setConversations(prevConversations => {
      return prevConversations.map(c =>
          String(c.id) === conversationId
              ? { ...c, unreadCount: data.unreadCount }
              : c
      );
    });
  }, []);

  const handleTypingIndicator = useCallback(data => {
    if (!isComponentMountedRef.current) return;

    if (typingTimeoutRef.current[data.conversationId]) {
      clearTimeout(typingTimeoutRef.current[data.conversationId]);
    }

    setIsTyping(prev => ({
      ...prev,
      [data.conversationId]: {
        username: data.senderUsername,
        isTyping: data.isTyping,
      },
    }));

    if (data.isTyping) {
      typingTimeoutRef.current[data.conversationId] = setTimeout(() => {
        if (isComponentMountedRef.current) {
          setIsTyping(prev => ({
            ...prev,
            [data.conversationId]: {
              username: data.senderUsername,
              isTyping: false,
            },
          }));
        }
      }, 3000);
    }
  }, []);

  const handleMessageReadStatus = useCallback(data => {
    if (!isComponentMountedRef.current) return;

    setMessages(prevMessages =>
        prevMessages.map(m =>
            String(m.id) === String(data.messageId) ? { ...m, read: data.isRead } : m
        )
    );
  }, []);

  const handleUserStatusChange = useCallback(data => {
    if (!isComponentMountedRef.current) return;

    if (data.online) {
      setOnlineUsers(prev =>
          prev.includes(data.username) ? prev : [...prev, data.username]
      );
    } else {
      setOnlineUsers(prev =>
          prev.filter(username => username !== data.username)
      );
    }

    setConversations(prevConversations =>
        prevConversations.map(conv => {
          if (
              conv.participant1Id === data.username ||
              conv.participant2Id === data.username
          ) {
            return { ...conv, online: data.online };
          }
          return conv;
        })
    );
  }, []);

  const handleOnlineUsers = useCallback(users => {
    if (!isComponentMountedRef.current) return;
    setOnlineUsers(users || []);
  }, []);

  const calculateTotalUnreadCount = useCallback(() => {
    if (!isComponentMountedRef.current) return;

    const count = conversations.reduce(
        (total, conv) => total + (conv.unreadCount || 0),
        0
    );
    setTotalUnreadCount(count);
  }, [conversations]);

  useEffect(() => {
    functionRefs.current.calculateTotalUnreadCount = calculateTotalUnreadCount;
  }, [calculateTotalUnreadCount]);

  useEffect(() => {
    calculateTotalUnreadCount();
  }, [conversations, calculateTotalUnreadCount]);

  const openChat = useCallback(() => {
    if (!isComponentMountedRef.current) return;
    setIsModalOpen(true);
  }, []);

  const selectConversation = useCallback(async conversation => {
    if (!isComponentMountedRef.current) return;

    setActiveConversation(conversation);

    if (!conversation) {
      setMessages([]);
      return;
    }

    setIsLoading(true);
    try {
      const firstUnreadResponse = await ChatService.getFirstUnreadMessageId(
          conversation.id
      );
      const firstUnread = firstUnreadResponse.messageId;

      let messageList = [];

      if (conversation.messages && conversation.messages.length > 0) {
        messageList = [...conversation.messages];
      } else {
        const data = await ChatService.getConversationMessages(conversation.id);
        messageList = [...(data.content || [])].reverse();
      }

      if (!isComponentMountedRef.current) return;

      setMessages(messageList);

      setFirstUnreadMessageId(
          firstUnread && firstUnread > 0 ? firstUnread : null
      );

      if (conversation.unreadCount > 0 || (firstUnread && firstUnread > 0)) {
        setTimeout(() => {
          if (isComponentMountedRef.current && conversation.id) {
            functionRefs.current.markConversationAsRead(conversation.id);

            setConversations(prevConversations =>
                prevConversations.map(c =>
                    String(c.id) === String(conversation.id)
                        ? { ...c, unreadCount: 0 }
                        : c
                )
            );

            functionRefs.current.calculateTotalUnreadCount();
          }
        }, 1000);
      }
    } catch (error) {
    } finally {
      if (isComponentMountedRef.current) {
        setIsLoading(false);
      }
    }
  }, []);

  const sendMessage = useCallback(
      async (receiverId, content, conversationId = null) => {
        if (!isComponentMountedRef.current) return;

        const currentUser = userRef.current;

        const tempMessage = {
          id: `temp-${Date.now()}`,
          content,
          senderId: currentUser?.username,
          receiverId,
          conversationId,
          timestamp: new Date().toISOString(),
          read: false,
        };

        setMessages(prevMessages => [...prevMessages, tempMessage]);

        setConversations(prevConversations =>
            prevConversations.map(conv => {
              if (String(conv.id) === String(conversationId)) {
                const updatedMessages = [...(conv.messages || []), tempMessage];
                return {
                  ...conv,
                  lastMessageContent: content,
                  lastMessageTime: tempMessage.timestamp,
                  messages: updatedMessages,
                };
              }
              return conv;
            })
        );

        try {
          await ChatService.sendChatMessage(receiverId, content, conversationId);
        } catch (error) {
          setMessages(prevMessages =>
              prevMessages.filter(m => String(m.id) !== String(tempMessage.id))
          );

          setConversations(prevConversations =>
              prevConversations.map(conv => {
                if (String(conv.id) === String(conversationId)) {
                  const updatedMessages = (conv.messages || []).filter(
                      m => String(m.id) !== String(tempMessage.id)
                  );
                  return {
                    ...conv,
                    messages: updatedMessages,
                  };
                }
                return conv;
              })
          );
        }
      },
      []
  );

  const sendTypingIndicator = useCallback(
      (receiverId, conversationId, isTyping = true) => {
        if (!isComponentMountedRef.current) return;

        try {
          ChatService.sendTypingIndicator(receiverId, conversationId, isTyping);

          if (isTyping) {
            if (typingTimeoutRef.current[conversationId]) {
              clearTimeout(typingTimeoutRef.current[conversationId]);
            }

            typingTimeoutRef.current[conversationId] = setTimeout(() => {
              if (isComponentMountedRef.current) {
                ChatService.sendTypingIndicator(
                    receiverId,
                    conversationId,
                    false
                );
              }
            }, 5000);
          }
        } catch (error) {}
      },
      []
  );

  const markConversationAsRead = useCallback(async conversationId => {
    if (!isComponentMountedRef.current || !conversationId) return;

    try {
      await ChatService.markAsRead(conversationId);

      setMessages(prev => prev.map(m => ({ ...m, read: true })));

      setConversations(prev =>
          prev.map(c =>
              String(c.id) === String(conversationId) ? { ...c, unreadCount: 0 } : c
          )
      );

      functionRefs.current.calculateTotalUnreadCount();
    } catch (error) {}
  }, []);

  useEffect(() => {
    functionRefs.current.markConversationAsRead = markConversationAsRead;
  }, [markConversationAsRead]);

  const openChatWithUser = useCallback(
      username => {
        if (!isComponentMountedRef.current) return;

        const currentUser = userRef.current;

        const existingConversation = conversations.find(
            c =>
                (c.participant1Id === username &&
                    c.participant2Id === currentUser?.username) ||
                (c.participant1Id === currentUser?.username &&
                    c.participant2Id === username)
        );

        if (existingConversation) {
          selectConversation(existingConversation);
        } else {
          const tempConversation = {
            id: null,
            participant1Id: currentUser?.username,
            participant2Id: username,
            lastMessageContent: '',
            lastMessageTime: new Date().toISOString(),
            unreadCount: 0,
            messages: [],
          };

          setActiveConversation(tempConversation);
          setMessages([]);
        }
      },
      [conversations, selectConversation]
  );

  const isUserOnline = useCallback(
      username => {
        return onlineUsers.includes(username);
      },
      [onlineUsers]
  );

  const formatMessageTime = useCallback(
      timestamp => {
        if (!timestamp) return '';

        const date = new Date(timestamp);

        if (isToday(date)) {
          return format(date, 'HH:mm');
        } else if (isYesterday(date)) {
          return t('chatContext.yesterday', 'Yesterday');
        } else if (date > new Date(Date.now() - 7 * 24 * 60 * 60 * 1000)) {
          return format(date, 'EEEE');
        }
        return format(date, 'dd/MM/yyyy');
      },
      [t]
  );

  const contextValue = {
    isModalOpen,
    setIsModalOpen,
    conversations,
    activeConversation,
    messages,
    setMessages,
    isTyping,
    availableUsers,
    onlineUsers,
    isConnected,
    isLoading,
    totalUnreadCount,
    firstUnreadMessageId,
    openChat,
    selectConversation,
    sendMessage,
    sendTypingIndicator,
    markConversationAsRead,
    openChatWithUser,
    isUserOnline,
    formatMessageTime,
  };

  return (
      <ChatContext.Provider value={contextValue}>{children}</ChatContext.Provider>
  );
};

export const useChat = () => {
  const context = useContext(ChatContext);
  const { t } = useTranslation();
  if (context === undefined) {
    throw new Error(
        t(
            'chatContext.errorUseChat',
            'useChat must be used within a ChatProvider'
        )
    );
  }
  return context;
};