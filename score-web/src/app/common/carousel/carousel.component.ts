import {ChangeDetectionStrategy, Component, DestroyRef, ElementRef, EventEmitter, inject, Input, OnInit, Output, ViewChild} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {FaIconComponent} from '@fortawesome/angular-fontawesome';
import {faChevronLeft, faChevronRight} from '@fortawesome/free-solid-svg-icons';

/**
 * What is put inside is a row of slides, `perView` of them at a time (`perViewNarrow` on a narrow screen), swiped sideways, dragged with
 * the mouse or stepped with the buttons and the dots, a dot for each page. A page is the slides in view; the last one is the last slides, so
 * that it is as full as the others.
 *
 * ```html
 * <score-carousel [perView]="3" [perViewNarrow]="2" (indexChange)="onPage($event)">
 *   <div>...</div> <div>...</div> <div>...</div> <div>...</div>
 * </score-carousel>
 * ```
 */
@Component({
  selector: 'score-carousel',
  standalone: true,
  imports: [MatButtonModule, FaIconComponent],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './carousel.component.html',
  styleUrl: './carousel.component.css',
  host: {'[style.--per-view]': 'pageSize'}
})
export class CarouselComponent implements OnInit {
  private destroyRef = inject(DestroyRef);

  /** How many slides are in view. */
  @Input() perView = 1;
  /** How many slides are in view below 768px, when it is not `perView`. */
  @Input() perViewNarrow: number | undefined;
  /** The page that is in view changed. */
  @Output() indexChange = new EventEmitter<number>();

  @ViewChild('track', {static: true}) trackRef: ElementRef<HTMLElement>;

  readonly faChevronLeft = faChevronLeft;
  readonly faChevronRight = faChevronRight;

  /** The page in view. */
  index = 0;
  /** The track slides to a page that a dot or an arrow asked for: it does not snap on the way, as that cuts the slide short. */
  sliding = false;
  /** The track is being dragged with the mouse (a touch screen scrolls it by itself). */
  dragging = false;

  private narrow = false;
  private scrollTimer: ReturnType<typeof setTimeout> | undefined;
  private navigatingTimer: ReturnType<typeof setTimeout> | undefined;
  /** The page that a dot or an arrow is taking the track to: the scroll on the way does not choose a page. */
  private navigatingTo: number | undefined;
  private drag: {startX: number, startLeft: number, from: number, moved: boolean} | undefined;

  constructor() {
    const query = typeof window !== 'undefined' ? window.matchMedia?.('(max-width: 767.98px)') : undefined;
    if (query) {
      this.narrow = query.matches;
      const onChange = (event: MediaQueryListEvent) => this.narrow = event.matches;
      query.addEventListener('change', onChange);
      this.destroyRef.onDestroy(() => query.removeEventListener('change', onChange));
    }
  }

  /**
   * When the slides change (they arrive after the page is drawn, or come in another order), the browser keeps the slide that it had
   * snapped to in view, wherever it has gone: the track is taken back to the page in view.
   */
  ngOnInit() {
    if (typeof MutationObserver === 'undefined') {
      return;
    }
    const observer = new MutationObserver(() => {
      if (this.slideCount > 0 && !this.sliding && !this.dragging) {
        this.track.scrollTo?.({left: this.leftOf(this.current), behavior: 'instant'});
      }
    });
    observer.observe(this.track, {childList: true});
    this.destroyRef.onDestroy(() => observer.disconnect());
  }

  get pageSize(): number {
    return Math.max(1, (this.narrow && this.perViewNarrow) ? this.perViewNarrow : this.perView);
  }

  private get track(): HTMLElement {
    return this.trackRef.nativeElement;
  }

  private get slideCount(): number {
    return this.track.children.length;
  }

  get pages(): number {
    return this.slideCount <= this.pageSize ? 1 : Math.ceil(this.slideCount / this.pageSize);
  }

  get pageNumbers(): number[] {
    return Array.from({length: this.pages}, (_, i) => i);
  }

  /** The page in view, which is never past the last one (the slides may have become fewer). */
  get current(): number {
    return Math.min(this.index, this.pages - 1);
  }

  /** The slide that a page begins with. */
  private firstSlideOf(page: number): number {
    return Math.min(page * this.pageSize, Math.max(this.slideCount - this.pageSize, 0));
  }

  /** Where the track is scrolled to when it shows a page. */
  private leftOf(page: number): number {
    const track = this.track;
    const slide = track.children[this.firstSlideOf(page)] as HTMLElement;
    return track.scrollLeft + slide.getBoundingClientRect().left - track.getBoundingClientRect().left;
  }

