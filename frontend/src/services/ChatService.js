import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import {
  createAuthAxios,
  handleApiError,
  buildUrlWithParams,
} from './apiUtils';

const API_BASE_URL = import.meta.env.VITE_CHAT_URL;

export class ChatService {
  static stompClient = null;
  static subscriptions = [];
  static connectionPromise = null;
  static reconnectAttempt = 0;
  static maxReconnectAttempts = 5;
  static reconnectDelay = 2000;
  static lastDisconnectTimestamp = null;
  static isExplicitDisconnect = false;

  static connect(
    token,
    onConnect,
    onMessage,
    onConnectionError,
    onConnectionStatus
  ) {
    if (this.connectionPromise) {
      return this.connectionPromise;
    }

    onConnectionStatus('connecting');

    this.isExplicitDisconnect = false;

    this.connectionPromise = new Promise((resolve, reject) => {
      try {
        const socketFactory = () => {
          return new SockJS(`${API_BASE_URL}/ws`);
        };

        this.stompClient = Stomp.over(socketFactory);

        this.stompClient.heartbeat.outgoing = 4000;
        this.stompClient.heartbeat.incoming = 4000;

        this.stompClient.debug = () => {};

        const headers = {
          Authorization: `Bearer ${token}`,
        };

        this.stompClient.connect(
          headers,
          frame => {
            this.reconnectAttempt = 0;
            onConnectionStatus('connected');

            this.subscriptions.push(
              this.stompClient.subscribe(
                '/user/queue/messages',
                message => {
                  try {
                    const parsedMessage = JSON.parse(message.body);
                    onMessage(parsedMessage);
                  } catch {}
                },
                headers
              )
            );

            this.subscriptions.push(
              this.stompClient.subscribe(
                '/user/queue/typing',
                message => {
                  try {
                    const typingUpdate = JSON.parse(message.body);
                    onMessage(typingUpdate);
                  } catch {}
                },
                headers
              )
            );

            this.subscriptions.push(
              this.stompClient.subscribe(
                '/topic/status',
                message => {
                  try {
                    const statusUpdate = JSON.parse(message.body);
                    onMessage(statusUpdate);
                  } catch {}
                },
                headers
              )
            );

            this.stompClient.send(
              '/app/messages.connect',
              headers,
              JSON.stringify({
                lastDisconnectTimestamp: this.lastDisconnectTimestamp,
              })
            );

            resolve(frame);
            if (onConnect) onConnect(frame);
          },
          connectionError => {
            this.connectionPromise = null;

            if (
              !this.isExplicitDisconnect &&
              this.reconnectAttempt < this.maxReconnectAttempts
            ) {
              this.reconnectAttempt++;

              window.setTimeout(() => {
                this.connect(
                  token,
                  onConnect,
                  onMessage,
                  onConnectionError,
                  onConnectionStatus
                );
              }, this.reconnectDelay * this.reconnectAttempt);
            }

            onConnectionStatus('error');
            reject(connectionError);
            if (onConnectionError) onConnectionError(connectionError);
          }
        );

        this.stompClient.ws.onclose = () => {
          this.clearSubscriptions();
          this.connectionPromise = null;
          onConnectionStatus('disconnected');

          if (this.isExplicitDisconnect) {
            this.lastDisconnectTimestamp = new Date().toISOString();
          }

          if (
            !this.isExplicitDisconnect &&
            this.reconnectAttempt < this.maxReconnectAttempts
          ) {
            this.reconnectAttempt++;

            window.setTimeout(() => {
              this.connect(
                token,
                onConnect,
                onMessage,
                onConnectionError,
                onConnectionStatus
              );
            }, this.reconnectDelay * this.reconnectAttempt);
          }
        };
      } catch (initError) {
        this.connectionPromise = null;
        onConnectionStatus('error');
        reject(initError);
        if (onConnectionError) onConnectionError(initError);
      }
    });

    return this.connectionPromise;
  }

  static disconnect() {
    if (this.stompClient && this.stompClient.connected) {
      this.isExplicitDisconnect = true;

      this.lastDisconnectTimestamp = new Date().toISOString();

      try {
        this.stompClient.send(
          '/app/messages.disconnect',
          {},
          JSON.stringify({})
        );

        this.clearSubscriptions();

        this.stompClient.disconnect(() => {});
      } catch {
      } finally {
        this.stompClient = null;
        this.connectionPromise = null;
      }
    }
  }

  static clearSubscriptions() {
    if (this.subscriptions.length > 0) {
      this.subscriptions.forEach(subscription => {
        if (subscription && subscription.unsubscribe) {
          subscription.unsubscribe();
        }
      });
      this.subscriptions = [];
    }
  }

