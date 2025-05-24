import AppLayout from './AppLayout';

const CreationLayout = ({ children, title, subtitle, icon, headerActions }) => {
  return (
    <AppLayout>
      <div className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="py-6">
            <div className="flex items-center justify-between">
              <div className="flex items-center">
                {icon && <div className="mr-4 text-purple-600">{icon}</div>}
                <div>
                  <h1 className="text-3xl font-bold text-gray-900">{title}</h1>
                  {subtitle && (
                    <p className="mt-1 text-sm text-gray-500">{subtitle}</p>
                  )}
                </div>
              </div>
              {headerActions && (
                <div className="flex items-center space-x-3">
                  {headerActions}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      <div className="py-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div>{children}</div>
        </div>
      </div>
    </AppLayout>
  );
};

export default CreationLayout;
