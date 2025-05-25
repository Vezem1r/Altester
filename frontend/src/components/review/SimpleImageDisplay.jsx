import React from 'react';
import AuthenticatedImage from '@/components/questions/AuthenticatedImage';

const SimpleImageDisplay = ({ imagePath, alt, className, ...props }) => {
  return (
    <AuthenticatedImage
      imagePath={imagePath}
      alt={alt || 'Question image'}
      className={className}
      {...props}
    />
  );
};

export default SimpleImageDisplay;
