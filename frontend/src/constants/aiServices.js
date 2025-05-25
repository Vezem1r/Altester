export const AI_SERVICES = {
  OPENAI: 'OPENAI',
  ANTHROPIC_CLAUDE: 'ANTHROPIC_CLAUDE',
  DEEPSEEK: 'DEEPSEEK',
  GEMINI: 'GEMINI',
};

export const AI_SERVICE_LABELS = {
  [AI_SERVICES.OPENAI]: 'OpenAI',
  [AI_SERVICES.ANTHROPIC_CLAUDE]: 'Anthropic Claude',
  [AI_SERVICES.DEEPSEEK]: 'DeepSeek',
  [AI_SERVICES.GEMINI]: 'Google Gemini',
};

export const AI_SERVICE_OPTIONS = Object.entries(AI_SERVICES).map(
  ([value]) => ({
    value,
    label: AI_SERVICE_LABELS[value],
  })
);

const MODEL_LABELS = {
  'gpt-4o': 'GPT-4o',
  'gpt-4-turbo': 'GPT-4 Turbo',
  'gpt-3.5-turbo': 'GPT-3.5 Turbo',
  'claude-3-opus': 'Claude 3 Opus',
  'claude-3-sonnet': 'Claude 3 Sonnet',
  'claude-3-haiku': 'Claude 3 Haiku',
  'deepseek-coder': 'DeepSeek Coder',
  'gemini-2.0-flash': 'Gemini 2.0 Flash',
  'gemini-1.5-pro': 'Gemini 1.5 Pro',
};

const ENV = {
  OPENAI: import.meta.env.VITE_AI_MODELS_OPENAI,
  ANTHROPIC: import.meta.env.VITE_AI_MODELS_ANTHROPIC,
  DEEPSEEK: import.meta.env.VITE_AI_MODELS_DEEPSEEK,
  GEMINI: import.meta.env.VITE_AI_MODELS_GEMINI,
};

// Parse models from environment variables
// Returns empty array if no models are defined in env vars
const getModelsForService = (envValue) => {
  if (!envValue) return [];

  // If env var has multiple comma-separated models
  if (envValue.includes(',')) {
    return envValue
      .split(',')
      .map(model => model.trim())
      .filter(model => model)
      .map(model => ({
        value: model,
        label: MODEL_LABELS[model] || model,
      }));
  }

  // If env var has a single model
  return [
    {
      value: envValue,
      label: MODEL_LABELS[envValue] || envValue,
    },
  ];
};

// AI Models for each service
export const AI_SERVICE_MODELS = {
  [AI_SERVICES.OPENAI]: getModelsForService(ENV.OPENAI),
  [AI_SERVICES.ANTHROPIC_CLAUDE]: getModelsForService(ENV.ANTHROPIC),
  [AI_SERVICES.DEEPSEEK]: getModelsForService(ENV.DEEPSEEK),
  [AI_SERVICES.GEMINI]: getModelsForService(ENV.GEMINI),
};

const getFirstModel = (envValue) => {
  if (!envValue) return '';
  return envValue.split(',')[0].trim() || '';
};

export const DEFAULT_MODELS = {
  [AI_SERVICES.OPENAI]: getFirstModel(ENV.OPENAI),
  [AI_SERVICES.ANTHROPIC_CLAUDE]: getFirstModel(ENV.ANTHROPIC),
  [AI_SERVICES.DEEPSEEK]: getFirstModel(ENV.DEEPSEEK),
  [AI_SERVICES.GEMINI]: getFirstModel(ENV.GEMINI),
};