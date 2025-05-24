import { useTranslation } from 'react-i18next';

const FormField = ({
  label,
  name,
  type = 'text',
  value,
  onChange,
  error,
  required = false,
  placeholder,
  maxLength,
  rows,
  disabled = false,
  helperText,
  className = '',
  children,
}) => {
  const { t } = useTranslation();

  const baseInputClass = `
    w-full px-4 py-3 
    bg-white border rounded-xl
    text-gray-900 text-sm
    placeholder-gray-400
    transition-all duration-200
    focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent
    ${
      error
        ? 'border-red-300 focus:ring-red-500'
        : 'border-gray-200 hover:border-gray-300'
    }
    ${disabled ? 'bg-gray-50 cursor-not-allowed opacity-60' : ''}
    ${className}
  `;

  const renderInput = () => {
    if (type === 'textarea') {
      return (
        <textarea
          name={name}
          id={name}
          value={value}
          onChange={onChange}
          rows={rows || 3}
          maxLength={maxLength}
          disabled={disabled}
          placeholder={
            placeholder ? t(`formField.${placeholder}`, placeholder) : ''
          }
          className={`${baseInputClass} resize-none`}
          required={required}
        />
      );
    }

    if (type === 'select') {
      return (
        <select
          name={name}
          id={name}
          value={value}
          onChange={onChange}
          disabled={disabled}
          className={baseInputClass}
          required={required}
        >
          {placeholder && (
            <option value="">
              {t(`formField.${placeholder}`, placeholder)}
            </option>
          )}
          {children}
        </select>
      );
    }

    if (type === 'checkbox') {
      return (
        <div className="flex items-center">
          <input
            type="checkbox"
            name={name}
            id={name}
            checked={value}
            onChange={onChange}
            disabled={disabled}
            className="
              h-5 w-5 
              text-purple-600 
              border-gray-300 
              rounded-md
              focus:ring-2 focus:ring-purple-500
              transition-all duration-200
            "
            required={required}
          />
          {label && (
            <label
              htmlFor={name}
              className="ml-3 text-sm font-medium text-gray-700"
            >
              {t(`formField.${label}`, label)}
              {required && <span className="text-red-500 ml-1">*</span>}
            </label>
          )}
        </div>
      );
    }

    return (
      <input
        type={type}
        name={name}
        id={name}
        value={value}
        onChange={onChange}
        maxLength={maxLength}
        disabled={disabled}
        placeholder={
          placeholder ? t(`formField.${placeholder}`, placeholder) : ''
        }
        className={baseInputClass}
        required={required}
      />
    );
  };

  if (type === 'checkbox') {
    return (
      <div>
        {renderInput()}
        {error && (
          <p className="mt-2 text-sm text-red-600 flex items-center">
            <svg
              className="w-4 h-4 mr-1"
              fill="currentColor"
              viewBox="0 0 20 20"
            >
              <path
                fillRule="evenodd"
                d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
                clipRule="evenodd"
              />
            </svg>
            {t(`formField.errors.${error}`, error)}
          </p>
        )}
      </div>
    );
  }

  return (
    <div>
      {label && (
        <label
          htmlFor={name}
          className="block text-sm font-semibold text-gray-800 mb-2"
        >
          {t(`formField.${label}`, label)}
          {required && <span className="text-red-500 ml-1">*</span>}
          {helperText && (
            <span className="ml-2 text-xs text-gray-500 font-normal">
              {t(`formField.helperText.${helperText}`, helperText)}
            </span>
          )}
        </label>
      )}
      <div className="relative">
        {renderInput()}
        {type !== 'checkbox' && type !== 'textarea' && type !== 'select' && (
          <div className="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
            {error && (
              <svg
                className="w-5 h-5 text-red-500"
                fill="currentColor"
                viewBox="0 0 20 20"
              >
                <path
                  fillRule="evenodd"
                  d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
                  clipRule="evenodd"
                />
              </svg>
            )}
          </div>
        )}
      </div>
      {error && type !== 'checkbox' && (
        <p className="mt-2 text-sm text-red-600 flex items-center">
          <svg className="w-4 h-4 mr-1" fill="currentColor" viewBox="0 0 20 20">
            <path
              fillRule="evenodd"
              d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
              clipRule="evenodd"
            />
          </svg>
          {t(`formField.errors.${error}`, error)}
        </p>
      )}
    </div>
  );
};

export default FormField;
