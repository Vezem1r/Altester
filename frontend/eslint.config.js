import js from '@eslint/js';
import react from 'eslint-plugin-react';
import reactHooks from 'eslint-plugin-react-hooks';
import jsxA11y from 'eslint-plugin-jsx-a11y';
import prettierConfig from 'eslint-config-prettier';

export default [
  {
    ignores: ['**/*'],
    //ignores: ['dist/**', 'build/**', 'node_modules/**', 'public/**', '*.config.js'],
  },

  js.configs.recommended,

  {
    files: ['**/*.{js,jsx}'],
    plugins: {
      react,
      'react-hooks': reactHooks,
      'jsx-a11y': jsxA11y,
    },

    languageOptions: {
      ecmaVersion: 2024,
      sourceType: 'module',
      parserOptions: {
        ecmaFeatures: {
          jsx: true,
        },
      },
      globals: {
        window: 'readonly',
        document: 'readonly',
        console: 'readonly',
        process: 'readonly',
        Buffer: 'readonly',
        global: 'readonly',
        localStorage: 'readonly',
        sessionStorage: 'readonly',
        fetch: 'readonly',
        FormData: 'readonly',
        URLSearchParams: 'readonly',
        setTimeout: 'readonly',
        clearTimeout: 'readonly',
        setInterval: 'readonly',
        clearInterval: 'readonly',
        requestAnimationFrame: 'readonly',
        cancelAnimationFrame: 'readonly',
        AbortController: 'readonly',
        AbortSignal: 'readonly',
        URL: 'readonly',
        FileReader: 'readonly',
      },
    },

    settings: {
      react: {
        version: 'detect',
      },
    },

    rules: {
      'no-unused-vars': ['warn', { 
        ignoreRestSiblings: true, 
        argsIgnorePattern: '^_',
        varsIgnorePattern: '^_' 
      }],
      'no-console': 'warn',
      'no-debugger': 'warn',
      'no-alert': 'warn',
      'no-var': 'error',
      'prefer-const': 'error',
      'no-duplicate-imports': 'warn',
      
      'eqeqeq': ['error', 'always', { null: 'ignore' }],
      'curly': ['warn', 'multi-line'],
      'no-else-return': 'warn',
      'no-unneeded-ternary': 'warn',
      'prefer-template': 'warn',
      'object-shorthand': 'warn',
      
      'no-unreachable': 'error',
      'no-duplicate-case': 'error',
      'no-empty': ['warn', { allowEmptyCatch: true }],
      'no-extra-boolean-cast': 'warn',
      'no-constant-condition': 'warn',
      'no-undef': 'error',
      'valid-typeof': 'error',
      
      'react/react-in-jsx-scope': 'off',
      'react/prop-types': 'off',
      'react/jsx-uses-react': 'off',
      'react/jsx-uses-vars': 'error',
      'react/jsx-key': ['warn', { checkFragmentShorthand: true }],
      'react/no-array-index-key': 'warn',
      'react/no-unescaped-entities': 'warn',
      'react/self-closing-comp': 'warn',
      'react/jsx-boolean-value': ['warn', 'never'],
      'react/jsx-curly-brace-presence': ['warn', { 
        props: 'never', 
        children: 'never' 
      }],
      'react/jsx-fragments': ['warn', 'syntax'],
      'react/no-deprecated': 'warn',
      'react/display-name': 'off',
      'react/jsx-no-target-blank': ['warn', { enforceDynamicLinks: 'always' }],
      
      'react-hooks/rules-of-hooks': 'error',
      'react-hooks/exhaustive-deps': 'warn',
      
      'jsx-a11y/alt-text': 'warn',
      'jsx-a11y/anchor-is-valid': 'warn',
      'jsx-a11y/click-events-have-key-events': 'warn',
      'jsx-a11y/no-static-element-interactions': 'warn',
      'jsx-a11y/img-redundant-alt': 'warn',
      'jsx-a11y/no-autofocus': 'warn',
    },
  },

  prettierConfig,
];