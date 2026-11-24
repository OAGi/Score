import {Component, signal} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import {CarouselComponent} from './carousel.component';

@Component({
  standalone: true,
  imports: [CarouselComponent],
  template: `<score-carousel [perView]="perView" (indexChange)="changed.push($event)">
    @for (slide of slides(); track slide) {<div>{{ slide }}</div>}
  </score-carousel>`
})
class HostComponent {
  perView = 1;
  slides = signal(['a', 'b', 'c']);
  changed: number[] = [];
}

describe('CarouselComponent', () => {
  const WIDTH = 1000;
  let fixture: ComponentFixture<HostComponent>;
  let component: CarouselComponent;
  let track: HTMLElement;
  let scrollLeft: number;
  let scrollTo: ReturnType<typeof vi.fn>;

  /** A track whose slides are as wide as itself, scrolled by `scrollLeft`. */
  function layOut() {
    scrollLeft = 0;
    scrollTo = vi.fn();
    Object.defineProperty(track, 'scrollLeft', {configurable: true, get: () => scrollLeft, set: (v: number) => scrollLeft = v});
    Object.defineProperty(track, 'scrollWidth', {configurable: true, value: WIDTH * track.children.length});
    Object.defineProperty(track, 'clientWidth', {configurable: true, value: WIDTH});
    track.scrollTo = scrollTo as any;
    track.getBoundingClientRect = () => ({left: 0, width: WIDTH, right: WIDTH} as DOMRect);
    Array.from(track.children).forEach((slide, i) => {
      (slide as HTMLElement).getBoundingClientRect = () => ({left: i * WIDTH - scrollLeft, width: WIDTH, right: (i + 1) * WIDTH - scrollLeft} as DOMRect);
    });
  }

  const mouse = (clientX: number, extra: object = {}) => ({pointerType: 'mouse', button: 0, pointerId: 1, clientX, ...extra}) as any;

  function render(configure: (host: HostComponent) => void = () => {}) {
    fixture = TestBed.createComponent(HostComponent);
    configure(fixture.componentInstance);
    fixture.detectChanges();
    component = fixture.debugElement.query(By.directive(CarouselComponent)).componentInstance;
    track = fixture.nativeElement.querySelector('.track');
    layOut();
  }

  beforeEach(async () => {
    vi.useFakeTimers();
    TestBed.configureTestingModule({imports: [HostComponent]});
    render();
    await Promise.resolve();      // the slides that were there from the start were seen coming
    scrollTo.mockClear();
  });

  afterEach(() => vi.useRealTimers());

  it('has a page, and a dot, for each of the slides when one is in view at a time', () => {
    expect(component.pages).toBe(3);
    expect(fixture.nativeElement.querySelectorAll('.dot').length).toBe(3);
  });

  it('has no buttons and dots when all the slides are in view', () => {
    render(host => host.perView = 3);
    expect(component.pages).toBe(1);
    expect(fixture.nativeElement.querySelector('.nav')).toBeNull();
  });

  it('shows as many slides as it is told and ends with the last ones, so that the last page is as full as the others', () => {
    render(host => {
      host.perView = 3;
      host.slides.set(['a', 'b', 'c', 'd']);
    });
    expect(component.pages).toBe(2);
    component.select(1);
    expect(scrollTo).toHaveBeenCalledWith({left: WIDTH, behavior: 'smooth'});     // the second slide, not the fourth
    expect(component.index).toBe(1);
  });

  it('takes the track back to the page in view when the slides change, as the browser keeps a snapped slide in view where it went', async () => {
    const host = fixture.componentInstance;
    host.slides.set(['x', ...host.slides()]);
    fixture.detectChanges();
    layOut();
    await Promise.resolve();
    await Promise.resolve();
    expect(scrollTo).toHaveBeenCalledWith({left: 0, behavior: 'instant'});
  });

  it('tells the page that it shows', () => {
    component.select(2);
    expect(fixture.componentInstance.changed).toEqual([2]);
  });

  it('slides to a page that is further away than the next one, as it does to the next one', () => {
    component.select(2);
    expect(component.index).toBe(2);
    expect(scrollTo).toHaveBeenCalledWith({left: 2 * WIDTH, behavior: 'smooth'});

    component.select(1);
    expect(scrollTo).toHaveBeenLastCalledWith({left: WIDTH, behavior: 'smooth'});
  });

  it('does not snap the track on the way to a page, and snaps it again once it is there', () => {
    expect(component.sliding).toBe(false);
    component.select(2);
    expect(component.sliding).toBe(true);
    scrollLeft = WIDTH;
    component.onScroll();
    expect(component.sliding).toBe(true);
    scrollLeft = 2 * WIDTH;
    component.onScroll();
    expect(component.sliding).toBe(false);
  });

  it('finishes a slide that the browser stopped short, so that the page that was asked for is shown', () => {
    component.select(2);
    scrollTo.mockClear();
    scrollLeft = 1.7 * WIDTH;           // the scroll ended on the way
    component.onScrollEnd();
    expect(scrollTo).toHaveBeenCalledWith({left: 2 * WIDTH, behavior: 'instant'});
    expect(component.sliding).toBe(false);
  });

  it('leaves the track alone when a scroll that nobody asked for ends', () => {
    scrollLeft = WIDTH;
    component.onScrollEnd();
    expect(scrollTo).not.toHaveBeenCalled();
  });

  it('does not let the scroll on the way choose another card', () => {
    component.select(2);
    scrollLeft = WIDTH;            // on the way, at the second card
    component.onScroll();
    vi.advanceTimersByTime(500);
    expect(component.index).toBe(2);

    scrollLeft = 2 * WIDTH;        // arrived
    component.onScroll();
    vi.advanceTimersByTime(500);
    expect(component.index).toBe(2);
  });

  it('does not take the page back to where it was while the scroll starts slowly', () => {
    component.select(2);
    scrollLeft = 2 * WIDTH;
    component.onScroll();                  // arrived at the third card
    vi.advanceTimersByTime(300);
    expect(component.index).toBe(2);

    component.select(0);                 // back to the first: the scroll is slow to start
    expect(component.index).toBe(0);
    for (let i = 0; i < 4; i++) {
      vi.advanceTimersByTime(250);                 // a pause longer than the settle time of a swipe, again and again
      component.onScroll();                // still at the third card
      vi.advanceTimersByTime(250);
      expect(component.index).toBe(0);
    }
    scrollLeft = 0;
    component.onScroll();                  // arrived
    vi.advanceTimersByTime(500);
    expect(component.index).toBe(0);
  });

  it('does not let what an earlier swipe left to be chosen take the page back after a dot was clicked', () => {
    scrollLeft = 2 * WIDTH;
    component.onScroll();                  // a swipe stops at the third card, to be chosen in 120ms
    component.select(0);                 // a dot is clicked before that
    vi.advanceTimersByTime(500);
    expect(component.index).toBe(0);
  });

  it('lets a swipe choose a page after a dot that was already there', () => {
    component.select(0);                 // the track is at the first card already: nothing to wait for
    scrollLeft = WIDTH;
    component.onScroll();
    vi.advanceTimersByTime(200);
    expect(component.index).toBe(1);
  });

  it('lets a finger take the track over from a dot that is on its way', () => {
    component.select(2);
    component.onPointerDown(mouse(500, {pointerType: 'touch'}));
    scrollLeft = WIDTH;
    component.onScroll();
    vi.advanceTimersByTime(200);
    expect(component.index).toBe(1);
  });

  it('lets a wheel take the track over from a dot that is on its way', () => {
    component.select(2);
    component.releaseNavigation();
    scrollLeft = WIDTH;
    component.onScroll();
    vi.advanceTimersByTime(200);
    expect(component.index).toBe(1);
  });

  it('chooses the page that a swipe stops at, once the swipe has settled', () => {
    scrollLeft = WIDTH;
    component.onScroll();
    expect(component.index).toBe(0);
    vi.advanceTimersByTime(200);
    expect(component.index).toBe(1);
  });

  it('is dragged with the mouse: a drag of more than a fifth of the page goes to the next one', () => {
    component.onPointerDown(mouse(600));
    component.onPointerMove(mouse(400));
    expect(component.dragging).toBe(true);
    expect(scrollLeft).toBe(200);
    component.onPointerUp(mouse(400));
    expect(component.dragging).toBe(false);
    expect(component.index).toBe(1);
    expect(scrollTo).toHaveBeenLastCalledWith({left: WIDTH - scrollLeft + scrollLeft, behavior: 'smooth'});
  });

  it('is dragged with the mouse: a short drag comes back, and a drag the other way goes to the one before', () => {
    component.onPointerDown(mouse(600));
    component.onPointerMove(mouse(500));
    component.onPointerUp(mouse(500));
    expect(component.index).toBe(0);

    scrollLeft = WIDTH;
    component.select(1);
    vi.advanceTimersByTime(2000);
    component.onPointerDown(mouse(100));
    component.onPointerMove(mouse(500));
    component.onPointerUp(mouse(500));
    expect(component.index).toBe(0);
  });

  it('is not dragged by a touch or by a button other than the main one (a touch screen scrolls by itself)', () => {
    component.onPointerDown(mouse(600, {pointerType: 'touch'}));
    component.onPointerMove(mouse(300));
    expect(component.dragging).toBe(false);
    component.onPointerDown(mouse(600, {button: 2}));
    component.onPointerMove(mouse(300));
    expect(component.dragging).toBe(false);
    expect(scrollLeft).toBe(0);
  });

  it('does not open what is under the mouse when a drag ends', () => {
    const opened = vi.fn();
    const chip = document.createElement('a');
    chip.addEventListener('click', opened);
    track.children[0].appendChild(chip);

    component.onPointerDown(mouse(600));
    component.onPointerMove(mouse(300));
    component.onPointerUp(mouse(300));
    chip.dispatchEvent(new MouseEvent('click', {bubbles: true}));
    expect(opened).not.toHaveBeenCalled();

    vi.advanceTimersByTime(500);
    chip.dispatchEvent(new MouseEvent('click', {bubbles: true}));
    expect(opened).toHaveBeenCalledTimes(1);
  });

  it('leaves a click that is not a drag to what is under the mouse', () => {
    const opened = vi.fn();
    const chip = document.createElement('a');
    chip.addEventListener('click', opened);
    track.children[0].appendChild(chip);
    component.onPointerDown(mouse(600));
    component.onPointerMove(mouse(598));
    component.onPointerUp(mouse(598));
    chip.dispatchEvent(new MouseEvent('click', {bubbles: true}));
    expect(opened).toHaveBeenCalledTimes(1);
  });
});