  /** The page whose beginning is the nearest to where the track is scrolled to. */
  private nearestPage(): number {
    let nearest = 0;
    let distance = Number.POSITIVE_INFINITY;
    for (let page = 0; page < this.pages; page++) {
      const d = Math.abs(this.leftOf(page) - this.track.scrollLeft);
      if (d < distance) {
        distance = d;
        nearest = page;
      }
    }
    return nearest;
  }

  private setIndex(index: number) {
    if (index !== this.index) {
      this.index = index;
      this.indexChange.emit(index);
    }
  }

  /** On a narrow screen the pages are swiped: the page that is in view is the current one. */
  onScroll() {
    const track = this.track;
    if (track.scrollWidth <= track.clientWidth + 1) {
      return;
    }
    if (this.navigatingTo !== undefined) {
      if (Math.abs(track.scrollLeft - this.leftOf(this.navigatingTo)) < 2) {
        this.finishSlide();
      }
      return;
    }
    const nearest = this.nearestPage();
    // The page is chosen once the swipe has settled: choosing it on the way makes what follows the page flicker.
    clearTimeout(this.scrollTimer);
    this.scrollTimer = setTimeout(() => this.setIndex(nearest), 120);
  }

  /**
   * The browser has stopped scrolling. A slide that it stopped short is finished at once, so that the track shows the page that was
   * asked for.
   */
  onScrollEnd() {
    if (this.navigatingTo !== undefined) {
      const left = this.leftOf(this.navigatingTo);
      if (Math.abs(this.track.scrollLeft - left) >= 2) {
        this.track.scrollTo({left, behavior: 'instant'});
      }
    }
    this.finishSlide();
  }

  /** Takes the track to a page, sliding, however far it is. */
  select(page: number) {
    if (page < 0 || page >= this.pages) {
      return;
    }
    this.setIndex(page);
    clearTimeout(this.scrollTimer);
    const left = this.leftOf(page);
    if (Math.abs(left - this.track.scrollLeft) < 1) {
      this.finishSlide();
      return;
    }
    this.navigatingTo = page;
    this.sliding = true;
    clearTimeout(this.navigatingTimer);
    // Only a safety: the slide ends when the page is reached, or when a hand or a wheel takes the track over.
    this.navigatingTimer = setTimeout(() => this.finishSlide(), 5000);
    this.track.scrollTo({left, behavior: 'smooth'});
  }

  /** A hand on the track (a finger, the mouse, a wheel) takes it over from a dot or an arrow that is taking it somewhere. */
  releaseNavigation() {
    this.finishSlide();
  }

  /** The slide to a page is over (it arrived, or it was taken over): the track snaps again, and a swipe chooses a page again. */
  finishSlide() {
    clearTimeout(this.navigatingTimer);
    this.navigatingTo = undefined;
    this.sliding = false;
  }

  onPointerDown(event: PointerEvent) {
    this.releaseNavigation();
    if (event.pointerType !== 'mouse' || event.button !== 0) {
      return;
    }
    this.drag = {startX: event.clientX, startLeft: this.track.scrollLeft, from: this.nearestPage(), moved: false};
  }

  /** The mouse drags the track as a finger does. */
  onPointerMove(event: PointerEvent) {
    const drag = this.drag;
    if (!drag) {
      return;
    }
    const track = this.track;
    const dx = event.clientX - drag.startX;
    if (!drag.moved) {
      if (Math.abs(dx) < 5) {
        return;
      }
      drag.moved = true;
      this.dragging = true;
      track.setPointerCapture?.(event.pointerId);
      // The click that ends a drag is not a click on what is under the mouse (a link, a chip).
      const swallow = (e: Event) => {
        e.stopPropagation();
        e.preventDefault();
      };
      track.addEventListener('click', swallow, {capture: true});
      setTimeout(() => track.removeEventListener('click', swallow, {capture: true}), 400);
    }
    track.scrollLeft = drag.startLeft - dx;
  }

  onPointerUp(event: PointerEvent) {
    const drag = this.drag;
    this.drag = undefined;
    if (!drag?.moved) {
      return;
    }
    const track = this.track;
    track.releasePointerCapture?.(event.pointerId);
    this.dragging = false;
    const dx = event.clientX - drag.startX;
    // A drag of more than a fifth of the track goes to the next page; a shorter one comes back.
    const threshold = track.clientWidth / 5;
    const page = dx <= -threshold ? drag.from + 1 : (dx >= threshold ? drag.from - 1 : drag.from);
    this.select(Math.min(Math.max(page, 0), this.pages - 1));
  }
}
