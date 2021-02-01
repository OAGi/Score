import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CodeListUpliftComponent } from './code-list-uplift.component';

describe('CodeListUpliftComponent', () => {
  let component: CodeListUpliftComponent;
  let fixture: ComponentFixture<CodeListUpliftComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CodeListUpliftComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CodeListUpliftComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
