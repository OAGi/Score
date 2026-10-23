import {NavbarComponent} from './navbar.component';

describe('NavbarComponent', () => {
  it('should be defined', () => {
    expect(NavbarComponent).toBeTruthy();
  });

  describe('Responsive Navbar & Sidenav Drawer', () => {
    it('should compute isCompact based on navbarWidth', () => {
      const component = Object.create(NavbarComponent.prototype);

      component.navbarWidth = 1400;
      expect(component.isCompact).toBe(false);

      component.navbarWidth = 1100;
      expect(component.isCompact).toBe(false);

      component.navbarWidth = 1099;
      expect(component.isCompact).toBe(true);

      component.navbarWidth = 800;
      expect(component.isCompact).toBe(true);
    });

    it('should compute showFullUserRole based on navbarWidth', () => {
      const component = Object.create(NavbarComponent.prototype);

      component.navbarWidth = 800;
      expect(component.showFullUserRole).toBe(true);

      component.navbarWidth = 740;
      expect(component.showFullUserRole).toBe(false);
    });

    it('should compute showNistLogo based on navbarWidth', () => {
      const component = Object.create(NavbarComponent.prototype);

      component.navbarWidth = 1200;
      expect(component.showNistLogo).toBe(true);

      component.navbarWidth = 1099;
      expect(component.showNistLogo).toBe(false);
    });

    it('should toggle, open, and close the drawer properly', () => {
      const component = Object.create(NavbarComponent.prototype);
      component.isDrawerOpen = false;

      component.toggleDrawer();
      expect(component.isDrawerOpen).toBe(true);

      component.toggleDrawer();
      expect(component.isDrawerOpen).toBe(false);

      component.openDrawer();
      expect(component.isDrawerOpen).toBe(true);

      component.closeDrawer();
      expect(component.isDrawerOpen).toBe(false);
    });

    it('should close the drawer on escape keypress', () => {
      const component = Object.create(NavbarComponent.prototype);
      component.isDrawerOpen = true;

      component.onEscape();
      expect(component.isDrawerOpen).toBe(false);
    });
  });
});
