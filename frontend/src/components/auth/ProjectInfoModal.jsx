import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';

export default function ProjectInfoModal({ isOpen, onClose }) {
  const { t } = useTranslation();

  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = 'unset';
    }

    return () => {
      document.body.style.overflow = 'unset';
    };
  }, [isOpen]);

  if (!isOpen) return null;

  return (
      <div className="fixed inset-0 z-50 flex items-center justify-center p-2 sm:p-4">
        <div
            className="absolute inset-0 bg-black bg-opacity-60 backdrop-blur-sm"
            onClick={onClose}
        />

        <div className="relative bg-white rounded-2xl sm:rounded-3xl shadow-2xl max-w-sm sm:max-w-6xl w-full max-h-[95vh] overflow-hidden">
          <div className="absolute top-0 left-0 right-0 h-2 bg-gradient-to-r from-purple-600 via-pink-600 to-blue-600"></div>

          <button
              onClick={onClose}
              className="absolute top-3 right-3 sm:top-6 sm:right-6 text-gray-400 hover:text-gray-600 transition-colors z-10 bg-gray-100 hover:bg-gray-200 rounded-full p-1.5 sm:p-2"
          >
            <svg className="w-4 h-4 sm:w-5 sm:h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>

          <div className="p-4 sm:p-8 overflow-y-auto max-h-[95vh]">
            <div className="text-center mb-6 sm:mb-8">
              <div className="inline-flex items-center justify-center w-16 h-16 sm:w-20 sm:h-20 bg-gradient-to-r from-purple-600 to-pink-600 rounded-xl sm:rounded-2xl mb-3 sm:mb-4 shadow-lg">
                <svg className="w-8 h-8 sm:w-10 sm:h-10 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
                </svg>
              </div>

              <h1 className="text-2xl sm:text-4xl font-bold bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent mb-2 sm:mb-3">
                {t('projectInfo.welcome', 'Welcome to Altester')}
              </h1>
              <p className="text-sm sm:text-xl text-gray-600">
                {t('projectInfo.subtitle', 'AI-Powered Educational Testing Platform')}
              </p>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-4 sm:gap-8 mb-6 sm:mb-8">
              <div className="lg:col-span-2">
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-6">
                  <div className="bg-gradient-to-br from-purple-50 to-purple-100 p-4 sm:p-6 rounded-xl sm:rounded-2xl">
                    <h2 className="text-lg sm:text-xl font-bold text-purple-800 mb-2 sm:mb-3 flex items-center">
                      <svg className="w-5 h-5 sm:w-6 sm:h-6 mr-2" fill="currentColor" viewBox="0 0 20 20">
                        <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                      </svg>
                      {t('projectInfo.aboutPlatform.title', 'About Platform')}
                    </h2>
                    <p className="text-purple-700 text-xs sm:text-sm leading-relaxed">
                      {t('projectInfo.aboutPlatform.description', 'Comprehensive educational platform built with microservices architecture. Combines traditional assessment with cutting-edge AI evaluation capabilities, streamlining the entire test lifecycle from creation to grading.')}
                    </p>
                  </div>

                  <div className="bg-gradient-to-br from-blue-50 to-blue-100 p-4 sm:p-6 rounded-xl sm:rounded-2xl">
                    <h2 className="text-lg sm:text-xl font-bold text-blue-800 mb-2 sm:mb-3 flex items-center">
                      <svg className="w-5 h-5 sm:w-6 sm:h-6 mr-2" fill="currentColor" viewBox="0 0 20 20">
                        <path d="M3 4a1 1 0 011-1h12a1 1 0 011 1v2a1 1 0 01-1 1H4a1 1 0 01-1-1V4zM3 10a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H4a1 1 0 01-1-1v-6zM14 9a1 1 0 00-1 1v6a1 1 0 001 1h2a1 1 0 001-1v-6a1 1 0 00-1-1h-2z" />
                      </svg>
                      {t('projectInfo.architecture.title', 'Architecture')}
                    </h2>
                    <div className="space-y-1">
                      <div className="text-blue-700 text-xs sm:text-sm">• {t('projectInfo.architecture.authService', 'Authorization Service')}</div>
                      <div className="text-blue-700 text-xs sm:text-sm">• {t('projectInfo.architecture.chatService', 'Chat Service')}</div>
                      <div className="text-blue-700 text-xs sm:text-sm">• {t('projectInfo.architecture.notificationService', 'Notification Service')}</div>
                      <div className="text-blue-700 text-xs sm:text-sm">• {t('projectInfo.architecture.aiGradingService', 'AI Grading Service')}</div>
                      <div className="text-blue-700 text-xs sm:text-sm">• {t('projectInfo.architecture.coreService', 'Core Service')}</div>
                    </div>
                  </div>

                  <div className="bg-gradient-to-br from-green-50 to-green-100 p-4 sm:p-6 rounded-xl sm:rounded-2xl">
                    <h2 className="text-lg sm:text-xl font-bold text-green-800 mb-2 sm:mb-3 flex items-center">
                      <svg className="w-5 h-5 sm:w-6 sm:h-6 mr-2" fill="currentColor" viewBox="0 0 20 20">
                        <path d="M13 6a3 3 0 11-6 0 3 3 0 016 0zM18 8a2 2 0 11-4 0 2 2 0 014 0zM14 15a4 4 0 00-8 0v3h8v-3z" />
                      </svg>
                      {t('projectInfo.userRoles.title', 'User Roles')}
                    </h2>
                    <div className="space-y-1">
                      <div className="flex items-center text-green-700 text-xs sm:text-sm">
                        <div className="w-2 h-2 sm:w-3 sm:h-3 bg-red-500 rounded-full mr-2"></div>
                        {t('projectInfo.userRoles.administrator', 'Administrator')}
                      </div>
                      <div className="flex items-center text-green-700 text-xs sm:text-sm">
                        <div className="w-2 h-2 sm:w-3 sm:h-3 bg-blue-500 rounded-full mr-2"></div>
                        {t('projectInfo.userRoles.teacher', 'Teacher')}
                      </div>
                      <div className="flex items-center text-green-700 text-xs sm:text-sm">
                        <div className="w-2 h-2 sm:w-3 sm:h-3 bg-green-500 rounded-full mr-2"></div>
                        {t('projectInfo.userRoles.student', 'Student')}
                      </div>
                    </div>
                  </div>

                  <div className="bg-gradient-to-br from-orange-50 to-orange-100 p-4 sm:p-6 rounded-xl sm:rounded-2xl">
                    <h2 className="text-lg sm:text-xl font-bold text-orange-800 mb-2 sm:mb-3 flex items-center">
                      <svg className="w-5 h-5 sm:w-6 sm:h-6 mr-2" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M11.3 1.046A1 1 0 0112 2v5h4a1 1 0 01.82 1.573l-7 10A1 1 0 018 18v-5H4a1 1 0 01-.82-1.573l7-10a1 1 0 011.12-.38z" clipRule="evenodd" />
                      </svg>
                      {t('projectInfo.keyFeatures.title', 'Key Features')}
                    </h2>
                    <div className="space-y-0.5">
                      <div className="text-orange-700 text-xs sm:text-sm">• {t('projectInfo.keyFeatures.aiGrading', 'AI-Powered Grading')}</div>
                      <div className="text-orange-700 text-xs sm:text-sm">• {t('projectInfo.keyFeatures.realTimeCommunication', 'Real-time Communication')}</div>
                      <div className="text-orange-700 text-xs sm:text-sm">• {t('projectInfo.keyFeatures.ldapIntegration', 'LDAP Integration')}</div>
                      <div className="text-orange-700 text-xs sm:text-sm">• {t('projectInfo.keyFeatures.testManagement', 'Advanced Test Management')}</div>
                    </div>
                  </div>
                </div>
              </div>

              <div className="space-y-4 sm:space-y-6">
                <div className="bg-gradient-to-br from-gray-900 to-gray-800 p-4 sm:p-6 rounded-xl sm:rounded-2xl text-white">
                  <h2 className="text-lg sm:text-xl font-bold mb-3 sm:mb-4 flex items-center">
                    <svg className="w-5 h-5 sm:w-6 sm:h-6 mr-2" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M12.316 3.051a1 1 0 01.633 1.265l-4 12a1 1 0 11-1.898-.632l4-12a1 1 0 011.265-.633zM5.707 6.293a1 1 0 010 1.414L3.414 10l2.293 2.293a1 1 0 11-1.414 1.414l-3-3a1 1 0 010-1.414l3-3a1 1 0 011.414 0zm8.586 0a1 1 0 011.414 0l3 3a1 1 0 010 1.414l-3 3a1 1 0 11-1.414-1.414L16.586 10l-2.293-2.293a1 1 0 010-1.414z" clipRule="evenodd" />
                    </svg>
                    {t('projectInfo.techStack.title', 'Technology Stack')}
                  </h2>
                  <div className="space-y-2 sm:space-y-3">
                    <div>
                      <div className="text-sm font-semibold text-gray-300 mb-1">{t('projectInfo.techStack.backend', 'Backend')}</div>
                      <div className="text-xs text-gray-400">{t('projectInfo.techStack.backendTech', 'Java 21 • Spring Boot 3.4.5')}</div>
                      <div className="text-xs text-gray-400">{t('projectInfo.techStack.backendDb', 'PostgreSQL • Redis • WebSockets')}</div>
                    </div>
                    <div>
                      <div className="text-sm font-semibold text-gray-300 mb-1">{t('projectInfo.techStack.frontend', 'Frontend')}</div>
                      <div className="text-xs text-gray-400">{t('projectInfo.techStack.frontendTech', 'React • Tailwind CSS')}</div>
                      <div className="text-xs text-gray-400">{t('projectInfo.techStack.frontendClient', 'WebSocket Client')}</div>
                    </div>
                    <div>
                      <div className="text-sm font-semibold text-gray-300 mb-1">{t('projectInfo.techStack.aiIntegration', 'AI Integration')}</div>
                      <div className="text-xs text-gray-400">{t('projectInfo.techStack.aiTech1', 'LangChain4j • OpenAI')}</div>
                      <div className="text-xs text-gray-400">{t('projectInfo.techStack.aiTech2', 'Anthropic • Google Gemini')}</div>
                    </div>
                  </div>
                </div>

                <div className="bg-gradient-to-br from-indigo-50 to-indigo-100 p-4 sm:p-6 rounded-xl sm:rounded-2xl">
                  <h2 className="text-lg sm:text-xl font-bold text-indigo-800 mb-3 sm:mb-4 flex items-center">
                    <svg className="w-5 h-5 sm:w-6 sm:h-6 mr-2" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    {t('projectInfo.capabilities.title', 'Capabilities')}
                  </h2>
                  <div className="grid grid-cols-2 gap-1 sm:gap-2">
                    <div className="flex items-center text-indigo-700 text-xs">
                      <svg className="w-2 h-2 sm:w-3 sm:h-3 text-indigo-500 mr-1" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                      </svg>
                      {t('projectInfo.capabilities.testCreation', 'Test Creation')}
                    </div>
                    <div className="flex items-center text-indigo-700 text-xs">
                      <svg className="w-2 h-2 sm:w-3 sm:h-3 text-indigo-500 mr-1" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                      </svg>
                      {t('projectInfo.capabilities.autoGrading', 'Auto Grading')}
                    </div>
                    <div className="flex items-center text-indigo-700 text-xs">
                      <svg className="w-2 h-2 sm:w-3 sm:h-3 text-indigo-500 mr-1" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                      </svg>
                      {t('projectInfo.capabilities.chatSystem', 'Chat System')}
                    </div>
                    <div className="flex items-center text-indigo-700 text-xs">
                      <svg className="w-2 h-2 sm:w-3 sm:h-3 text-indigo-500 mr-1" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                      </svg>
                      {t('projectInfo.capabilities.notifications', 'Notifications')}
                    </div>
                    <div className="flex items-center text-indigo-700 text-xs">
                      <svg className="w-2 h-2 sm:w-3 sm:h-3 text-indigo-500 mr-1" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                      </svg>
                      {t('projectInfo.capabilities.userManagement', 'User Management')}
                    </div>
                    <div className="flex items-center text-indigo-700 text-xs">
                      <svg className="w-2 h-2 sm:w-3 sm:h-3 text-indigo-500 mr-1" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                      </svg>
                      {t('projectInfo.capabilities.analytics', 'Analytics')}
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div className="border-t border-gray-200 pt-4 sm:pt-6">
              <div className="flex flex-col items-center justify-between space-y-4 sm:space-y-0 sm:flex-row">
                <div className="text-center sm:text-left">
                  <p className="text-gray-600 text-sm mb-1">
                    {t('projectInfo.footer.thesisProject', "Bachelor's Thesis Project")}
                  </p>
                  <p className="text-gray-500 text-xs">
                    {t('projectInfo.footer.features', 'Microservices • AI Integration • Real-time Communication')}
                  </p>
                </div>

                <div className="flex flex-col sm:flex-row space-y-2 sm:space-y-0 sm:space-x-4 w-full sm:w-auto">
                  <a
                      href="https://github.com/Vezem1r/Altester/tree/main"
                      target="_blank"
                      rel="noopener noreferrer"
                      className="inline-flex items-center justify-center px-4 sm:px-6 py-2 sm:py-3 bg-gray-900 text-white rounded-lg sm:rounded-xl hover:bg-gray-800 transition-all duration-200 shadow-lg hover:shadow-xl transform hover:scale-105 text-sm sm:text-base"
                  >
                    <svg className="w-4 h-4 sm:w-5 sm:h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M10 0C4.477 0 0 4.484 0 10.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0110 4.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.203 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.942.359.31.678.921.678 1.856 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0020 10.017C20 4.484 15.522 0 10 0z" clipRule="evenodd" />
                    </svg>
                    {t('projectInfo.footer.githubRepository', 'GitHub Repository')}
                  </a>

                  <button
                      onClick={onClose}
                      className="inline-flex items-center justify-center px-4 sm:px-6 py-2 sm:py-3 bg-gradient-to-r from-purple-600 to-pink-600 text-white rounded-lg sm:rounded-xl hover:from-purple-700 hover:to-pink-700 transition-all duration-200 shadow-lg hover:shadow-xl transform hover:scale-105 text-sm sm:text-base"
                  >
                    <svg className="w-4 h-4 sm:w-5 sm:h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
                    </svg>
                    {t('projectInfo.footer.startExploring', 'Start Exploring')}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
  );
}