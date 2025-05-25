import React from 'react';

const TabNavigation = ({ tabs, activeTab, onChange }) => {
    return (
        <div className="bg-white rounded-xl shadow-lg mb-6 overflow-hidden">
            <nav
                className="hidden md:flex border-b border-gray-200 w-full"
                aria-label="Tabs"
            >
                {tabs.map(tab => (
                    <button
                        key={tab.id}
                        onClick={() => onChange(tab.id)}
                        disabled={tab.disabled}
                        className={`
              flex-1 relative py-4 px-6 text-sm font-medium border-b-2 transition-all text-center
              ${
                            activeTab === tab.id
                                ? 'text-purple-600 border-purple-500'
                                : 'text-gray-500 border-transparent hover:text-gray-700 hover:border-gray-300'
                        }
              ${tab.disabled ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer'}
            `}
                    >
                        <div className="flex items-center justify-center">
                            {tab.icon && <span className="mr-2">{tab.icon}</span>}
                            {tab.label}
                            {tab.count !== undefined && (
                                <span className="ml-2 bg-gray-100 text-gray-700 py-0.5 px-2 rounded-full text-xs">
                  {tab.count}
                </span>
                            )}
                        </div>
                    </button>
                ))}
            </nav>
            <div className="md:hidden border-b border-gray-200">
                <select
                    value={activeTab}
                    onChange={e => onChange(e.target.value)}
                    className="w-full py-3 px-4 text-sm font-medium bg-white border-none focus:outline-none focus:ring-2 focus:ring-purple-500"
                >
                    {tabs.map(tab => (
                        <option key={tab.id} value={tab.id} disabled={tab.disabled}>
                            {tab.label}
                            {tab.count !== undefined && ` (${tab.count})`}
                        </option>
                    ))}
                </select>
            </div>
        </div>
    );
};

export default TabNavigation;
