import { useEffect, useState } from 'react';
import { XIcon } from '@heroicons/react/outline';
import { useTranslation } from 'react-i18next';

const MODAL_TRANSLATION_KEYS = {
  addQuestion: 'modal.addQuestion',
  editQuestion: 'modal.editQuestion',
  deleteQuestion: 'modal.deleteQuestion',
  addTest: 'modal.addTest',
  editTest: 'modal.editTest',
  deleteTest: 'modal.deleteTest',
  addGroup: 'modal.addGroup',
  editGroup: 'modal.editGroup',
  deleteGroup: 'modal.deleteGroup',
  addStudent: 'modal.addStudent',
  editStudent: 'modal.editStudent',
  deleteStudent: 'modal.deleteStudent',
  confirmAction: 'modal.confirmAction',
  warning: 'modal.warning',
  error: 'modal.error',
  success: 'modal.success',
  info: 'modal.info',
};

const Modal = ({
  isOpen,
  onClose,
  title,
  description,
  children,
  footer,
  size = 'md',
  closeOnOverlayClick = true,
  showCloseButton = true,
  className = '',
}) => {
  const { t } = useTranslation();
  const [isVisible, setIsVisible] = useState(false);
  const [isAnimating, setIsAnimating] = useState(false);

  useEffect(() => {
    if (isOpen) {
      setIsAnimating(true);
      setTimeout(() => setIsVisible(true), 10);
    } else {
      setIsVisible(false);
      setTimeout(() => setIsAnimating(false), 300);
    }
  }, [isOpen]);

  if (!isOpen && !isAnimating) return null;

  const sizeClasses = {
    sm: 'sm:max-w-lg',
    md: 'sm:max-w-2xl',
    lg: 'sm:max-w-4xl',
    xl: 'sm:max-w-6xl',
    full: 'sm:max-w-full',
  };

  const getTranslatedTitle = titleText => {
    if (!titleText) return '';

    const translationKey = MODAL_TRANSLATION_KEYS[titleText];
    return translationKey ? t(translationKey, titleText) : titleText;
  };

  const getTranslatedDescription = descriptionText => {
    if (!descriptionText) return '';

    const translationKey = MODAL_TRANSLATION_KEYS[descriptionText];
    return translationKey
      ? t(translationKey, descriptionText)
      : descriptionText;
  };

  const translatedTitle = getTranslatedTitle(title);
  const translatedDescription = getTranslatedDescription(description);

  return (
    <div
      className={`fixed z-30 inset-0 overflow-y-auto ${isAnimating ? 'block' : 'hidden'}`}
    >
      <div className="flex items-center justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
        <div
          className={`fixed inset-0 transition-all duration-300 ease-out ${
            isVisible ? 'opacity-100' : 'opacity-0'
          }`}
          aria-hidden="true"
          onClick={closeOnOverlayClick ? onClose : undefined}
        >
          <div className="absolute inset-0 bg-gradient-to-br from-gray-900/90 via-gray-800/90 to-gray-900/90 backdrop-blur-sm" />
        </div>

        <span
          className="hidden sm:inline-block sm:align-middle sm:h-screen"
          aria-hidden="true"
        >
          &#8203;
        </span>

        <div
          className={`inline-block align-bottom bg-white rounded-2xl text-left overflow-hidden shadow-2xl transform transition-all duration-300 ease-out sm:my-8 sm:align-middle ${sizeClasses[size]} sm:w-full ${className} ${
            isVisible
              ? 'opacity-100 translate-y-0 scale-100'
              : 'opacity-0 translate-y-4 scale-95'
          }`}
        >
          {(title || description) && (
            <div className="bg-gradient-to-r from-purple-600 to-purple-400 px-6 py-6 sm:px-8">
              {showCloseButton && (
                <button
                  onClick={onClose}
                  className="absolute top-4 right-4 p-2 bg-white/10 backdrop-blur-sm rounded-xl text-white/90 hover:text-white hover:bg-white/20 transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-white/50"
                  aria-label={t('modal.close', 'Close')}
                >
                  <XIcon className="h-5 w-5" />
                </button>
              )}
              {title && (
                <h3 className="text-xl font-semibold text-white">
                  {translatedTitle}
                </h3>
              )}
              {description && (
                <p className="mt-1 text-sm text-purple-100">
                  {translatedDescription}
                </p>
              )}
            </div>
          )}

          <div className="bg-gradient-to-br from-white to-gray-50">
            <div className="px-6 py-6 sm:px-8">
              {!title && !description && showCloseButton && (
                <button
                  onClick={onClose}
                  className="absolute top-4 right-4 p-2 bg-white/80 backdrop-blur-sm rounded-xl text-gray-400 hover:text-gray-600 hover:bg-gray-100 transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500"
                  aria-label={t('modal.close', 'Close')}
                >
                  <XIcon className="h-5 w-5" />
                </button>
              )}

              <div className="relative">{children}</div>
            </div>
          </div>

          {footer && (
            <div className="bg-gradient-to-br from-gray-50 to-gray-100 px-6 py-4 sm:px-8 border-t border-gray-200">
              {footer}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Modal;
