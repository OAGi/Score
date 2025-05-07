import {ComponentFixture, fakeAsync, TestBed} from '@angular/core/testing';
import {BiePackageAddBieDialogComponent} from './bie-package-add-bie-dialog.component';

describe('BiePackageAddBieDialogComponent', () => {
  let component: BiePackageAddBieDialogComponent;
  let fixture: ComponentFixture<BiePackageAddBieDialogComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [BiePackageAddBieDialogComponent]
    })
        .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BiePackageAddBieDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});