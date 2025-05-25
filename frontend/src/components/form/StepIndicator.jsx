import { motion } from 'framer-motion';

const StepIndicator = ({
  currentStep,
  totalSteps,
  onStepClick,
  stepLabels = [],
  disableBackToStep,
}) => {
  return (
    <div className="relative">
      <div className="flex items-center justify-between mb-8">
        {[...Array(totalSteps)].map((_, index) => {
          const stepNumber = index + 1;
          const isCompleted = currentStep > stepNumber;
          const isCurrent = currentStep === stepNumber;
          const isDisabled =
            disableBackToStep &&
            currentStep === disableBackToStep.from &&
            stepNumber === disableBackToStep.to;
          const canNavigate = (isCompleted || isCurrent) && !isDisabled;

          return (
            <div
              key={stepNumber}
              className="relative flex flex-col items-center flex-1"
            >
              <motion.button
                type="button"
                whileHover={canNavigate ? { scale: 1.05 } : {}}
                whileTap={canNavigate ? { scale: 0.95 } : {}}
                onClick={() => canNavigate && onStepClick(stepNumber)}
                disabled={!canNavigate}
                className={`w-12 h-12 rounded-full flex items-center justify-center font-medium text-sm transition-all duration-200 relative z-10 ${
                  isCompleted && !isDisabled
                    ? 'bg-purple-600 text-white'
                    : isCurrent
                      ? 'bg-white text-purple-600 border-2 border-purple-600 shadow-lg'
                      : 'bg-gray-200 text-gray-500 cursor-not-allowed'
                } ${isDisabled ? 'opacity-50' : ''}`}
              >
                {isCompleted && !isDisabled ? (
                  <svg
                    className="w-6 h-6"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M5 13l4 4L19 7"
                    />
                  </svg>
                ) : (
                  stepNumber
                )}
              </motion.button>
              {stepLabels[index] && (
                <span
                  className={`mt-2 text-xs font-medium ${
                    isCurrent ? 'text-purple-600' : 'text-gray-500'
                  }`}
                >
                  {stepLabels[index]}
                </span>
              )}

              {index < totalSteps - 1 && (
                <>
                  <div className="absolute top-6 left-[50%] w-full h-0.5 bg-gray-300" />
                  <motion.div
                    className="absolute top-6 left-[50%] h-0.5 bg-purple-600"
                    initial={{ width: '0%' }}
                    animate={{
                      width: isCompleted ? '100%' : '0%',
                    }}
                    transition={{ duration: 0.5, ease: 'easeInOut' }}
                  />
                </>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default StepIndicator;
