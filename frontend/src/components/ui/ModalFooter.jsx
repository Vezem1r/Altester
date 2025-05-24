import { useTranslation } from 'react-i18next';

const BUTTON_TRANSLATION_KEYS = {
  confirm: 'modalFooter.confirm',
  cancel: 'modalFooter.cancel',
  save: 'modalFooter.save',
  delete: 'modalFooter.delete',
  edit: 'modalFooter.edit',
  add: 'modalFooter.add',
  update: 'modalFooter.update',
  create: 'modalFooter.create',
  close: 'modalFooter.close',
  submit: 'modalFooter.submit',
  ok: 'modalFooter.ok',
  yes: 'modalFooter.yes',
  no: 'modalFooter.no',
};

const ModalFooter = ({
  primaryButtonText = 'Confirm',
  secondaryButtonText = 'Cancel',
  onPrimaryClick,
  onSecondaryClick,
  primaryButtonClass = 'bg-gradient-to-r from-purple-600 to-purple-700 hover:from-purple-700 hover:to-purple-800 text-white',
  secondaryButtonClass = 'bg-white hover:bg-gray-50 text-gray-700 border-gray-300',
  loading = false,
  reverseOrder = false,
}) => {
  const { t } = useTranslation();

  const getButtonTranslation = buttonText => {
    if (!buttonText) return '';

    const key = buttonText.toLowerCase();
    const translationKey = BUTTON_TRANSLATION_KEYS[key];

    return translationKey ? t(translationKey, buttonText) : buttonText;
  };

  const primaryButton = (
    <button
      type="button"
      onClick={onPrimaryClick}
      disabled={loading}
      className={`
        relative w-full inline-flex justify-center items-center
        rounded-xl border shadow-sm px-6 py-3
        text-base font-medium
        transform transition-all duration-200
        focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500
        sm:w-auto sm:text-sm
        ${primaryButtonClass}
        ${loading ? 'opacity-50 cursor-not-allowed' : 'hover:scale-105 hover:shadow-lg'}
      `}
    >
      {loading && (
        <svg
          className="animate-spin -ml-1 mr-3 h-5 w-5 text-white"
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
      )}
      {getButtonTranslation(primaryButtonText)}
    </button>
  );

  const secondaryButton = secondaryButtonText && (
    <button
      type="button"
      onClick={onSecondaryClick}
      className={`
        mt-3 w-full inline-flex justify-center items-center
        rounded-xl border shadow-sm px-6 py-3
        text-base font-medium
        transform transition-all duration-200
        hover:scale-105 hover:shadow-md
        focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500
        sm:mt-0 sm:w-auto sm:text-sm
        ${secondaryButtonClass}
      `}
    >
      {getButtonTranslation(secondaryButtonText)}
    </button>
  );

  return (
    <div
      className={`sm:flex gap-3 ${reverseOrder ? 'sm:flex-row-reverse' : 'sm:flex-row'}`}
    >
      {reverseOrder ? (
        <>
          {primaryButton}
          {secondaryButton}
        </>
      ) : (
        <>
          {secondaryButton}
          {primaryButton}
        </>
      )}
    </div>
  );
};

export default ModalFooter;
