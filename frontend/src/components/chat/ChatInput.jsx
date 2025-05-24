import React, { useState, useRef, useEffect, useCallback } from 'react';
import { useTranslation } from 'react-i18next';

const ChatInput = ({ onSendMessage, onTyping }) => {
  const { t } = useTranslation();

  const [message, setMessage] = useState('');
  const [inputHeight, setInputHeight] = useState('50px');
  const inputRef = useRef(null);
  const typingTimeoutRef = useRef(null);

  useEffect(() => {
    if (inputRef.current) {
      inputRef.current.focus();
    }
  }, []);

  useEffect(() => {
    if (!inputRef.current) return;

    inputRef.current.style.height = 'auto';

    const newHeight = Math.min(
      Math.max(inputRef.current.scrollHeight, 50),
      120
    );

    setInputHeight(`${newHeight}px`);
  }, [message]);

  const handleChange = useCallback(
    e => {
      setMessage(e.target.value);

      if (typingTimeoutRef.current) {
        window.clearTimeout(typingTimeoutRef.current);
      }

      onTyping();

      typingTimeoutRef.current = window.setTimeout(() => {
        onTyping(false);
        typingTimeoutRef.current = null;
      }, 2000);
    },
    [onTyping]
  );

  const handleSend = useCallback(() => {
    if (!message.trim()) return;

    onSendMessage(message.trim());
    setMessage('');

    setInputHeight('50px');

    if (inputRef.current) {
      inputRef.current.focus();
    }
  }, [message, onSendMessage]);

  const handleKeyPress = useCallback(
    e => {
      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        handleSend();
      }
    },
    [handleSend]
  );

  return (
    <div className="flex items-end">
      <div className="flex-1 mr-2 relative">
        <textarea
          ref={inputRef}
          value={message}
          onChange={handleChange}
          onKeyPress={handleKeyPress}
          placeholder={t('chatInput.placeholder', 'Type a message...')}
          className="w-full p-3 border rounded-2xl focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent resize-none shadow-sm"
          rows={1}
          style={{
            height: inputHeight,
            minHeight: '50px',
            maxHeight: '120px',
            paddingRight: '44px',
            transition: 'height 0.1s ease',
          }}
        />
        <button
          onClick={handleSend}
          disabled={!message.trim()}
          aria-label={t('chatInput.sendMessage', 'Send message')}
          className={`absolute right-3 bottom-2.5 p-2 rounded-full ${
            message.trim()
              ? 'bg-purple-600 text-white hover:bg-purple-700'
              : 'bg-gray-200 text-gray-400 cursor-not-allowed'
          } focus:outline-none focus:ring-2 focus:ring-purple-500 transition-colors duration-200`}
          style={{
            width: '36px',
            height: '36px',
          }}
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            className="h-5 w-5 mx-auto"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"
            />
          </svg>
        </button>
      </div>
    </div>
  );
};

export default React.memo(ChatInput);
