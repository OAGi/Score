import {expect, test} from '@playwright/test';

test('loads the application shell', async ({page}) => {
  await page.goto('/');

  await expect(page).toHaveTitle(/connectCenter/i);

  // The Angular root host is mounted...
  await expect(page.locator('score-web')).toBeAttached();

  // ...and the SPA has actually rendered interactive content. We assert a concrete visible element
  // (the sign-in screen a fresh, unauthenticated session lands on) rather than toBeVisible() on the
  // <score-web> host itself — that host carries no layout box of its own, so toBeVisible() reports it
  // "hidden" even when the app rendered fine.
  await expect(page.locator('button.login-submit-button')).toBeVisible();
});
