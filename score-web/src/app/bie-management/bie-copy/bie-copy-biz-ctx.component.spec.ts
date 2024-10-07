import {fakeAsync, ComponentFixture, TestBed} from '@angular/core/testing';

import {BieCopyBizCtxComponent} from './bie-copy-biz-ctx.component';

describe('BieCopyBizCtxComponent', () => {
  let component: BieCopyBizCtxComponent;
  let fixture: ComponentFixture<BieCopyBizCtxComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [BieCopyBizCtxComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BieCopyBizCtxComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
