import {ComponentFixture, TestBed} from '@angular/core/testing';
import {BiePackageDetailComponent} from './bie-package-detail.component';

describe('BiePackageDetailComponent', () => {
  let component: BiePackageDetailComponent;
  let fixture: ComponentFixture<BiePackageDetailComponent>;

  beforeEach(fakeAsync () => {
    await TestBed.configureTestingModule({
      declarations: [BiePackageDetailComponent]
    })
      .compileComponents();

    fixture = TestBed.createComponent(BiePackageDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
