import { useTranslation } from 'react-i18next';

const DataTable = ({
  columns,
  data,
  loading,
  emptyMessage = 'No data found',
  searchQuery,
  onSort,
  sortField,
  sortDirection,
  renderRow,
}) => {
  const { t } = useTranslation();

  const renderSortIndicator = field => {
    if (sortField !== field) return null;

    return <span className="ml-1">{sortDirection === 'asc' ? '▲' : '▼'}</span>;
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center py-12">
        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-purple-600" />
        <p className="ml-3 text-gray-600">
          {t('dataTable.loading', 'Loading...')}
        </p>
      </div>
    );
  }

  return (
    <div className="overflow-x-auto shadow-sm rounded-md border border-gray-200">
      <table className="min-w-full divide-y divide-gray-200">
        <thead className="bg-gray-50">
          <tr>
            {columns.map(column => (
              <th
                key={column.key}
                scope="col"
                className={`px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider whitespace-nowrap sm:px-6 ${
                  column.sortable ? 'cursor-pointer hover:bg-gray-100' : ''
                }`}
                onClick={() => column.sortable && onSort(column.key)}
              >
                <div className="flex items-center">
                  {column.label}
                  {column.sortable && renderSortIndicator(column.key)}
                </div>
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-gray-200">
          {data.length > 0 ? (
            data.map(item => renderRow(item))
          ) : (
            <tr>
              <td
                colSpan={columns.length}
                className="px-6 py-10 text-center text-gray-500"
              >
                {searchQuery
                  ? t(
                      'dataTable.noMatchingResults',
                      'No matching results for "{{searchQuery}}"',
                      { searchQuery }
                    )
                  : emptyMessage}
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
};

export default DataTable;
