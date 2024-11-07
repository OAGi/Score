import {ComponentFixture, TestBed} from '@angular/core/testing';
import {BiePackageListComponent} from './bie-package-list.component';

describe('BiePackageListComponent', () => {
  let component: BiePackageListComponent;
  let fixture: ComponentFixture<BiePackageListComponent>;

  beforeEach(fakeAsync () => {
    await TestBed.configureTestingModule({
      declarations: [BiePackageListComponent]
    })
      .compileComponents();

    fixture = TestBed.createComponent(BiePackageListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
