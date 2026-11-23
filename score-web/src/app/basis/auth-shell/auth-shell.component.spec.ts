import {ComponentFixture, TestBed} from '@angular/core/testing';
import {of} from 'rxjs';
import {AuthShellComponent} from './auth-shell.component';
import {WebPageInfoService} from '../basis.service';

describe('AuthShellComponent', () => {
  let fixture: ComponentFixture<AuthShellComponent>;

  function setup(brand?: string) {
    TestBed.configureTestingModule({
      declarations: [AuthShellComponent],
      providers: [{provide: WebPageInfoService, useValue: {load: () => of(brand ? {brand} : null)}}]
    });
    fixture = TestBed.createComponent(AuthShellComponent);
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  it('shows the administrator-defined SVG logo', () => {
    const el = setup('<svg class="custom-logo" width="160" height="26"></svg>');
    expect(el.querySelector('.auth-brand svg.custom-logo')).not.toBeNull();
  });

  it('shows no logo block when no brand is set', () => {
    expect(setup().querySelector('.auth-brand')).toBeNull();
  });
});
