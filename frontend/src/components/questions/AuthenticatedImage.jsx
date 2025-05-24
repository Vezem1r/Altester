import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { QuestionService } from '@/services/QuestionService';

const AuthenticatedImage = ({ imagePath, alt, className, ...props }) => {
  const { t } = useTranslation();
  const [imageUrl, setImageUrl] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    const loadImage = async () => {
      if (!imagePath) {
        setLoading(false);
        setError(true);
        return;
      }

      try {
        setLoading(true);
        const url = await QuestionService.fetchQuestionImage(imagePath);
        setImageUrl(url);
        setError(false);
      } catch (err) {
        setError(true);
      } finally {
        setLoading(false);
      }
    };

    loadImage();

    return () => {
      if (imageUrl) {
        URL.revokeObjectURL(imageUrl);
      }
    };
  }, [imagePath]);

  if (loading) {
    return (
      <div
        className={`flex items-center justify-center bg-gray-100 rounded-md ${className || 'h-48 w-full'}`}
      >
        <svg
          className="animate-spin h-8 w-8 text-gray-400"
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
        >
          <circle
            className="opacity-25"
            cx="12"
            cy="12"
            r="10"
            stroke="currentColor"
            strokeWidth="4"
          />
          <path
            className="opacity-75"
            fill="currentColor"
            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
          />
        </svg>
      </div>
    );
  }

  if (error) {
    return (
      <div
        className={`flex flex-col items-center justify-center bg-gray-100 rounded-md ${className || 'h-48 w-full'}`}
      >
        <svg
          className="h-12 w-12 text-gray-400"
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
          />
        </svg>
        <p className="mt-2 text-sm text-gray-500">
          {alt || t('authenticatedImage.failedToLoad', 'Failed to load image')}
        </p>
      </div>
    );
  }

  return (
    <img
      src={imageUrl}
      alt={alt || t('authenticatedImage.questionImage', 'Question image')}
      className={className}
      {...props}
    />
  );
};

export default AuthenticatedImage;
