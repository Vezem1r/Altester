import { useState, useEffect, useMemo, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import { TestApiKeyService } from '@/services/TestApiKeyService';
import { ApiKeyService } from '@/services/ApiKeyService';
import { PromptService } from '@/services/PromptService';
import { toast } from 'react-toastify';
import { AI_SERVICE_LABELS } from '@/constants/aiServices';
import Modal from '@/components/ui/Modal';
import ModalTabNavigation from '@/components/common/ModalTabNavigation';
import {
  OverviewTab,
  AssignKeyTab,
  ManageAiTab,
  PromptsTab,
} from '@/components/tabs';
import PromptViewModal from '@/components/modals/PromptViewModal';

import claudeIcon from '@/assets/icons/claude.svg';
import deepseekIcon from '@/assets/icons/deepseek.svg';
import geminiIcon from '@/assets/icons/gemini.svg';
import openaiIcon from '@/assets/icons/openai.svg';

const ApiKeyManagementModal = ({
  isOpen,
  testId,
  onClose,
  onKeysUpdated,
  associatedGroups = [],
}) => {
  const { t } = useTranslation();
  const [apiKeys, setApiKeys] = useState([]);
  const [availableKeys, setAvailableKeys] = useState([]);
  const [selectedKey, setSelectedKey] = useState('');
  const [selectedGroup, setSelectedGroup] = useState('');
  const [loading, setLoading] = useState(false);
  const [tabView, setTabView] = useState('overview');

  const [prompts, setPrompts] = useState([]);
  const [myPrompts, setMyPrompts] = useState([]);
  const [publicPrompts, setPublicPrompts] = useState([]);
  const [promptType, setPromptType] = useState('my');
  const [selectedPrompt, setSelectedPrompt] = useState('');
  const [currentPrompts, setCurrentPrompts] = useState({});
  const [selectedPromptGroup, setSelectedPromptGroup] = useState('');
  const [viewPrompt, setViewPrompt] = useState(null);

  useEffect(() => {
    if (!isOpen) {
      setSelectedKey('');
      setSelectedPrompt('');
      setViewPrompt(null);
      setTabView('overview');
    }
  }, [isOpen]);

  useEffect(() => {
    if (isOpen && testId) {
      loadTabData();
    }
  }, [isOpen, testId, tabView]);

  useEffect(() => {
    if (associatedGroups && associatedGroups.length > 0 && !selectedGroup) {
      setSelectedGroup(associatedGroups[0].id.toString());
    }
  }, [associatedGroups, selectedGroup]);

  const loadTabData = useCallback(async () => {
    if (!isOpen || !testId) return;

    switch (tabView) {
      case 'overview':
        await fetchTestApiKeys();
        break;
      case 'assign':
        await fetchAvailableApiKeys();
        break;
      case 'manage-ai':
        await fetchTestApiKeys();
        break;
      case 'prompts':
        await fetchPrompts();
        break;
      default:
        break;
    }
  }, [isOpen, testId, tabView]);

  const fetchPrompts = useCallback(async () => {
    try {
      setLoading(true);
      const [myPromptsRes, publicPromptsRes] = await Promise.all([
        PromptService.getMyPrompts(0, 100),
        PromptService.getPublicPrompts(0, 100),
      ]);

      setMyPrompts(myPromptsRes.content || []);
      setPublicPrompts(publicPromptsRes.content || []);
      setPrompts(
        promptType === 'my'
          ? myPromptsRes.content || []
          : publicPromptsRes.content || []
      );
    } catch (error) {
      toast.error(
        t('apiKeyManagementModalFailedToLoadPrompts', 'Failed to load prompts')
      );
      console.error(error);
    } finally {
      setLoading(false);
    }
  }, [promptType, t]);

  const fetchTestApiKeys = useCallback(async () => {
    try {
      setLoading(true);
      const response = await TestApiKeyService.getTestApiKeys(testId);
      setApiKeys(response.assignments || []);

      const promptsMap = {};
      response.assignments.forEach(assignment => {
        if (assignment.promptName) {
          promptsMap[assignment.group.groupId] = {
            name: assignment.promptName,
            id: assignment.promptId,
          };
        }
      });
      setCurrentPrompts(promptsMap);
    } catch {
      toast.error(
        t(
          'apiKeyManagementModalFailedToLoadApiKeys',
          'Failed to load API keys for test'
        )
      );
    } finally {
      setLoading(false);
    }
  }, [testId, t]);

  const fetchAvailableApiKeys = useCallback(async () => {
    try {
      setLoading(true);
      const response = await ApiKeyService.getAvailableApiKeys();
      setAvailableKeys(response || []);
    } catch {
      toast.error(
        t(
          'apiKeyManagementModalFailedToLoadAvailableKeys',
          'Failed to load available API keys'
        )
      );
    } finally {
      setLoading(false);
    }
  }, [t]);

  const handleAssignKey = useCallback(async () => {
    if (!selectedKey || !selectedGroup) {
      toast.warning(
        t(
          'apiKeyManagementModalSelectKeyAndGroup',
          'Please select an API key and group to assign'
        )
      );
      return;
    }

    try {
      setLoading(true);
      await TestApiKeyService.assignApiKeyToTest(
        parseInt(selectedKey),
        testId,
        parseInt(selectedGroup)
      );
      toast.success(
        t(
          'apiKeyManagementModalKeyAssignedSuccess',
          'API key assigned successfully'
        )
      );
      await fetchTestApiKeys();
      if (onKeysUpdated) onKeysUpdated();
      setTabView('overview');
    } catch (error) {
      toast.error(
        error.message ||
          t(
            'apiKeyManagementModalFailedToAssignKey',
            'Failed to assign API key'
          )
      );
    } finally {
      setLoading(false);
    }
  }, [selectedKey, selectedGroup, testId, fetchTestApiKeys, onKeysUpdated, t]);

  const handleUnassignKey = useCallback(
    async groupId => {
      try {
        setLoading(true);
        await TestApiKeyService.unassignApiKeyFromTest(testId, groupId);
        toast.success(
          t(
            'apiKeyManagementModalKeyUnassignedSuccess',
            'API key unassigned successfully'
          )
        );
        await fetchTestApiKeys();
        if (onKeysUpdated) onKeysUpdated();
      } catch (error) {
        toast.error(
          error.message ||
            t(
              'apiKeyManagementModalFailedToUnassignKey',
              'Failed to unassign API key'
            )
        );
      } finally {
        setLoading(false);
      }
    },
    [testId, fetchTestApiKeys, onKeysUpdated, t]
  );

  const handleToggleAiEvaluation = useCallback(
    async groupId => {
      try {
        setLoading(true);
        await TestApiKeyService.toggleAiEvaluation(testId, groupId);
        toast.success(
          t(
            'apiKeyManagementModalAiEvaluationUpdated',
            'AI evaluation setting updated'
          )
        );
        await fetchTestApiKeys();
        if (onKeysUpdated) onKeysUpdated();
      } catch (error) {
        toast.error(
          error.message ||
            t(
              'apiKeyManagementModalFailedToUpdateAiEvaluation',
              'Failed to update AI evaluation setting'
            )
        );
      } finally {
        setLoading(false);
      }
    },
    [testId, fetchTestApiKeys, onKeysUpdated, t]
  );

  const handlePromptTypeChange = useCallback(
    type => {
      setPromptType(type);
      setPrompts(type === 'my' ? myPrompts : publicPrompts);
      setSelectedPrompt('');
    },
    [myPrompts, publicPrompts]
  );

  const handleAssignPrompt = useCallback(async () => {
    if (!selectedPrompt || !selectedPromptGroup) {
      toast.warning(
        t(
          'apiKeyManagementModalSelectPromptAndGroup',
          'Please select a prompt and group'
        )
      );
      return;
    }

    try {
      setLoading(true);
      await ApiKeyService.updateAssignmentPrompt(
        testId,
        parseInt(selectedPromptGroup),
        parseInt(selectedPrompt)
      );
      toast.success(
        t(
          'apiKeyManagementModalPromptAssignedSuccess',
          'Prompt assigned successfully'
        )
      );
      await fetchTestApiKeys();
      if (onKeysUpdated) onKeysUpdated();
      setTabView('overview');
    } catch (error) {
      toast.error(
        error.message ||
          t(
            'apiKeyManagementModalFailedToAssignPrompt',
            'Failed to assign prompt'
          )
      );
    } finally {
      setLoading(false);
    }
  }, [
    selectedPrompt,
    selectedPromptGroup,
    testId,
    fetchTestApiKeys,
    onKeysUpdated,
    t,
  ]);

  const handleViewPrompt = useCallback(
    async promptId => {
      try {
        setLoading(true);
        const details = await PromptService.getPromptDetails(promptId);
        setViewPrompt(details);
      } catch {
        toast.error(
          t(
            'apiKeyManagementModalFailedToLoadPromptDetails',
            'Failed to load prompt details'
          )
        );
      } finally {
        setLoading(false);
      }
    },
    [t]
  );

  const getGroupInfo = useCallback(
    groupId => {
      const group = associatedGroups.find(g => g.id === groupId);
      const assignedKey = apiKeys.find(
        key => key.group && key.group.groupId === groupId
      );

      return {
        name: group
          ? group.name
          : t('apiKeyManagementModalUnknownGroup', 'Unknown Group'),
        hasApiKey: !!assignedKey,
        apiKeyName: assignedKey ? assignedKey.apiKeyName : null,
        aiServiceName: assignedKey ? assignedKey.aiServiceName : null,
        aiEvaluationEnabled: group ? group.aiEvaluationEnabled : false,
        currentPrompt: assignedKey
          ? {
              name: assignedKey.promptName,
              id: assignedKey.promptId,
            }
          : null,
        model: assignedKey ? assignedKey.model : null,
      };
    },
    [associatedGroups, apiKeys]
  );

  const getServiceDisplayName = useCallback(serviceEnum => {
    return AI_SERVICE_LABELS[serviceEnum] || serviceEnum;
  }, []);

  const getAiServiceIcon = useCallback(serviceName => {
    switch (serviceName) {
      case 'OPENAI':
        return openaiIcon;
      case 'CLAUDE':
        return claudeIcon;
      case 'GEMINI':
        return geminiIcon;
      case 'DEEPSEEK':
        return deepseekIcon;
      default:
        return null;
    }
  }, []);

  const tabs = useMemo(() => {
    return [
      {
        id: 'overview',
        label: t('apiKeyManagementModalOverview', 'Overview'),
        icon: 'M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z',
      },
      {
        id: 'assign',
        label: t('apiKeyManagementModalAssignApiKeys', 'Assign API Keys'),
        icon: 'M12 6v6m0 0v6m0-6h6m-6 0H6',
      },
      {
        id: 'manage-ai',
        label: t('apiKeyManagementModalAiEvaluation', 'AI Evaluation'),
        icon: 'M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z',
      },
      {
        id: 'prompts',
        label: t('apiKeyManagementModalPrompts', 'Prompts'),
        icon: 'M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z',
      },
    ];
  }, [t]);

  const renderTabContent = useCallback(() => {
    switch (tabView) {
      case 'overview':
        return (
          <OverviewTab
            associatedGroups={associatedGroups}
            getGroupInfo={getGroupInfo}
            apiKeys={apiKeys}
            handleUnassignKey={handleUnassignKey}
            handleToggleAiEvaluation={handleToggleAiEvaluation}
            setSelectedGroup={setSelectedGroup}
            setTabView={setTabView}
            getAiServiceIcon={getAiServiceIcon}
            getServiceDisplayName={getServiceDisplayName}
            currentPrompts={currentPrompts}
          />
        );
      case 'assign':
        return (
          <AssignKeyTab
            selectedKey={selectedKey}
            setSelectedKey={setSelectedKey}
            availableKeys={availableKeys}
            selectedGroup={selectedGroup}
            setSelectedGroup={setSelectedGroup}
            associatedGroups={associatedGroups}
            handleAssignKey={handleAssignKey}
            setTabView={setTabView}
            getServiceDisplayName={getServiceDisplayName}
            loading={loading}
          />
        );
      case 'manage-ai':
        return (
          <ManageAiTab
            associatedGroups={associatedGroups}
            getGroupInfo={getGroupInfo}
            apiKeys={apiKeys}
            handleToggleAiEvaluation={handleToggleAiEvaluation}
            setSelectedGroup={setSelectedGroup}
            setTabView={setTabView}
            getAiServiceIcon={getAiServiceIcon}
            getServiceDisplayName={getServiceDisplayName}
          />
        );
      case 'prompts':
        return (
          <PromptsTab
            selectedPromptGroup={selectedPromptGroup}
            setSelectedPromptGroup={setSelectedPromptGroup}
            associatedGroups={associatedGroups}
            promptType={promptType}
            handlePromptTypeChange={handlePromptTypeChange}
            prompts={prompts}
            selectedPrompt={selectedPrompt}
            setSelectedPrompt={setSelectedPrompt}
            handleAssignPrompt={handleAssignPrompt}
            setTabView={setTabView}
            loading={loading}
            handleViewPrompt={handleViewPrompt}
            viewPrompt={viewPrompt}
            setViewPrompt={setViewPrompt}
          />
        );
      default:
        return (
          <OverviewTab
            associatedGroups={associatedGroups}
            getGroupInfo={getGroupInfo}
            apiKeys={apiKeys}
            handleUnassignKey={handleUnassignKey}
            handleToggleAiEvaluation={handleToggleAiEvaluation}
            setSelectedGroup={setSelectedGroup}
            setTabView={setTabView}
            getAiServiceIcon={getAiServiceIcon}
            getServiceDisplayName={getServiceDisplayName}
            currentPrompts={currentPrompts}
          />
        );
    }
  }, [
    tabView,
    associatedGroups,
    getGroupInfo,
    apiKeys,
    handleUnassignKey,
    handleToggleAiEvaluation,
    setSelectedGroup,
    getAiServiceIcon,
    getServiceDisplayName,
    currentPrompts,
    selectedKey,
    setSelectedKey,
    availableKeys,
    selectedGroup,
    setSelectedGroup,
    handleAssignKey,
    selectedPromptGroup,
    setSelectedPromptGroup,
    promptType,
    handlePromptTypeChange,
    prompts,
    selectedPrompt,
    setSelectedPrompt,
    handleAssignPrompt,
    loading,
    handleViewPrompt,
    viewPrompt,
    setViewPrompt,
  ]);

  if (!isOpen) return null;

  return (
    <>
      <Modal
        isOpen={isOpen}
        onClose={onClose}
        size="lg"
        closeOnOverlayClick
        showCloseButton
        title={t('apiKeyManagementModalTitle', 'Test Configuration Center')}
        description={t(
          'apiKeyManagementModalDescription',
          'Manage API keys, AI evaluation settings, and prompts for your test'
        )}
      >
        <div>
          <ModalTabNavigation
            tabs={tabs}
            activeTab={tabView}
            onChange={setTabView}
          />

          <div className="mt-6">
            {loading && !renderTabContent() ? (
              <div className="flex justify-center py-12">
                <div className="animate-spin rounded-full h-12 w-12 border-4 border-purple-200 border-t-purple-600" />
              </div>
            ) : (
              renderTabContent()
            )}
          </div>
        </div>
      </Modal>

      {viewPrompt && (
        <PromptViewModal
          prompt={viewPrompt}
          onClose={() => setViewPrompt(null)}
        />
      )}
    </>
  );
};

export default ApiKeyManagementModal;
