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
import { IS_DEMO_MODE } from '../services/apiUtils';

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

      // Demo version: Skip online users
      if (!IS_DEMO_MODE) {
        const onlineUsersData = await ChatService.getOnlineUsers();
        if (isComponentMountedRef.current) {
          setOnlineUsers(onlineUsersData || []);
        }
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
            setIsConnected(status === 'connected');
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

  const handleWebSocketMessage = useCallback(
    data => {
      if (!isComponentMountedRef.current) return;

      if (!data || !data.type) {
        return;
      }

      // Demo version: Only handle INITIAL_DATA
      if (data.type === 'INITIAL_DATA') {
        handleInitialData(data);
      }
      // All other message types are ignored in demo mode
    },
    []
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

    // Demo version: Skip online users
    if (!IS_DEMO_MODE && data.onlineUsers) {
      setOnlineUsers(data.onlineUsers);
    }
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

      try {
        await ChatService.sendChatMessage(receiverId, content, conversationId);
      } catch (error) {
        // Demo version: Remove temp message if send fails
        setMessages(prevMessages =>
          prevMessages.filter(m => m.id !== tempMessage.id)
        );
      }
    },
    []
  );

  const sendTypingIndicator = useCallback(
    (receiverId, conversationId, isTyping = true) => {
      if (!isComponentMountedRef.current || IS_DEMO_MODE) return;

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
      // Demo version: Always return false for online status
      if (IS_DEMO_MODE) return false;
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