import { useTranslation } from 'react-i18next';

const StatusBadge = ({ status, variant, children }) => {
  const { t } = useTranslation();

  const variants = {
    success: 'bg-green-100 text-green-800',
    warning: 'bg-yellow-100 text-yellow-800',
    error: 'bg-red-100 text-red-800',
    info: 'bg-blue-100 text-blue-800',
    default: 'bg-gray-100 text-gray-800',
    purple: 'bg-purple-100 text-purple-800',
  };

  const statusMap = {
    active: { variant: 'success', label: t('statusBadge.active', 'Active') },
    inactive: {
      variant: 'default',
      label: t('statusBadge.inactive', 'Inactive'),
    },
    future: { variant: 'info', label: t('statusBadge.future', 'Future') },
    open: { variant: 'success', label: t('statusBadge.open', 'Open') },
    closed: { variant: 'error', label: t('statusBadge.closed', 'Closed') },
    ldap: { variant: 'warning', label: t('statusBadge.ldapUser', 'LDAP User') },
    registered: {
      variant: 'default',
      label: t('statusBadge.registered', 'Registered'),
    },
  };

  const currentVariant =
    variant || (status && statusMap[status]?.variant) || 'default';
  const displayText =
    children || (status && statusMap[status]?.label) || status;

  return (
    <span
      className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full justify-center items-center ${variants[currentVariant]}`}
    >
      {displayText}
    </span>
  );
};

export default StatusBadge;
