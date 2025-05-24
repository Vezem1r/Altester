import { useState, useEffect } from 'react';
import Modal from '@/components/ui/Modal';
import ModalFooter from '@/components/ui/ModalFooter';
import FormField from '@/components/ui/FormField';
import { AI_SERVICE_OPTIONS, AI_SERVICE_MODELS } from '@/constants/aiServices';
import { useTranslation } from 'react-i18next';

const ApiKeyForm = ({
  initialData,
  isEditing,
  userRole,
  onSubmit,
  onCancel,
}) => {
  const { t } = useTranslation();
  const [formData, setFormData] = useState({
    name: initialData?.name || '',
    apiKey: '',
    aiServiceName: initialData?.aiServiceName || '',
    model: initialData?.model || '',
    isGlobal: initialData?.global || false,
    description: initialData?.description || '',
  });

  const [availableModels, setAvailableModels] = useState([]);

  const canCreateGlobalKeys = userRole === 'ADMIN';

  useEffect(() => {
    if (formData.aiServiceName) {
      setAvailableModels(AI_SERVICE_MODELS[formData.aiServiceName] || []);

      if (
        formData.model &&
        AI_SERVICE_MODELS[formData.aiServiceName] &&
        !AI_SERVICE_MODELS[formData.aiServiceName].some(
          model => model.value === formData.model
        )
      ) {
        const defaultModel =
          AI_SERVICE_MODELS[formData.aiServiceName]?.[0]?.value || '';
        setFormData(prev => ({ ...prev, model: defaultModel }));
      }
    } else {
      setAvailableModels([]);
      setFormData(prev => ({ ...prev, model: '' }));
    }
  }, [formData.aiServiceName]);

  useEffect(() => {
    if (!isEditing && !canCreateGlobalKeys && formData.isGlobal) {
      setFormData(prev => ({ ...prev, isGlobal: false }));
    }
  }, [isEditing, canCreateGlobalKeys, formData.isGlobal]);

  const handleInputChange = e => {
    const { name, value, type, checked } = e.target;

    if (name === 'isGlobal' && checked && !canCreateGlobalKeys) {
      return;
    }

    setFormData({
      ...formData,
      [name]: type === 'checkbox' ? checked : value,
    });
  };

  const handleSubmit = e => {
    e.preventDefault();
    onSubmit(formData);
  };

  const footer = (
    <ModalFooter
      primaryButtonText={
        isEditing
          ? t('ApiKeyForm.update', 'Update')
          : t('ApiKeyForm.create', 'Create')
      }
      secondaryButtonText={t('ApiKeyForm.cancel', 'Cancel')}
      onPrimaryClick={handleSubmit}
      onSecondaryClick={onCancel}
      reverseOrder
    />
  );

  return (
    <Modal
      isOpen
      onClose={onCancel}
      title={
        isEditing
          ? t('ApiKeyForm.editTitle', 'Edit API Key')
          : t('ApiKeyForm.addTitle', 'Add New API Key')
      }
      size="md"
      footer={footer}
    >
      <form onSubmit={handleSubmit} className="space-y-4 mt-4">
        <FormField
          label={t('ApiKeyForm.name', 'Name')}
          name="name"
          value={formData.name}
          onChange={handleInputChange}
          required
        />

        <FormField
          label={
            isEditing
              ? t(
                  'ApiKeyForm.apiKeyEditLabel',
                  'API Key (leave blank to keep current)'
                )
              : t('ApiKeyForm.apiKey', 'API Key')
          }
          name="apiKey"
          value={formData.apiKey}
          onChange={handleInputChange}
          required={!isEditing}
        />

        <FormField
          label={t('ApiKeyForm.aiService', 'AI Service')}
          name="aiServiceName"
          type="select"
          value={formData.aiServiceName}
          onChange={handleInputChange}
          required
        >
          <option value="">
            {t('ApiKeyForm.selectService', '-- Select AI Service --')}
          </option>
          {AI_SERVICE_OPTIONS.map(option => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </FormField>

        <FormField
          label={t('ApiKeyForm.aiModel', 'AI Model')}
          name="model"
          type="select"
          value={formData.model}
          onChange={handleInputChange}
          disabled={!formData.aiServiceName}
          required
        >
          <option value="">
            {t('ApiKeyForm.selectModel', '-- Select AI Model --')}
          </option>
          {availableModels.map(model => (
            <option key={model.value} value={model.value}>
              {model.label}
            </option>
          ))}
        </FormField>
        {!formData.aiServiceName && (
          <p className="mt-1 text-sm text-gray-500">
            {t(
              'ApiKeyForm.selectServiceFirst',
              'Select an AI service first to see available models'
            )}
          </p>
        )}

        <FormField
          label={t('ApiKeyForm.description', 'Description')}
          name="description"
          type="textarea"
          value={formData.description}
          onChange={handleInputChange}
          rows={3}
        />

        {(canCreateGlobalKeys || (isEditing && formData.isGlobal)) && (
          <div className="flex items-center">
            {isEditing ? (
              <div
                className={`flex items-center ${formData.isGlobal ? 'text-green-600' : 'text-gray-600'}`}
              >
                <svg
                  className={`h-5 w-5 mr-2 ${formData.isGlobal ? 'text-green-500' : 'text-gray-400'}`}
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M3.055 11H5a2 2 0 012 2v1a2 2 0 002 2 2 2 0 012 2v2.945M8 3.935V5.5A2.5 2.5 0 0010.5 8h.5a2 2 0 012 2 2 2 0 104 0 2 2 0 012-2h1.064M15 20.488V18a2 2 0 012-2h3.064M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                  />
                </svg>
                <span className="text-sm">
                  {t('ApiKeyForm.globalApiKeyStatus', 'Global API Key')}:{' '}
                  {formData.isGlobal
                    ? t('ApiKeyForm.yes', 'Yes')
                    : t('ApiKeyForm.no', 'No')}
                  <span className="ml-2 text-xs text-gray-500">
                    {t(
                      'ApiKeyForm.cannotBeChanged',
                      '(Cannot be changed after creation)'
                    )}
                  </span>
                </span>
              </div>
            ) : (
              <>
                <input
                  id="isGlobal"
                  name="isGlobal"
                  type="checkbox"
                  checked={formData.isGlobal}
                  onChange={handleInputChange}
                  disabled={!canCreateGlobalKeys}
                  className="h-4 w-4 text-purple-600 focus:ring-purple-500 border-gray-300 rounded disabled:opacity-50 disabled:cursor-not-allowed"
                />
                <label
                  htmlFor="isGlobal"
                  className={`ml-2 block text-sm items-center ${
                    canCreateGlobalKeys ? 'text-gray-900' : 'text-gray-500'
                  }`}
                >
                  {t('ApiKeyForm.globalApiKey', 'Global API Key')}
                  <span className="ml-1 inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-indigo-100 text-indigo-800">
                    {t('ApiKeyForm.availableToAll', 'available to all users')}
                  </span>
                  {!canCreateGlobalKeys && (
                    <div className="text-xs text-gray-500 mt-1">
                      {t(
                        'ApiKeyForm.adminOnlyFeature',
                        'Only administrators can create global API keys'
                      )}
                    </div>
                  )}
                </label>
              </>
            )}
          </div>
        )}

        {!canCreateGlobalKeys && !isEditing && (
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-3">
            <div className="flex">
              <svg
                className="h-5 w-5 text-blue-400"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
              <div className="ml-3">
                <p className="text-sm text-blue-700">
                  {t(
                    'ApiKeyForm.personalKeyInfo',
                    'This API key will be created as a personal key and will only be available to you.'
                  )}
                </p>
              </div>
            </div>
          </div>
        )}
      </form>
    </Modal>
  );
};

export default ApiKeyForm;
