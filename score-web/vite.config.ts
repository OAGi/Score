/// <reference types="vitest" />

import path from 'node:path';
import { defineConfig } from 'vite';

import angular from '@analogjs/vite-plugin-angular';

export default defineConfig({
  plugins: [angular()],
  test: {
    globals: true,
    setupFiles: ['src/test-setup.ts'],
    environment: 'jsdom',
    include: ['src/**/*.spec.ts'],
    exclude: ['dist/**', 'e2e/**', 'node_modules/**'],
    reporters: ['default']
  },
  resolve: {
    alias: {
      src: path.resolve(__dirname, 'src')
    }
  }
});
