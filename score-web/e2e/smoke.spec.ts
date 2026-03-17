import {expect, test} from '@playwright/test';

test('loads the application shell', async ({page}) => {
  await page.goto('/');

  await expect(page).toHaveTitle(/connectCenter/i);
  await expect(page.locator('score-web')).toBeVisible();
});
