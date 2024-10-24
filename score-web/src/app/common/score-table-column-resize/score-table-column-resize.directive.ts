import {Directive, ElementRef, EventEmitter, HostListener, Input, OnChanges, OnInit, Output, Renderer2, SimpleChanges} from '@angular/core';

@Directive({
  selector: '[score-table-column-resize]'
})
export class ScoreTableColumnResizeDirective implements OnInit, OnChanges {
  private startX: number;
  private startWidth: number;
  private resizeHandle: HTMLElement;
  private innerLine: HTMLElement;
  private _resizing = false;
  private _title: string;
  private _width: number | string;

  @Input() defaultWidth: number | string;
  @Output() onResize: EventEmitter<{ name: string; width: number | string }> = new EventEmitter();

  constructor(private el: ElementRef, private renderer: Renderer2) {
  }

  ngOnInit() {
    this.setInitialWidth();
    this.createResizeHandle();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.defaultWidth && changes.defaultWidth.currentValue !== undefined) {
      this.setInitialWidth();
    }
  }

  get resizing(): boolean {
    return this._resizing;
  }

  get title(): string {
    return this._title;
  }

  get width(): number | string {
    return this._width;
  }

  setInitialWidth() {
    if (this.defaultWidth) {
      this._width = this.defaultWidth;
      if (typeof (this._width) === 'string') {
        this.renderer.setStyle(this.el.nativeElement, 'width', `${this._width}`);
      } else {
        this.renderer.setStyle(this.el.nativeElement, 'width', `${this._width}px`);
      }
    } else {
      this.renderer.removeStyle(this.el.nativeElement, 'width');
    }
  }

  createResizeHandle() {
    // Create the visual resize handle
    this.resizeHandle = this.renderer.createElement('span');
    this.renderer.addClass(this.resizeHandle, 'resize-handle');

    // Find the child <div> element inside the <th>
    const divChild = this.el.nativeElement.querySelector('div');
    if (divChild) {
      this._title = divChild.textContent?.trim();
      this.renderer.appendChild(divChild, this.resizeHandle);
    } else {
      this._title = this.el.nativeElement.textContent?.trim();
      this.renderer.appendChild(this.el.nativeElement, this.resizeHandle);
    }

    // Style the resize handle for positioning and interaction
    this.renderer.setStyle(this.resizeHandle, 'position', 'absolute');
    this.renderer.setStyle(this.resizeHandle, 'top', '0');
    this.renderer.setStyle(this.resizeHandle, 'right', '0');
    this.renderer.setStyle(this.resizeHandle, 'width', '22px');
    this.renderer.setStyle(this.resizeHandle, 'height', '100%');
    this.renderer.setStyle(this.resizeHandle, 'cursor', 'col-resize');
    this.renderer.setStyle(this.resizeHandle, 'background-color', 'transparent');

    this.innerLine = this.renderer.createElement('span');
    this.renderer.setStyle(this.innerLine, 'position', 'absolute');
    this.renderer.setStyle(this.innerLine, 'top', '0');
    this.renderer.setStyle(this.innerLine, 'right', '50%');
    this.renderer.setStyle(this.innerLine, 'transform', 'translateX(50%)');
    this.renderer.setStyle(this.innerLine, 'width', '1px');
    this.renderer.setStyle(this.innerLine, 'height', '100%');
    this.renderer.setStyle(this.innerLine, 'background-color', '#ccc');

    this.renderer.appendChild(this.resizeHandle, this.innerLine);

    // Add dynamic matTooltip to the resize handle
    const tooltip = this.renderer.createElement('span');
    this.renderer.setAttribute(this.resizeHandle, 'matTooltip', `${this._width}`);
    this.renderer.setAttribute(this.resizeHandle, 'matTooltipPosition', 'above');

    this.renderer.listen(this.el.nativeElement, 'mouseenter', () => {
      this.renderer.setStyle(this.innerLine, 'background-color', '#999');
    });
    this.renderer.listen(this.el.nativeElement, 'mouseleave', () => {
      this.renderer.setStyle(this.innerLine, 'background-color', '#ccc');
    });

    this.renderer.listen(this.resizeHandle, 'mousedown', (event: MouseEvent) => {
      event.stopPropagation();
      event.preventDefault();

      this.onResizeStart(event);
    });
  }

  @HostListener('mousedown', ['$event'])
  onResizeStart(event: MouseEvent) {
    if (event.target === this.resizeHandle || event.target === this.innerLine) {
      this._resizing = true;  // Set resizing flag
      this.startX = event.pageX;
      this.startWidth = this.el.nativeElement.offsetWidth;

      document.addEventListener('mousemove', this.onMouseMove);
      document.addEventListener('mouseup', this.onMouseUp);
    }
  }

  onMouseMove = (event: MouseEvent) => {
    this._width = this.startWidth + (event.pageX - this.startX);
    if (typeof (this._width) === 'string') {
      this.renderer.setStyle(this.el.nativeElement, 'width', `${this._width}`);
    } else {
      this.renderer.setStyle(this.el.nativeElement, 'width', `${this._width}px`);
    }
  };

  onMouseUp = (event: MouseEvent) => {
    document.removeEventListener('mousemove', this.onMouseMove);
    document.removeEventListener('mouseup', this.onMouseUp);
    this.onResize.emit({
      name: this._title,
      width: this._width
    });

    // Set a small timeout to differentiate click from drag
    setTimeout(() => {
      this._resizing = false;  // Reset resizing flag
    }, 0);
  };
}
