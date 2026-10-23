// @ts-check
// Flat config. angular-eslint v22 dropped the legacy .eslintrc format, so this
// replaces the previous .eslintrc.json. Rule selection is carried over as-is.
const angular = require('angular-eslint');
const tsParser = require('@typescript-eslint/parser');
const tsPlugin = require('@typescript-eslint/eslint-plugin');

module.exports = [
  {
    ignores: ['dist/', 'node_modules/', '**/*.spec.ts'],
  },
  {
    files: ['**/*.ts'],
    languageOptions: {
      parser: tsParser,
      parserOptions: {
        project: ['tsconfig.eslint.json'],
        tsconfigRootDir: __dirname,
      },
    },
    plugins: {
      '@typescript-eslint': tsPlugin,
      '@angular-eslint': angular.tsPlugin,
    },
    processor: angular.processInlineTemplates,
    rules: {
      ...angular.configs.tsRecommended
        .map((c) => c.rules)
        .reduce((acc, rules) => ({...acc, ...rules}), {}),
      '@angular-eslint/component-selector': [
        'error',
        {
          type: 'element',
          prefix: 'score',
          style: 'kebab-case',
        },
      ],
      '@angular-eslint/component-class-suffix': 'off',
      '@angular-eslint/contextual-lifecycle': 'off',
      '@angular-eslint/directive-selector': 'off',
      '@angular-eslint/no-empty-lifecycle-method': 'off',
      '@angular-eslint/no-output-on-prefix': 'off',
      '@angular-eslint/prefer-inject': 'off',
      // New in angular-eslint v22's recommended set. It flags every component
      // that explicitly opts out of OnPush -- i.e. all 147 components the
      // Angular 22 migration annotated with ChangeDetectionStrategy.Eager to
      // preserve pre-v22 behavior.
      //
      // `Eager` is a supported, non-deprecated strategy in v22 (only the old
      // `Default` alias is deprecated, in favour of `Eager`), so these are not
      // deprecation debt. Converting them is a performance refactor, not part
      // of the upgrade: 828 of the 949 asynchronous callbacks in those
      // components assign to `this`, and under OnPush each one needs an
      // explicit `markForCheck()` or the view goes stale. That is a separate,
      // separately-verified exercise -- see the follow-ups on issue #1749.
      '@angular-eslint/prefer-on-push-component-change-detection': 'off',
      '@angular-eslint/prefer-standalone': 'off',
      // This is a convention, not a default: Angular 22 makes OnPush the default, and a component without `changeDetection`
      // silently stops repainting when its host mutates the model in place (the shared tree components did, once).
      // A subclass does not inherit the strategy of its base class either, so every @Component has to say it.
      'no-restricted-syntax': ['error', {
        selector: "Decorator > CallExpression[callee.name='Component'] > ObjectExpression:not(:has(> Property[key.name='changeDetection']))",
        message: 'This @Component has no changeDetection. Angular 22 makes OnPush the default, so the view stops repainting when the '
          + 'parent changes the model in place (a tree node, a table row, a form value): the screen shows stale data and e2e tests fail on it. '
          + 'A component does not inherit this from its base class or from the component it wraps, so each one has to say it. '
          + 'Add `changeDetection: ChangeDetectionStrategy.Eager` (what the rest of the app uses), '
          + 'or OnPush if the component only reads signals/immutable inputs and calls markForCheck() where needed.',
      }],
      '@angular-eslint/use-lifecycle-interface': 'off',
    },
  },
  {
    files: ['**/*.html'],
    languageOptions: {
      parser: angular.templateParser,
    },
    plugins: {
      '@angular-eslint/template': angular.templatePlugin,
    },
    rules: {
      ...angular.configs.templateRecommended
        .map((c) => c.rules)
        .reduce((acc, rules) => ({...acc, ...rules}), {}),
      '@angular-eslint/template/eqeqeq': 'off',
    },
  },
];
