import { useTranslation } from 'react-i18next';

import openaiIcon from '@/assets/icons/openai.svg';
import geminiIcon from '@/assets/icons/gemini.svg';
import deepseekIcon from '@/assets/icons/deepseek.svg';
import claudeIcon from '@/assets/icons/claude.svg';

const AIModelComponent = () => {
  const { t } = useTranslation();

  const providers = [
    {
      name: 'OpenAI',
      iconSrc: openaiIcon,
      iconClass: 'text-green-500',
      models: import.meta.env.VITE_AI_MODELS_OPENAI?.split(',').filter(
        Boolean
      ) || ['gpt-4o'],
    },
    {
      name: 'Anthropic',
      iconSrc: claudeIcon,
      iconClass: 'text-purple-600',
      models:
        import.meta.env.VITE_AI_MODELS_ANTHROPIC?.split(',').filter(Boolean) ||
        [],
    },
    {
      name: 'DeepSeek',
      iconSrc: deepseekIcon,
      iconClass: 'text-blue-600',
      models:
        import.meta.env.VITE_AI_MODELS_DEEPSEEK?.split(',').filter(Boolean) ||
        [],
    },
    {
      name: 'Google Gemini',
      iconSrc: geminiIcon,
      iconClass: 'text-teal-600',
      models: import.meta.env.VITE_AI_MODELS_GEMINI?.split(',').filter(
        Boolean
      ) || ['gemini-2.0-flash'],
    },
  ];

  return (
    <div className="bg-white shadow-md rounded-lg overflow-hidden">
      <div className="px-4 py-5 border-b border-gray-200">
        <h3 className="text-lg font-medium text-gray-900">
          {t('aiModelComponent.aiModels', 'AI Models')}
        </h3>
        <p className="text-sm text-gray-500">
          {t(
            'aiModelComponent.availableModels',
            'Available AI models for test verification'
          )}
        </p>
      </div>

      <div className="p-5">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {providers.map((provider, index) => (
            <div
              key={index}
              className="bg-white border border-gray-100 rounded-lg shadow-sm hover:shadow-md transition-shadow duration-300"
            >
              <div className="p-4">
                <div className="flex items-center space-x-3">
                  <div className={`flex-shrink-0 ${provider.iconClass}`}>
                    <img
                      src={provider.iconSrc}
                      alt={`${provider.name} icon`}
                      className="h-8 w-8"
                    />
                  </div>
                  <div>
                    <h4 className="text-base font-semibold text-gray-900">
                      {provider.name}
                    </h4>
                    <div className="mt-1">
                      {provider.models.length > 0 ? (
                        <div className="flex flex-wrap gap-2 mt-2">
                          {provider.models.map((model, idx) => (
                            <span
                              key={idx}
                              className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800"
                            >
                              {model}
                            </span>
                          ))}
                        </div>
                      ) : (
                        <span className="text-sm text-gray-500">
                          {t(
                            'aiModelComponent.noModelsConfigured',
                            'No models configured'
                          )}
                        </span>
                      )}
                    </div>
                  </div>
                </div>
              </div>
              <div className="bg-gray-50 px-4 py-2 border-t border-gray-100">
                <div className="flex justify-between items-center">
                  <span className="text-xs font-medium text-gray-500">
                    {provider.models.length}{' '}
                    {provider.models.length === 1
                      ? t('aiModelComponent.modelSingular', 'model')
                      : t('aiModelComponent.modelPlural', 'models')}{' '}
                    {t('aiModelComponent.available', 'available')}
                  </span>
                  <span
                    className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${provider.models.length > 0 ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'}`}
                  >
                    {provider.models.length > 0
                      ? t('aiModelComponent.active', 'Active')
                      : t('aiModelComponent.inactive', 'Inactive')}
                  </span>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default AIModelComponent;
