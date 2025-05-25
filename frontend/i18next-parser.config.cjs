module.exports = {
  input: [
    'src/**/*.{js,jsx}',
  ],
  
  output: 'public/locales/$LOCALE/$NAMESPACE.json',
  locales: ['en', 'cs'],
  defaultNamespace: 'translation',
  keySeparator: false,
  namespaceSeparator: false,
  keepRemoved: true,
  
  defaultValue: function(locale, namespace, key, value) {
    if (locale === 'en') {
      return value;
    }
    return '';
  },
  
  sort: true,
  skipDefaultValues: false,
  addKeyAtTheBottom: false,
  debug: false,
  
  lexers: {
    js: ['JsxLexer'],
    jsx: ['JsxLexer'],
    default: ['JsxLexer'],
  },
  
  lineEnding: 'auto',
  createOldCatalogs: false,
};