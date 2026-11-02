import {Component, signal} from '@angular/core';
import {TestBed} from '@angular/core/testing';
import {provideRouter, Router} from '@angular/router';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {ResponsiveButtonComponent} from './responsive-button.component';

@Component({
  imports: [ResponsiveButtonComponent],
  template: `
    <score-responsive-button class="pressed" label="New Thing" (pressed)="count.set(count() + 1)"/>
    <score-responsive-button class="linked" label="New Linked" link="create"/>
    <score-responsive-button class="discard" label="Discard" icon="delete" color="warn" [disabled]="!enabled()" (pressed)="count.set(count() + 10)"/>`
})
class HostComponent {
  count = signal(0);
  enabled = signal(false);
}

@Component({template: ''})
class CreateComponent {
}

describe('ResponsiveButtonComponent', () => {
  const render = () => {
    TestBed.configureTestingModule({imports: [NoopAnimationsModule], providers: [provideRouter([{path: 'create', component: CreateComponent}])]});
    const fixture = TestBed.createComponent(HostComponent);
    fixture.detectChanges();
    return fixture;
  };

  it('is a button with its text, and the icon button of the narrow screen, which has the text as its name', () => {
    const el: HTMLElement = render().nativeElement.querySelector('.pressed');
    expect(el.querySelector('.responsive-text').textContent.trim()).toBe('New Thing');
    expect(el.querySelector('.responsive-icon mat-icon').textContent.trim()).toBe('add');
    expect(el.querySelector('.responsive-icon').getAttribute('aria-label')).toBe('New Thing');
  });

  it('tells that it is pressed, whichever of the two is', () => {
    const fixture = render();
    const el: HTMLElement = fixture.nativeElement.querySelector('.pressed');
    (el.querySelector('.responsive-text') as HTMLElement).click();
    (el.querySelector('.responsive-icon') as HTMLElement).click();
    expect(fixture.componentInstance.count()).toBe(2);
  });

  it('has the icon, the colour and the disabled state of the page: a trash can, warn, and no press while it is disabled', () => {
    const fixture = render();
    const el: HTMLElement = fixture.nativeElement.querySelector('.discard');
    expect(el.querySelector('.responsive-icon mat-icon').textContent.trim()).toBe('delete');
    expect(el.querySelector('.responsive-text').classList.contains('mat-warn')).toBe(true);
    expect(el.querySelector('.responsive-icon').classList.contains('mat-warn')).toBe(true);
    expect(el.querySelector('.responsive-text').hasAttribute('disabled')).toBe(true);
    expect(el.querySelector('.responsive-icon').hasAttribute('disabled')).toBe(true);
    fixture.componentInstance.enabled.set(true);
    fixture.detectChanges();
    expect(el.querySelector('.responsive-text').hasAttribute('disabled')).toBe(false);
    (el.querySelector('.responsive-icon') as HTMLElement).click();
    expect(fixture.componentInstance.count()).toBe(10);
  });

  it('leads to its link, whichever of the two is', async () => {
    const fixture = render();
    const router = TestBed.inject(Router);
    const el: HTMLElement = fixture.nativeElement.querySelector('.linked');
    for (const selector of ['.responsive-text', '.responsive-icon']) {
      await router.navigateByUrl('/');
      (el.querySelector(selector) as HTMLElement).click();
      await fixture.whenStable();
      expect(router.url).toBe('/create');
    }
  });
});
