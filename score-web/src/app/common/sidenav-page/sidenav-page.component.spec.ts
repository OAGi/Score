import {ChangeDetectionStrategy, Component, ViewChild} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {ScoreCommonModule} from '../score-common.module';
import {CommentDrawerControl, SidenavPageComponent} from './sidenav-page.component';

@Component({
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [ScoreCommonModule],
  template: `
    <score-sidenav-page [commentControl]="control" [currentUser]="'tester'" [title]="title"
      [loading]="loading" [progress]="progress" [showToolbar]="showToolbar">
      <ng-container ngProjectAs="[sidenavActions]"><button class="action">Act</button></ng-container>
      <p class="body">Body</p>
    </score-sidenav-page>`
})
class HostComponent {
  @ViewChild(SidenavPageComponent, {static: true}) page!: SidenavPageComponent;
  control: CommentDrawerControl | null = null;
  title: string | null = 'Detail';
  loading = false;
  progress = false;
  showToolbar = true;
}

function commentControl(): CommentDrawerControl {
  return {
    comments: [{
      commentId: 1, prevCommentId: null, text: 'Hello', hidden: false, isNew: false, isEditing: false,
      created: {who: {loginId: 'tester'}, when: new Date()}, timestamp: new Date()
    } as any],
    commentMessage: '', replyCommentId: null, replyMessage: '', replyOpened: false,
    closeCommentSlide: () => {}, deleteComment: () => {}, openEditComment: () => {}, editComment: () => {},
    cancelEditComment: () => {}, openReply: () => {}, closeReply: () => {}, addComment: () => {}
  };
}

describe('SidenavPageComponent', () => {
  let fixture: ComponentFixture<HostComponent>;
  let host: HostComponent;
  const el = (selector: string): HTMLElement | null => fixture.nativeElement.querySelector(selector);
  // The fixture host is checked like an OnPush view, so a changed host property needs to be marked.
  const detect = (): void => {
    fixture.changeDetectorRef.markForCheck();
    fixture.detectChanges();
  };

  beforeEach(() => {
    TestBed.configureTestingModule({imports: [NoopAnimationsModule]});
    fixture = TestBed.createComponent(HostComponent);
    host = fixture.componentInstance;
  });

  it('exposes the drawer to the host before its first change detection', () => {
    expect(host.page.sidenav).toBeTruthy();
  });

  it('renders the content in a base page inside the sidenav content', () => {
    detect();
    expect(el('mat-sidenav-content div.context-section > p.body')).toBeTruthy();
    expect(el('mat-sidenav-content mat-toolbar span.title')?.textContent).toBe('Detail');
    expect(el('mat-sidenav-content mat-toolbar button.action')).toBeTruthy();
  });

  it('leaves the drawer empty without a comment control', () => {
    detect();
    const drawer = el('mat-sidenav')!;
    expect(drawer.classList.contains('pd-15')).toBe(false);
    expect(drawer.querySelector('.ui.feed')).toBeNull();
  });

  it('renders the comments of the control in the drawer', () => {
    host.control = commentControl();
    detect();
    const drawer = el('mat-sidenav')!;
    expect(drawer.classList.contains('pd-15')).toBe(true);
    expect(drawer.getAttribute('position')).toBe('end');
    expect(drawer.querySelector('h4.dividing')?.textContent).toBe('Comment');
    expect(drawer.querySelector('.ui.feed .summary a')?.textContent).toBe('tester');
    expect(drawer.querySelector('.ui.feed .extra.text span')?.textContent).toBe('Hello');
  });

  it('can hide the toolbar', () => {
    host.showToolbar = false;
    detect();
    expect(el('mat-toolbar')).toBeNull();
  });

  it('shows the progress bar and the spinner independently', () => {
    detect();
    expect(el('.main-content-loading-overlay')).toBeNull();
    expect(el('.loading-container')).toBeNull();
    host.progress = true;
    detect();
    expect(el('mat-sidenav-content > .main-content-loading-overlay mat-progress-bar')).toBeTruthy();
    expect(el('.loading-container')).toBeNull();
    host.progress = false;
    host.loading = true;
    detect();
    expect(el('.main-content-loading-overlay')).toBeNull();
    expect(el('div.context-section > .loading-container mat-spinner')).toBeTruthy();
  });
});
