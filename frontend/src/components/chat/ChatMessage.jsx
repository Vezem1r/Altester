import React from 'react';
import { useChat } from '@/context/ChatContext';
import { useTranslation } from 'react-i18next';

const ChatMessage = ({ message, isOwnMessage }) => {
  const { t } = useTranslation();
  const { formatMessageTime } = useChat();

  const timestamp = formatMessageTime(message.timestamp);

  return (
    <div
      className={`flex mb-4 ${isOwnMessage ? 'justify-end' : 'justify-start'}`}
    >
      {!isOwnMessage && (
        <div className="w-8 h-8 bg-gradient-to-br from-purple-500 to-purple-700 rounded-full flex items-center justify-center text-white font-medium mr-2 flex-shrink-0 shadow-sm">
          {message.senderId.charAt(0).toUpperCase()}
        </div>
      )}

      <div
        className={`max-w-xs md:max-w-md ${
          isOwnMessage
            ? 'bg-gradient-to-r from-purple-500 to-purple-600 text-white rounded-tl-2xl rounded-tr-2xl rounded-bl-2xl'
            : 'bg-gray-100 text-gray-800 rounded-tl-2xl rounded-tr-2xl rounded-br-2xl'
        } p-3 shadow-sm relative`}
      >
        <div className="text-sm whitespace-pre-wrap break-words">
          {message.content}
        </div>

        <div
          className={`text-xs mt-1 ${isOwnMessage ? 'text-purple-200' : 'text-gray-500'} text-right flex items-center justify-end`}
        >
          {timestamp}

          {isOwnMessage && (
            <span
              className="ml-1"
              title={
                message.read
                  ? t('chatMessage.read', 'Read')
                  : t('chatMessage.delivered', 'Delivered')
              }
            >
              {message.read ? (
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  className="h-3.5 w-3.5 text-purple-300"
                  viewBox="0 0 20 20"
                  fill="currentColor"
                >
                  <path
                    fillRule="evenodd"
                    d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                    clipRule="evenodd"
                  />
                </svg>
              ) : (
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  className="h-3 w-3 text-purple-300"
                  viewBox="0 0 20 20"
                  fill="currentColor"
                >
                  <path
                    fillRule="evenodd"
                    d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                    clipRule="evenodd"
                  />
                </svg>
              )}
            </span>
          )}
        </div>
      </div>

      {isOwnMessage && (
        <div className="w-8 h-8 bg-gradient-to-br from-purple-500 to-purple-700 rounded-full flex items-center justify-center text-white font-medium ml-2 flex-shrink-0 shadow-sm">
          {message.senderId.charAt(0).toUpperCase()}
        </div>
      )}
    </div>
  );
};

export default React.memo(ChatMessage);
