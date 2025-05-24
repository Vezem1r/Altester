import React, { createContext } from 'react';

export const ReviewNavigationContext = createContext({
  goToNextPage: () => {},
  goToPrevPage: () => {},
  goToPage: () => {},
  toggleSummary: () => {},
  handleBackToDashboard: () => {},
  currentPage: 1,
  totalPages: 1,
  showSummary: true,
});

export const ReviewNavigationProvider = ({ children, value }) => {
  return (
    <ReviewNavigationContext.Provider value={value}>
      {children}
    </ReviewNavigationContext.Provider>
  );
};

export default ReviewNavigationContext;
