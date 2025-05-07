import {ComponentFixture, fakeAsync, TestBed} from '@angular/core/testing';

import {BusinessTermUploadFileComponent} from './business-term-upload-file.component';

describe('BiztermCreateComponent', () => {
  let component: BusinessTermUploadFileComponent;
  let fixture: ComponentFixture<BusinessTermUploadFileComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [BusinessTermUploadFileComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BusinessTermUploadFileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
