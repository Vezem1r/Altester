import { useState, useEffect } from 'react';

const TestTimer = ({ seconds, onExpire }) => {
  const [timeLeft, setTimeLeft] = useState(seconds || 0);
  const [timerActive, setTimerActive] = useState(false);
  const [hasExpired, setHasExpired] = useState(false);

  useEffect(() => {
    if (seconds && seconds > 0) {
      setTimeLeft(seconds);
      setTimerActive(true);
      setHasExpired(false);
    }
  }, [seconds]);

  useEffect(() => {
    if (!timerActive || timeLeft <= 0) {
      if (timerActive && timeLeft <= 0 && !hasExpired) {
        setHasExpired(true);
        onExpire && onExpire();
        setTimerActive(false);
      }
      return;
    }

    const timer = setInterval(() => {
      setTimeLeft(prevTime => {
        if (prevTime <= 1) {
          clearInterval(timer);
          return 0;
        }
        return prevTime - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [timeLeft, timerActive, onExpire, hasExpired]);

  const formatTime = totalSeconds => {
    if (totalSeconds === undefined || totalSeconds === null) {
      return '00:00:00';
    }

    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const seconds = totalSeconds % 60;

    return [
      hours.toString().padStart(2, '0'),
      minutes.toString().padStart(2, '0'),
      seconds.toString().padStart(2, '0'),
    ].join(':');
  };

  const getTimerColor = () => {
    if (!timerActive) {
      return 'text-gray-400';
    } else if (timeLeft < 300) {
      return 'text-red-600';
    } else if (timeLeft < 600) {
      return 'text-orange-500';
    }
    return 'text-gray-900';
  };

  return (
    <div className="flex items-center">
      <div className="mr-2">
        <svg
          className="h-5 w-5 text-gray-400"
          xmlns="http://www.w3.org/2000/svg"
          viewBox="0 0 20 20"
          fill="currentColor"
        >
          <path
            fillRule="evenodd"
            d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z"
            clipRule="evenodd"
          />
        </svg>
      </div>
      <div className={`font-mono text-lg font-bold ${getTimerColor()}`}>
        {formatTime(timeLeft)}
      </div>
    </div>
  );
};

export default TestTimer;
