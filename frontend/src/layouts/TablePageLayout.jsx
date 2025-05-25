import React from 'react';
import AppLayout from './AppLayout';

const TablePageLayout = ({
  children,
  icon,
  title,
  description,
  primaryAction,
  variant = 'purple',
}) => {
  const gradientClasses = {
    purple: 'bg-gradient-to-r from-purple-600 to-purple-700',
    'purple-light': 'bg-gradient-to-r from-purple-500 to-purple-600',
    'purple-dark': 'bg-gradient-to-r from-purple-700 to-purple-800',
    'purple-indigo': 'bg-gradient-to-r from-purple-600 to-indigo-600',
    'indigo-purple': 'bg-gradient-to-r from-indigo-600 to-purple-600',
    'purple-violet': 'bg-gradient-to-r from-purple-600 to-violet-600',
    'violet-purple': 'bg-gradient-to-r from-violet-600 to-purple-600',
  };

  const textClasses = {
    purple: 'text-purple-100',
    'purple-light': 'text-purple-100',
    'purple-dark': 'text-purple-100',
    'purple-indigo': 'text-purple-100',
    'indigo-purple': 'text-indigo-100',
    'purple-violet': 'text-purple-100',
    'violet-purple': 'text-violet-100',
  };

  const buttonVariants = {
    purple:
      'text-purple-600 bg-white hover:bg-purple-50 focus:ring-white focus:ring-offset-purple-600',
    'purple-light':
      'text-purple-600 bg-white hover:bg-purple-50 focus:ring-white focus:ring-offset-purple-600',
    'purple-dark':
      'text-purple-700 bg-white hover:bg-purple-50 focus:ring-white focus:ring-offset-purple-700',
    'purple-indigo':
      'text-purple-600 bg-white hover:bg-purple-50 focus:ring-white focus:ring-offset-purple-600',
    'indigo-purple':
      'text-indigo-600 bg-white hover:bg-indigo-50 focus:ring-white focus:ring-offset-indigo-600',
    'purple-violet':
      'text-purple-600 bg-white hover:bg-purple-50 focus:ring-white focus:ring-offset-purple-600',
    'violet-purple':
      'text-violet-600 bg-white hover:bg-violet-50 focus:ring-white focus:ring-offset-violet-600',
  };

  return (
    <AppLayout>
      <div className="bg-white shadow-sm rounded-lg overflow-hidden">
        <div
          className={`${gradientClasses[variant]} px-4 py-5 sm:px-6 sm:py-6`}
        >
          <div
            className={`flex flex-col sm:flex-row sm:items-center ${primaryAction ? 'sm:justify-between' : ''}`}
          >
            <div className="flex items-center mb-3 sm:mb-0">
              {icon && (
                <div className="h-8 w-8 text-white flex-shrink-0">{icon}</div>
              )}
              <h1 className="ml-3 text-xl sm:text-2xl font-bold text-white">
                {title}
              </h1>
            </div>
            {primaryAction && (
              <div className="flex justify-end">
                {React.cloneElement(primaryAction, {
                  className: `inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 ${buttonVariants[variant]}`,
                })}
              </div>
            )}
          </div>
          {description && (
            <p className={`mt-2 ${textClasses[variant]} text-sm`}>
              {description}
            </p>
          )}
        </div>

        <div className="px-4 py-4 sm:px-6 sm:py-6">{children}</div>
      </div>
    </AppLayout>
  );
};

export default TablePageLayout;
