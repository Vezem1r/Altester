import React from 'react';
import { motion } from 'framer-motion';

const FormField = ({
  label,
  name,
  type = 'text',
  value,
  onChange,
  error,
  required = false,
  placeholder,
  description,
  className = '',
  options = [],
  rows = 3,
  disabled = false,
}) => {
  const baseClasses = `
    w-full px-4 py-2 border rounded-lg shadow-sm 
    focus:ring-2 focus:ring-purple-500 focus:border-purple-500 
    transition-all duration-200
    ${error ? 'border-red-300 bg-red-50' : 'border-gray-300 bg-white'}
    ${disabled ? 'opacity-50 cursor-not-allowed' : ''}
    ${className}
  `;

  const renderField = () => {
    switch (type) {
      case 'textarea':
        return (
          <textarea
            id={name}
            name={name}
            value={value}
            onChange={onChange}
            rows={rows}
            placeholder={placeholder}
            required={required}
            disabled={disabled}
            className={baseClasses}
          />
        );

      case 'select':
        return (
          <select
            id={name}
            name={name}
            value={value}
            onChange={onChange}
            required={required}
            disabled={disabled}
            className={baseClasses}
          >
            {options.map(option => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        );

      case 'checkbox':
        return (
          <input
            id={name}
            name={name}
            type="checkbox"
            checked={value}
            onChange={onChange}
            disabled={disabled}
            className="h-4 w-4 text-purple-600 focus:ring-purple-500 border-gray-300 rounded"
          />
        );

      default:
        return (
          <input
            id={name}
            name={name}
            type={type}
            value={value}
            onChange={onChange}
            placeholder={placeholder}
            required={required}
            disabled={disabled}
            className={baseClasses}
          />
        );
    }
  };

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      className="space-y-1"
    >
      {label && (
        <label
          htmlFor={name}
          className={`block text-sm font-medium text-gray-700 ${type === 'checkbox' ? 'inline-flex items-center' : ''}`}
        >
          {type === 'checkbox' && renderField()}
          <span className={type === 'checkbox' ? 'ml-2' : ''}>
            {label}
            {required && <span className="text-red-500 ml-1">*</span>}
          </span>
        </label>
      )}

      {type !== 'checkbox' && renderField()}

      {description && (
        <p className="text-xs text-gray-500 mt-1">{description}</p>
      )}

      {error && (
        <motion.p
          initial={{ opacity: 0, y: -5 }}
          animate={{ opacity: 1, y: 0 }}
          className="text-sm text-red-600 mt-1"
        >
          {error}
        </motion.p>
      )}
    </motion.div>
  );
};

export default FormField;
