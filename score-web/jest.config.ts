import type { Config } from 'jest';
import { createCjsPreset } from 'jest-preset-angular/presets';

export default {
  ...createCjsPreset(),
  setupFilesAfterEnv: ['<rootDir>/setup-jest.ts'],
  testEnvironment: 'jsdom',
  testPathIgnorePatterns: ['<rootDir>/dist/', '<rootDir>/node_modules/'],
} satisfies Config;
