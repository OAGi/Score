import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LibraryCreateComponent } from './library-create.component';

describe('LibraryCreateComponent', () => {
  let component: LibraryCreateComponent;
  let fixture: ComponentFixture<LibraryCreateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LibraryCreateComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LibraryCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
