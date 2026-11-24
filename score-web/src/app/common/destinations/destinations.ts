import {ChangeDetectionStrategy, Component, Directive, HostBinding, HostListener, inject, Input, output, ViewChild, ViewContainerRef} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import {MatMenuModule, MatMenuTrigger} from '@angular/material/menu';

/** A page that a number opens: the list of one kind of item, with the filter of the number. */
export interface Destination {
  /** What the menu says, e.g. `Code Lists (2)`. */
  label: string;
  route: string;
  queryParams: Record<string, string>;
}

/** The menu of the pages that a number can open, at the place of the click. */
@Component({
  selector: 'score-destination-menu',
  standalone: true,
  imports: [MatMenuModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.Eager,
  template: `
    <span class="anchor" [style.left.px]="x" [style.top.px]="y" [matMenuTriggerFor]="menu"></span>
    <mat-menu #menu="matMenu" (closed)="closed.emit()">
      @for (destination of destinations; track destination.route) {
        <a mat-menu-item [routerLink]="destination.route" [queryParams]="destination.queryParams">{{ destination.label }}</a>
      }
    </mat-menu>
  `,
  styles: [`.anchor { position: fixed; width: 0; height: 0; }`]
})
export class DestinationMenuComponent {
  @Input() destinations: Destination[] = [];
  x = 0;
  y = 0;
  readonly closed = output<void>();
  @ViewChild(MatMenuTrigger, {static: true}) trigger: MatMenuTrigger;

  openAt(x: number, y: number) {
    this.x = x;
    this.y = y;
  }
}

/**
 * Makes an element open the page of the number that it shows. When the number is of one kind of item it is a link to that page; when it is
 * of several (Core Components, Data Types, Code Lists, ...), a click asks, in a menu, which of them to open.
 */
@Directive({selector: '[scoreDestinations]', standalone: true})
export class DestinationsDirective {
  private router = inject(Router);
  private container = inject(ViewContainerRef);

  @Input('scoreDestinations') destinations: Destination[] = [];

  /** The address of the page, for what has only one, so that it opens in another tab as a link does. */
  @HostBinding('attr.href')
  get href(): string | null {
    const only = this.destinations.length === 1 ? this.destinations[0] : undefined;
    return only ? this.router.serializeUrl(this.router.createUrlTree([only.route], {queryParams: only.queryParams})) : null;
  }

  /** An element that asks has no address to show the pointer of a link for. */
  @HostBinding('style.cursor')
  get cursor(): string | null {
    return this.destinations.length > 1 ? 'pointer' : null;
  }

  @HostListener('click', ['$event'])
  onClick(event: MouseEvent) {
    if (this.destinations.length === 0) {
      return;
    }
    if (this.destinations.length === 1) {
      // A click with a key (a new tab or window) is left to the browser, with the address of the page.
      if (!(event.ctrlKey || event.metaKey || event.shiftKey || event.altKey)) {
        event.preventDefault();
        const only = this.destinations[0];
        this.router.navigate([only.route], {queryParams: only.queryParams});
      }
      return;
    }
    event.preventDefault();
    this.openMenu(event.clientX, event.clientY);
  }

  @HostListener('keydown.enter', ['$event'])
  onEnter(event: Event) {
    if (this.destinations.length > 1) {
      event.preventDefault();
      const box = (event.target as HTMLElement).getBoundingClientRect();
      this.openMenu(box.left, box.bottom);
    }
  }

  private openMenu(x: number, y: number) {
    const menu = this.container.createComponent(DestinationMenuComponent);
    menu.setInput('destinations', this.destinations);
    menu.instance.openAt(x, y);
    menu.instance.closed.subscribe(() => setTimeout(() => menu.destroy()));
    menu.changeDetectorRef.detectChanges();
    menu.instance.trigger.openMenu();
  }
}
