import { motion } from 'framer-motion';

const FormSection = ({
  title,
  description,
  children,
  variant = 'default',
  className = '',
}) => {
  const variants = {
    default: 'bg-white',
    highlighted: 'bg-purple-50 border-purple-200',
    warning: 'bg-yellow-50 border-yellow-200',
    info: 'bg-blue-50 border-blue-200',
  };

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.3 }}
      className={`p-6 rounded-xl shadow-sm border ${variants[variant]} ${className}`}
    >
      {(title || description) && (
        <div className="mb-6">
          {title && (
            <h3 className="text-lg font-semibold text-gray-900 mb-1">
              {title}
            </h3>
          )}
          {description && (
            <p className="text-sm text-gray-600">{description}</p>
          )}
        </div>
      )}
      <div>{children}</div>
    </motion.div>
  );
};

export default FormSection;