  static sendMessageWS(receiverId, content, conversationId = null) {
    if (!this.stompClient || !this.stompClient.connected) {
      return Promise.reject('Not connected to WebSocket server');
    }

    const message = {
      receiverId,
      content,
      conversationId,
    };

    return new Promise((resolve, reject) => {
      try {
        this.stompClient.send(
          '/app/messages.send',
          {},
          JSON.stringify(message)
        );
        resolve();
      } catch (sendError) {
        reject(sendError);
      }
    });
  }

  static sendTypingIndicator(receiverId, conversationId, isTyping = true) {
    if (!this.stompClient || !this.stompClient.connected) {
      return;
    }

    const message = {
      receiverId,
      conversationId,
      isTyping,
    };

    try {
      this.stompClient.send(
        '/app/messages.typing',
        {},
        JSON.stringify(message)
      );
    } catch {}
  }

  static markMessagesAsReadWS(conversationId) {
    if (!this.stompClient || !this.stompClient.connected) {
      return Promise.reject('Not connected to WebSocket server');
    }

    const message = {
      conversationId,
    };

    return new Promise((resolve, reject) => {
      try {
        this.stompClient.send(
          '/app/messages.markRead',
          {},
          JSON.stringify(message)
        );
        resolve();
      } catch (markReadError) {
        reject(markReadError);
      }
    });
  }

  static getUserStatusWS(username) {
    if (!this.stompClient || !this.stompClient.connected) {
      return Promise.reject('Not connected to WebSocket server');
    }

    const request = {
      username,
    };

    try {
      this.stompClient.send('/app/users.status', {}, JSON.stringify(request));
    } catch {}
  }

  static getConversationMessages(conversationId, page = 0, size = 20) {
    try {
      const axiosInstance = createAuthAxios();

      const url = buildUrlWithParams(
        `${API_BASE_URL}/chat/conversations/${conversationId}`,
        {
          page,
          size,
        }
      );

      return axiosInstance.get(url).then(response => response.data);
    } catch (apiError) {
      throw handleApiError(apiError);
    }
  }

  static getUserConversations(page = 0, size = 10) {
    try {
      const axiosInstance = createAuthAxios();
      const url = buildUrlWithParams(`${API_BASE_URL}/chat/conversations`, {
        page,
        size,
      });
      return axiosInstance.get(url).then(response => response.data);
    } catch (apiError) {
      throw handleApiError(apiError);
    }
  }

  static markConversationAsRead(conversationId) {
    try {
      const axiosInstance = createAuthAxios();
      return axiosInstance
        .put(`${API_BASE_URL}/chat/conversations/${conversationId}/read`)
        .then(response => response.data);
    } catch (apiError) {
      throw handleApiError(apiError);
    }
  }

  static sendMessage(receiverId, content, conversationId = null) {
    try {
      const axiosInstance = createAuthAxios();
      const messageData = {
        receiverId,
        content,
        conversationId,
      };

      return axiosInstance
        .post(`${API_BASE_URL}/chat/messages`, messageData)
        .then(response => response.data);
    } catch (apiError) {
      throw handleApiError(apiError);
    }
  }

  static getUnreadCount() {
    try {
      const axiosInstance = createAuthAxios();
      return axiosInstance
        .get(`${API_BASE_URL}/chat/messages/unread/count`)
        .then(response => response.data.count);
    } catch (apiError) {
      throw handleApiError(apiError);
    }
  }

  static getOnlineUsers() {
    try {
      const axiosInstance = createAuthAxios();
      return axiosInstance
        .get(`${API_BASE_URL}/chat/users/online`)
        .then(response => response.data);
    } catch (apiError) {
      throw handleApiError(apiError);
    }
  }

  static getFirstUnreadMessageId(conversationId) {
    try {
      const axiosInstance = createAuthAxios();
      return axiosInstance
        .get(
          `${API_BASE_URL}/chat/conversations/${conversationId}/first-unread`
        )
        .then(response => response.data);
    } catch (apiError) {
      throw handleApiError(apiError);
    }
  }

  static async sendChatMessage(receiverId, content, conversationId = null) {
    if (this.stompClient && this.stompClient.connected) {
      try {
        return await this.sendMessageWS(receiverId, content, conversationId);
      } catch {
        return await this.sendMessage(receiverId, content, conversationId);
      }
    } else {
      return await this.sendMessage(receiverId, content, conversationId);
    }
  }

  static async markAsRead(conversationId) {
    if (this.stompClient && this.stompClient.connected) {
      try {
        return await this.markMessagesAsReadWS(conversationId);
      } catch {
        return await this.markConversationAsRead(conversationId);
      }
    } else {
      return await this.markConversationAsRead(conversationId);
    }
  }
}
