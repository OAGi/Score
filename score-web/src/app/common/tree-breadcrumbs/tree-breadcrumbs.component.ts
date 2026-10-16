import {AfterViewChecked, Component, ElementRef, EventEmitter, Input, Output, ViewChild} from '@angular/core';

export interface TreeBreadcrumbNode {
  name: string;
  displayName?: string;
}

/** Path from the root to the selected tree node; keeps the last (current) item visible when the path overflows. */
@Component({
  selector: 'score-tree-breadcrumbs',
  templateUrl: './tree-breadcrumbs.component.html',
  host: {style: 'display: contents'}
})
export class TreeBreadcrumbsComponent<N extends TreeBreadcrumbNode = TreeBreadcrumbNode> implements AfterViewChecked {

  @Input() nodes: N[] | null | undefined;
  @Output() nodeClick = new EventEmitter<N>();

  @ViewChild('wrap') wrap: ElementRef<HTMLElement> | undefined;

  ngAfterViewChecked(): void {
    const breadcrumbs = this.wrap?.nativeElement;
    if (!breadcrumbs) {
      return;
    }
    if (breadcrumbs.scrollWidth > breadcrumbs.clientWidth) {
      breadcrumbs.scrollLeft = breadcrumbs.scrollWidth - breadcrumbs.clientWidth;
      breadcrumbs.classList.add('inner-box');
    } else {
      breadcrumbs.scrollLeft = 0;
      breadcrumbs.classList.remove('inner-box');
    }
  }
}
