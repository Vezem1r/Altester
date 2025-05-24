export const ImageOptionsIcon = ({
  className = 'h-6 w-6 text-purple-600 mb-2',
}) => (
  <svg
    xmlns="http://www.w3.org/2000/svg"
    className={className}
    viewBox="0 0 24 24"
    fill="none"
  >
    <rect
      x="3"
      y="2"
      width="18"
      height="12"
      rx="2"
      stroke="currentColor"
      strokeWidth="1.5"
      fill="none"
    />

    <path
      d="M3 12L7 8L10 10L14 6L21 9"
      stroke="currentColor"
      strokeWidth="1.5"
      strokeLinecap="round"
      strokeLinejoin="round"
    />
    <circle cx="6" cy="5" r="1" fill="currentColor" />

    <rect
      x="4"
      y="16"
      width="2.5"
      height="2.5"
      rx="0.5"
      stroke="currentColor"
      strokeWidth="1"
      fill="none"
    />
    <path
      d="M5 17L5.5 17.5L6 16.5"
      stroke="currentColor"
      strokeWidth="1"
      strokeLinecap="round"
      strokeLinejoin="round"
    />

    <line
      x1="8"
      y1="17.25"
      x2="18"
      y2="17.25"
      stroke="currentColor"
      strokeWidth="1.5"
      strokeLinecap="round"
    />
  </svg>
);

export const TextOnlyIcon = ({
  className = 'h-6 w-6 text-purple-600 mb-2',
}) => (
  <svg
    xmlns="http://www.w3.org/2000/svg"
    className={className}
    fill="none"
    viewBox="0 0 24 24"
    stroke="currentColor"
  >
    <path
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={2}
      d="M4 6h16M4 12h16m-7 6h7"
    />
  </svg>
);

export const ImageOnlyIcon = ({
  className = 'h-6 w-6 text-purple-600 mb-2',
}) => (
  <svg
    xmlns="http://www.w3.org/2000/svg"
    className={className}
    fill="none"
    viewBox="0 0 24 24"
    stroke="currentColor"
  >
    <path
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={2}
      d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
    />
  </svg>
);

export const TextWithImageIcon = ({
  className = 'h-6 w-6 text-purple-600 mb-2',
}) => (
  <svg
    xmlns="http://www.w3.org/2000/svg"
    className={className}
    fill="none"
    viewBox="0 0 24 24"
    stroke="currentColor"
  >
    <path
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={2}
      d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
    />
  </svg>
);

export const MultipleChoiceIcon = ({
  className = 'h-6 w-6 text-purple-600 mb-2',
}) => (
  <svg
    xmlns="http://www.w3.org/2000/svg"
    className={className}
    fill="none"
    viewBox="0 0 24 24"
    stroke="currentColor"
  >
    <path
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={2}
      d="M9 12l2 2 4-4M7.835 4.697a3.42 3.42 0 001.946-.806 3.42 3.42 0 014.438 0 3.42 3.42 0 001.946.806 3.42 3.42 0 013.138 3.138 3.42 3.42 0 00.806 1.946 3.42 3.42 0 010 4.438 3.42 3.42 0 00-.806 1.946 3.42 3.42 0 01-3.138 3.138 3.42 3.42 0 00-1.946.806 3.42 3.42 0 01-4.438 0 3.42 3.42 0 00-1.946-.806 3.42 3.42 0 01-3.138-3.138 3.42 3.42 0 00-.806-1.946 3.42 3.42 0 010-4.438 3.42 3.42 0 00.806-1.946 3.42 3.42 0 013.138-3.138z"
    />
  </svg>
);

export const getIconForQuestionType = (type, className) => {
  switch (type) {
    case 'TEXT_ONLY':
      return <TextOnlyIcon className={className} />;
    case 'IMAGE_ONLY':
      return <ImageOnlyIcon className={className} />;
    case 'TEXT_WITH_IMAGE':
      return <TextWithImageIcon className={className} />;
    case 'MULTIPLE_CHOICE':
      return <MultipleChoiceIcon className={className} />;
    case 'IMAGE_WITH_MULTIPLE_CHOICE':
      return <ImageOptionsIcon className={className} />;
    default:
      return null;
  }
};
