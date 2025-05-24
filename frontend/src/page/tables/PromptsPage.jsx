import { useState, useEffect, useCallback } from 'react';
import { PromptService } from '@/services/PromptService';
import { toast } from 'react-toastify';
import {
  DocumentTextIcon,
  PlusIcon,
  PencilIcon,
  TrashIcon,
  EyeIcon,
  LockClosedIcon,
  GlobeAltIcon,
} from '@heroicons/react/outline';
import { useTranslation } from 'react-i18next';

import TablePageLayout from '@/layouts/TablePageLayout';
import DataTable from '@/components/table/DataTable';
import SearchBar from '@/components/search/SearchBar';
import SharedPagination from '@/components/common/SharedPagination';
import StatusBadge from '@/components/ui/StatusBadge';
import DeleteConfirmationModal from '@/components/modals/DeleteConfirmationModal';
import PromptForm from '@/components/modals/PromptForm';
import PromptViewModal from '@/components/modals/PromptViewModal';

const PromptsPage = () => {
  const { t } = useTranslation();
  const [activeTab, setActiveTab] = useState('all');
  const [prompts, setPrompts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalItems, setTotalItems] = useState(0);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [promptToDelete, setPromptToDelete] = useState(null);
  const [showFormModal, setShowFormModal] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [selectedPrompt, setSelectedPrompt] = useState(null);
  const [showViewModal, setShowViewModal] = useState(false);
  const [viewingPrompt, setViewingPrompt] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const itemsPerPage = 10;

  const [userRole, setUserRole] = useState(() => {
    return localStorage.getItem('userRole') || 'TEACHER';
  });

  const fetchPrompts = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      let response;

      switch (activeTab) {
        case 'all':
          if (userRole === 'ADMIN') {
            response = await PromptService.getAllPrompts(currentPage);
          } else {
            setError(
              t(
                'promptsPage.adminOnlyError',
                'Only administrators can view all prompts'
              )
            );
            setLoading(false);
            return;
          }
          break;
        case 'my':
          response = await PromptService.getMyPrompts(currentPage);
          break;
        case 'public':
          response = await PromptService.getPublicPrompts(currentPage);
          break;
        default:
          response = await PromptService.getMyPrompts(currentPage);
      }

      setPrompts(response.content || []);
      setTotalPages(response.totalPages || 0);
      setTotalItems(response.totalElements || 0);
      setError(null);
    } catch (fetchError) {
      setError(
        fetchError.message ||
          t('promptsPage.fetchError', 'Failed to fetch prompts')
      );
    } finally {
      setLoading(false);
    }
  }, [currentPage, activeTab, userRole, t]);

  useEffect(() => {
    const role = localStorage.getItem('userRole');
    if (role && role !== userRole) {
      setUserRole(role);
    }
  }, [userRole]);

  useEffect(() => {
    fetchPrompts();
  }, [fetchPrompts]);

  useEffect(() => {
    if (activeTab === 'all' && userRole !== 'ADMIN') {
      setActiveTab('my');
    }
  }, [userRole, activeTab]);

  const handlePageChange = page => {
    setCurrentPage(page);
  };

  const handleTabChange = tab => {
    setActiveTab(tab);
    setCurrentPage(0);
  };

  const handleSearchChange = e => {
    setSearchQuery(e.target.value);
    setCurrentPage(0);
  };

  const handleCreateClick = () => {
    setIsEditing(false);
    setSelectedPrompt(null);
    setShowFormModal(true);
  };

  const handleEditClick = async prompt => {
    try {
      setLoading(true);
      const details = await PromptService.getPromptDetails(prompt.id);
      setSelectedPrompt(details);
      setIsEditing(true);
      setShowFormModal(true);
    } catch (editError) {
      toast.error(
        editError.message ||
          t('promptsPage.loadDetailsError', 'Failed to load prompt details')
      );
    } finally {
      setLoading(false);
    }
  };

  const handleViewClick = async prompt => {
    try {
      setLoading(true);
      const details = await PromptService.getPromptDetails(prompt.id);
      setViewingPrompt(details);
      setShowViewModal(true);
    } catch (viewError) {
      toast.error(
        viewError.message ||
          t('promptsPage.loadDetailsError', 'Failed to load prompt details')
      );
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteClick = prompt => {
    setPromptToDelete(prompt);
    setShowDeleteModal(true);
  };

  const confirmDelete = async () => {
    try {
      setLoading(true);
      await PromptService.deletePrompt(promptToDelete.id);
      setShowDeleteModal(false);
      toast.success(
        t('promptsPage.deleteSuccess', 'Prompt deleted successfully')
      );
      fetchPrompts();
    } catch (deleteError) {
      toast.error(
        deleteError.message ||
          t('promptsPage.deleteError', 'Failed to delete prompt')
      );
    } finally {
      setLoading(false);
    }
  };

  const handleFormSubmit = async formData => {
    try {
      setLoading(true);
      if (isEditing && selectedPrompt) {
        await PromptService.updatePrompt(selectedPrompt.id, formData);
        toast.success(
          t('promptsPage.updateSuccess', 'Prompt updated successfully')
        );
      } else {
        await PromptService.createPrompt(formData);
        toast.success(
          t('promptsPage.createSuccess', 'Prompt created successfully')
        );
      }
      setShowFormModal(false);
      fetchPrompts();
    } catch (submitError) {
      toast.error(
        submitError.message ||
          t('promptsPage.saveError', 'Failed to save prompt')
      );
    } finally {
      setLoading(false);
    }
  };

  const tabs = [
    ...(userRole === 'ADMIN'
      ? [
          {
            id: 'all',
            name: t('promptsPage.allPrompts', 'All Prompts'),
            icon: DocumentTextIcon,
          },
        ]
      : []),
    {
      id: 'my',
      name: t('promptsPage.myPrompts', 'My Prompts'),
      icon: LockClosedIcon,
    },
    {
      id: 'public',
      name: t('promptsPage.publicPrompts', 'Public Prompts'),
      icon: GlobeAltIcon,
    },
  ];

  const columns = [
    { key: 'title', label: t('promptsPage.prompt', 'Prompt'), sortable: false },
    {
      key: 'author',
      label: t('promptsPage.author', 'Author'),
      sortable: false,
    },
    {
      key: 'created',
      label: t('promptsPage.created', 'Created'),
      sortable: false,
    },
    {
      key: 'actions',
      label: t('promptsPage.actions', 'Actions'),
      sortable: false,
    },
  ];

  const renderRow = prompt => (
    <tr key={prompt.id} className="hover:bg-gray-50">
      <td className="px-3 py-4 sm:px-6">
        <div>
          <div className="flex items-center">
            <h3 className="text-sm font-medium text-gray-900">
              {prompt.title}
            </h3>
            {prompt.isPublic && (
              <StatusBadge variant="success" className="ml-2">
                <GlobeAltIcon className="h-3 w-3 mr-1 inline" />{' '}
                {t('promptsPage.public', 'Public')}
              </StatusBadge>
            )}
          </div>
          {prompt.lastModified && (
            <p className="text-xs text-gray-400 mt-1">
              {t('promptsPage.lastModified', 'Last modified')}:{' '}
              {new Date(prompt.lastModified).toLocaleDateString()}
            </p>
          )}
        </div>
      </td>
      <td className="px-3 py-4 whitespace-nowrap text-sm text-gray-500 sm:px-6">
        {prompt.authorUsername}
      </td>
      <td className="px-3 py-4 whitespace-nowrap text-sm text-gray-500 sm:px-6">
        {new Date(prompt.created).toLocaleDateString()}
      </td>
      <td className="px-3 py-4 whitespace-nowrap text-right text-sm font-medium sm:px-6">
        <div className="flex items-center justify-end space-x-2">
          <button
            onClick={() => handleViewClick(prompt)}
            className="p-1 text-gray-400 hover:text-blue-600"
            title={t('promptsPage.viewPrompt', 'View prompt')}
          >
            <EyeIcon className="h-5 w-5" />
          </button>
          {(prompt.authorUsername === localStorage.getItem('username') ||
            userRole === 'ADMIN') && (
            <>
              <button
                onClick={() => handleEditClick(prompt)}
                className="p-1 text-gray-400 hover:text-purple-600"
                title={t('promptsPage.editPrompt', 'Edit prompt')}
              >
                <PencilIcon className="h-5 w-5" />
              </button>
              <button
                onClick={() => handleDeleteClick(prompt)}
                className="p-1 text-gray-400 hover:text-red-600"
                title={t('promptsPage.deletePromptButton', 'Delete prompt')}
              >
                <TrashIcon className="h-5 w-5" />
              </button>
            </>
          )}
        </div>
      </td>
    </tr>
  );

  const primaryAction = (
    <button
      onClick={handleCreateClick}
      className="inline-flex items-center transition-colors duration-200"
    >
      <PlusIcon className="-ml-1 mr-2 h-5 w-5" />
      {t('promptsPage.createPrompt', 'Create Prompt')}
    </button>
  );

  return (
    <TablePageLayout
      icon={<DocumentTextIcon />}
      title={t('promptsPage.title', 'Prompt Management')}
      description={t(
        'promptsPage.description',
        'Manage AI evaluation prompts for automated test assessments'
      )}
      primaryAction={primaryAction}
      variant="indigo-purple"
    >
      <div className="border-b border-gray-200 mb-6">
        <nav className="-mb-px flex space-x-8" aria-label="Tabs">
          {tabs.map(tab => (
            <button
              key={tab.id}
              onClick={() => handleTabChange(tab.id)}
              className={`
                flex items-center whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm
                ${
                  activeTab === tab.id
                    ? 'border-purple-500 text-purple-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }
              `}
            >
              <tab.icon
                className={`mr-2 h-5 w-5 ${
                  activeTab === tab.id ? 'text-purple-500' : 'text-gray-400'
                }`}
              />
              {tab.name}
            </button>
          ))}
        </nav>
      </div>

      {error && !loading && (
        <div className="mb-6 bg-red-50 border-l-4 border-red-400 p-4 rounded-md">
          <p className="text-sm text-red-700">{error}</p>
        </div>
      )}

      <SearchBar
        value={searchQuery}
        onChange={handleSearchChange}
        placeholder={t('promptsPage.searchPlaceholder', 'Search prompts...')}
        itemCount={totalItems}
        itemName={t('promptsPage.prompts', 'prompts')}
      />

      <DataTable
        columns={columns}
        data={prompts}
        loading={loading}
        renderRow={renderRow}
        emptyMessage={t('promptsPage.noPromptsFound', 'No prompts found')}
      />

      <div className="mt-6">
        <SharedPagination
          currentPage={currentPage}
          totalPages={totalPages}
          totalItems={totalItems}
          onPageChange={handlePageChange}
          itemName={t('promptsPage.prompts', 'prompts')}
          itemsPerPage={itemsPerPage}
        />
      </div>

      <DeleteConfirmationModal
        isOpen={showDeleteModal}
        title={t('promptsPage.deletePrompt', 'Delete Prompt')}
        description={t(
          'promptsPage.deletePromptConfirmation',
          'Are you sure you want to delete this prompt? This action cannot be undone.'
        )}
        itemName={promptToDelete?.title}
        onConfirm={confirmDelete}
        onCancel={() => setShowDeleteModal(false)}
      />

      {showFormModal && (
        <PromptForm
          isEditing={isEditing}
          initialData={selectedPrompt}
          onSubmit={handleFormSubmit}
          onCancel={() => setShowFormModal(false)}
        />
      )}

      {showViewModal && viewingPrompt && (
        <PromptViewModal
          prompt={viewingPrompt}
          onClose={() => setShowViewModal(false)}
        />
      )}
    </TablePageLayout>
  );
};

export default PromptsPage;
