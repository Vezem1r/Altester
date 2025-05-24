import { useTranslation } from 'react-i18next';

const ITEM_NAME_TRANSLATION_KEYS = {
  items: 'searchBar.items',
  tests: 'searchBar.tests',
  students: 'searchBar.students',
  groups: 'searchBar.groups',
  attempts: 'searchBar.attempts',
  questions: 'searchBar.questions',
};

const SearchBar = ({
  value,
  onChange,
  placeholder = 'Search...',
  filters = [],
  itemCount,
  itemName = 'items',
}) => {
  const { t } = useTranslation();

  const getItemNameTranslationKey = name => {
    return ITEM_NAME_TRANSLATION_KEYS[name] || 'searchBar.items';
  };

  return (
    <div className="mb-6">
      <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
        <div className="md:col-span-2">
          <label htmlFor="search" className="sr-only">
            {t('searchBar.search', 'Search')}
          </label>
          <div className="relative">
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <svg
                className="h-5 w-5 text-gray-400"
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 20 20"
                fill="currentColor"
              >
                <path
                  fillRule="evenodd"
                  d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z"
                  clipRule="evenodd"
                />
              </svg>
            </div>
            <input
              id="search"
              name="search"
              type="text"
              value={value}
              onChange={onChange}
              className="block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md leading-5 bg-white shadow-sm focus:outline-none focus:ring-purple-500 focus:border-purple-500 sm:text-sm"
              placeholder={
                placeholder || t('searchBar.searchPlaceholder', 'Search...')
              }
            />
          </div>
        </div>

        {filters.length > 0 && (
          <div className="flex items-center space-x-2">
            {filters.map((filter, index) => (
              <select
                key={index}
                value={filter.value}
                onChange={filter.onChange}
                className="block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-purple-500 focus:border-purple-500 sm:text-sm rounded-md"
              >
                {filter.options.map(option => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            ))}
          </div>
        )}
      </div>

      {itemCount !== undefined && (
        <div className="mt-2">
          <p className="text-sm text-gray-500">
            {t('searchBar.foundItems', 'Found {{count}} {{itemName}}', {
              count: itemCount,
              itemName: t(getItemNameTranslationKey(itemName), itemName),
            })}
            {value
              ? t('searchBar.matching', ' matching "{{query}}"', {
                  query: value,
                })
              : ''}
          </p>
        </div>
      )}
    </div>
  );
};

export default SearchBar;
