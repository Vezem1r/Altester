import { useState, useEffect, useCallback } from 'react';
import { ApiKeyService } from '@/services/ApiKeyService';
import { toast } from 'react-toastify';
import {
  KeyIcon,
  PlusIcon,
  TrashIcon,
  PencilIcon,
  SwitchHorizontalIcon,
  GlobeAltIcon,
} from '@heroicons/react/outline';
import { useTranslation } from 'react-i18next';

import TablePageLayout from '@/layouts/TablePageLayout';
import DataTable from '@/components/table/DataTable';
import SearchBar from '@/components/search/SearchBar';
import SharedPagination from '@/components/common/SharedPagination';
import StatusBadge from '@/components/ui/StatusBadge';
import DeleteConfirmationModal from '@/components/modals/DeleteConfirmationModal';
import ApiKeyForm from '@/components/modals/ApiKeyForm';
import { AI_SERVICE_LABELS } from '@/constants/aiServices';

const ApiKeysPage = () => {
  const { t } = useTranslation();
  const [apiKeys, setApiKeys] = useState([]);
  const [loading, setLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalItems, setTotalItems] = useState(0);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [keyToDelete, setKeyToDelete] = useState(null);
  const [showFormModal, setShowFormModal] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [selectedKey, setSelectedKey] = useState(null);
  const [userRole, setUserRole] = useState('ADMIN');
  const [searchQuery, setSearchQuery] = useState('');
  const itemsPerPage = 10;

  const fetchApiKeys = useCallback(async () => {
    try {
      setLoading(true);
      const response = await ApiKeyService.getAllApiKeys();
      setApiKeys(response.content || []);
      setTotalPages(response.totalPages || 0);
      setTotalItems(response.totalElements || 0);
    } catch (error) {
      toast.error(
        error.message || t('apiKeysPage.fetchError', 'Failed to fetch API keys')
      );
    } finally {
      setLoading(false);
    }
  }, [t]);

  useEffect(() => {
    const role = localStorage.getItem('userRole') || 'ADMIN';
    setUserRole(role);
    fetchApiKeys();
  }, [fetchApiKeys]);

  const handleSearchChange = e => {
    setSearchQuery(e.target.value);
    setCurrentPage(0);
  };

  const handleDeleteClick = key => {
    setKeyToDelete(key);
    setShowDeleteModal(true);
  };

  const confirmDelete = async () => {
    try {
      setLoading(true);
      await ApiKeyService.deleteApiKey(keyToDelete.id);
      setShowDeleteModal(false);
      toast.success(
        t('apiKeysPage.deleteSuccess', 'API key deleted successfully')
      );
      fetchApiKeys();
    } catch (error) {
      toast.error(
        error.message ||
          t('apiKeysPage.deleteError', 'Failed to delete API key')
      );
    } finally {
      setLoading(false);
    }
  };

  const handleCreateClick = () => {
    setIsEditing(false);
    setSelectedKey(null);
    setShowFormModal(true);
  };

  const handleEditClick = key => {
    setIsEditing(true);
    setSelectedKey(key);
    setShowFormModal(true);
  };

  const handleToggleStatus = async key => {
    try {
      setLoading(true);
      const response = await ApiKeyService.toggleApiKeyStatus(key.id);
      toast.success(
        response.active
          ? t('apiKeysPage.activatedSuccess', 'API key activated successfully')
          : t(
              'apiKeysPage.deactivatedSuccess',
              'API key deactivated successfully'
            )
      );
      fetchApiKeys();
    } catch (error) {
      toast.error(
        error.message ||
          t('apiKeysPage.toggleStatusError', 'Failed to toggle API key status')
      );
    } finally {
      setLoading(false);
    }
  };

  const handleFormSubmit = async formData => {
    try {
      setLoading(true);
      if (!isEditing) {
        await ApiKeyService.createApiKey(formData);
        toast.success(
          t('apiKeysPage.createSuccess', 'API key created successfully')
        );
      } else if (selectedKey) {
        await ApiKeyService.updateApiKey(selectedKey.id, formData);
        toast.success(
          t('apiKeysPage.updateSuccess', 'API key updated successfully')
        );
      }
      setShowFormModal(false);
      fetchApiKeys();
    } catch (error) {
      toast.error(
        error.message || t('apiKeysPage.saveError', 'Failed to save API key')
      );
    } finally {
      setLoading(false);
    }
  };

  const getServiceDisplayName = serviceEnum => {
    return AI_SERVICE_LABELS[serviceEnum] || serviceEnum;
  };

  const columns = [
    { key: 'name', label: t('apiKeysPage.apiKey', 'API Key'), sortable: false },
    {
      key: 'service',
      label: t('apiKeysPage.service', 'Service'),
      sortable: false,
    },
    {
      key: 'status',
      label: t('apiKeysPage.status', 'Status'),
      sortable: false,
    },
    {
      key: 'createdBy',
      label: t('apiKeysPage.createdBy', 'Created By'),
      sortable: false,
    },
    {
      key: 'actions',
      label: t('apiKeysPage.actions', 'Actions'),
      sortable: false,
    },
  ];

  const renderRow = apiKey => {
    const canManageKey = userRole === 'ADMIN' || apiKey.ownedByCurrentUser;

    return (
      <tr
        key={apiKey.id}
        className={`hover:bg-gray-50 ${!apiKey.active ? 'bg-gray-50' : ''}`}
      >
        <td className="px-3 py-4 sm:px-6">
          <div className="flex items-center">
            <div className="flex-shrink-0 h-10 w-10 flex items-center justify-center bg-purple-100 rounded-full">
              <KeyIcon className="h-6 w-6 text-purple-600" />
            </div>
            <div className="ml-4">
              <div className="flex items-center">
                <div
                  className={`text-sm font-medium ${apiKey.active ? 'text-gray-900' : 'text-gray-500'}`}
                >
                  {apiKey.name}
                </div>
                {apiKey.global && (
                  <StatusBadge variant="success" className="ml-2">
                    <GlobeAltIcon className="h-3 w-3 mr-1 inline" />{' '}
                    {t('apiKeysPage.global', 'Global')}
                  </StatusBadge>
                )}
              </div>
              <div className="text-sm text-gray-500">
                <code className="px-2 py-0.5 bg-gray-100 rounded text-xs">
                  {apiKey.keyMasked}
                </code>
              </div>
              {apiKey.description && (
                <div className="text-xs text-gray-500 mt-1 max-w-md truncate">
                  {apiKey.description}
                </div>
              )}
            </div>
          </div>
        </td>
        <td className="px-3 py-4 whitespace-nowrap sm:px-6">
          <div className="text-sm text-gray-900">
            {getServiceDisplayName(apiKey.aiServiceName)}
          </div>
          <div className="text-xs text-gray-500">
            {t('apiKeysPage.model', 'Model')}:{' '}
            {apiKey.model || t('apiKeysPage.notSpecified', 'Not specified')}
          </div>
        </td>
        <td className="px-3 py-4 whitespace-nowrap sm:px-6">
          <StatusBadge status={apiKey.active ? 'active' : 'inactive'} />
        </td>
        <td className="px-3 py-4 whitespace-nowrap text-sm text-gray-500 sm:px-6">
          <div className="text-sm">{apiKey.ownerUsername}</div>
          <div className="text-xs">
            {new Date(apiKey.createdAt).toLocaleDateString()}
          </div>
        </td>
        <td className="px-3 py-4 whitespace-nowrap text-right text-sm font-medium sm:px-6">
          <div className="flex justify-end space-x-2">
            {canManageKey && (
              <>
                <button
                  onClick={() => handleToggleStatus(apiKey)}
                  className={`inline-flex items-center p-1 border ${
                    apiKey.active
                      ? 'border-red-300 text-red-700 hover:bg-red-50'
                      : 'border-green-300 text-green-700 hover:bg-green-50'
                  } text-xs font-medium rounded`}
                  title={
                    apiKey.active
                      ? t('apiKeysPage.deactivate', 'Deactivate')
                      : t('apiKeysPage.activate', 'Activate')
                  }
                >
                  <SwitchHorizontalIcon className="h-4 w-4" />
                </button>

                <button
                  onClick={() => handleEditClick(apiKey)}
                  className="inline-flex items-center p-1 border border-gray-300 text-xs font-medium rounded text-gray-700 bg-white hover:bg-gray-50"
                  title={t('apiKeysPage.edit', 'Edit')}
                >
                  <PencilIcon className="h-4 w-4" />
                </button>
              </>
            )}

            {apiKey.ownedByCurrentUser || userRole === 'ADMIN' ? (
              <button
                onClick={() => handleDeleteClick(apiKey)}
                className="inline-flex items-center p-1 border border-transparent text-xs font-medium rounded text-white bg-red-600 hover:bg-red-700"
                title={t('apiKeysPage.delete', 'Delete')}
              >
                <TrashIcon className="h-4 w-4" />
              </button>
            ) : (
              <div className="w-6" />
            )}
          </div>
        </td>
      </tr>
    );
  };

  const primaryAction = (
    <button
      onClick={handleCreateClick}
      className="inline-flex items-center transition-colors duration-200"
    >
      <PlusIcon className="-ml-1 mr-2 h-5 w-5" aria-hidden="true" />
      {t('apiKeysPage.addNewApiKey', 'Add New API Key')}
    </button>
  );

  return (
    <TablePageLayout
      icon={<KeyIcon />}
      title={t('apiKeysPage.title', 'API Keys Management')}
      description={t(
        'apiKeysPage.description',
        'Manage API keys for different AI services used in your application'
      )}
      primaryAction={primaryAction}
      variant="purple-dark"
    >
      <SearchBar
        value={searchQuery}
        onChange={handleSearchChange}
        placeholder={t('apiKeysPage.searchPlaceholder', 'Search API keys...')}
        itemCount={totalItems}
        itemName={t('apiKeysPage.apiKeys', 'API keys')}
      />

      <DataTable
        columns={columns}
        data={apiKeys}
        loading={loading}
        renderRow={renderRow}
        emptyMessage={t('apiKeysPage.noApiKeysFound', 'No API keys found')}
      />

      <div className="mt-6">
        <SharedPagination
          currentPage={currentPage}
          totalPages={totalPages}
          totalItems={totalItems}
          onPageChange={setCurrentPage}
          itemName={t('apiKeysPage.apiKeys', 'API keys')}
          itemsPerPage={itemsPerPage}
        />
      </div>

      <DeleteConfirmationModal
        isOpen={showDeleteModal}
        title={t('apiKeysPage.deleteApiKey', 'Delete API Key')}
        description={
          keyToDelete?.global
            ? t(
                'apiKeysPage.deleteGlobalWarning',
                'Warning: You are about to delete a global API key. This will affect all users and tests that use this key. Are you sure you want to proceed?'
              )
            : t(
                'apiKeysPage.deleteWarning',
                'Are you sure you want to delete this API key? This action cannot be undone and will affect any tests using this key.'
              )
        }
        itemName={keyToDelete?.name}
        onConfirm={confirmDelete}
        onCancel={() => setShowDeleteModal(false)}
      />

      {showFormModal && (
        <ApiKeyForm
          initialData={selectedKey}
          isEditing={isEditing}
          userRole={userRole}
          onSubmit={handleFormSubmit}
          onCancel={() => setShowFormModal(false)}
        />
      )}
    </TablePageLayout>
  );
};

export default ApiKeysPage;
