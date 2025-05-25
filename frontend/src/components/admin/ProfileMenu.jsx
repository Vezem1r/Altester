import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

const ProfileMenu = ({ handleLogout, username }) => {
  const { t } = useTranslation();

  return (
    <div className="origin-top-right absolute right-0 mt-2 w-48 rounded-md shadow-lg bg-white ring-1 ring-black ring-opacity-5 z-50">
      <div
        className="py-1"
        role="menu"
        aria-orientation="vertical"
        aria-labelledby="options-menu"
      >
        <div className="px-4 py-2 text-sm text-gray-700 border-b border-gray-100">
          <span className="block font-medium truncate">{username}</span>
        </div>
        <Link
          to="/admin/profile"
          className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
          role="menuitem"
        >
          {t('profileMenu.yourProfile', 'Your Profile')}
        </Link>
        <Link
          to="/admin/settings"
          className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
          role="menuitem"
        >
          {t('profileMenu.settings', 'Settings')}
        </Link>
        <button
          onClick={handleLogout}
          className="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
          role="menuitem"
        >
          {t('profileMenu.signOut', 'Sign out')}
        </button>
      </div>
    </div>
  );
};

export default ProfileMenu;
