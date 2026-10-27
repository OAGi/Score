import {Component} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {faRecycle} from '@fortawesome/free-solid-svg-icons';
import {TreeNodeDecoration, TreeNodeDecorator} from './tree-decorator';
import {TreeNodeDecorationsComponent} from './tree-node-decorations.component';

interface Row {
  name: string;
}

/** A feature that has nothing to do with validation: the tree must draw it all the same. */
function decorator(by: (row: Row) => TreeNodeDecoration[]): TreeNodeDecorator<Row> {
  return {decorationsFor: by};
}

@Component({
  imports: [TreeNodeDecorationsComponent],
  template: `
    <span class="row" (click)="rowClicks = rowClicks + 1">
      <score-tree-decorations [node]="node" [decorators]="decorators"/>
    </span>`
})
class HostComponent {
  node: Row = {name: 'Amount'};
  decorators: TreeNodeDecorator<Row>[] = [];
  rowClicks = 0;
}

describe('TreeNodeDecorationsComponent', () => {
  let fixture: ComponentFixture<HostComponent>;
  let host: HostComponent;
  let root: HTMLElement;

  const render = (decorators: TreeNodeDecorator<Row>[], node: Row = {name: 'Amount'}) => {
    TestBed.configureTestingModule({});
    fixture = TestBed.createComponent(HostComponent);
    host = fixture.componentInstance;
    host.decorators = decorators;
    host.node = node;
    fixture.detectChanges();
    root = fixture.nativeElement as HTMLElement;
  };

  const decorations = () => Array.from(root.querySelectorAll<HTMLElement>('.tree-decoration'));

  it('draws nothing when no feature is attached', () => {
    render([]);

    expect(decorations().length).toBe(0);
  });

  it('draws nothing for a node that the features have nothing to say about', () => {
    render([decorator(() => [])]);

    expect(decorations().length).toBe(0);
  });

  it('draws the icon in the colour the feature gives', () => {
    render([decorator(() => [{icon: 'bolt', color: 'rgb(1, 2, 3)'}])]);

    const icon = decorations()[0].querySelector<HTMLElement>('mat-icon');
    expect(icon.textContent.trim()).toBe('bolt');
    expect(icon.style.color).toBe('rgb(1, 2, 3)');
  });

  it('asks the features about the node it was given', () => {
    const asked: string[] = [];
    render([decorator(row => {
      asked.push(row.name);
      return [];
    })], {name: 'Name'});

    expect(asked).toContain('Name');
  });

  it('draws what every feature shows, in the order of the features and then of their decorations', () => {
    render([
      decorator(() => [{icon: 'a'}, {icon: 'b'}]),
      decorator(() => [{icon: 'c'}])
    ]);

    expect(decorations().map(d => d.querySelector('mat-icon').textContent.trim())).toEqual(['a', 'b', 'c']);
  });

  it('gives a feature its id and class to find its decoration by', () => {
    render([decorator(() => [{icon: 'a', id: 'my-feature', cssClass: 'my-class', onClick: () => undefined}])]);

    const element = root.querySelector('#my-feature');
    expect(element).not.toBeNull();
    expect(element.classList).toContain('my-class');
  });

  it('dims a decoration the feature says is dimmed', () => {
    render([decorator(() => [{icon: 'a', opacity: 0.45}, {icon: 'b'}])]);

    expect(decorations().map(d => d.style.opacity)).toEqual(['0.45', '1']);
  });

  describe('a decoration with a click', () => {
    it('is a button, and a click does what the feature says', () => {
      const clicked = vi.fn();
      render([decorator(() => [{icon: 'a', onClick: clicked}])]);

      expect(decorations()[0].tagName).toBe('BUTTON');
      decorations()[0].click();

      expect(clicked).toHaveBeenCalledTimes(1);
    });

    it('does not select the row it is on', () => {
      render([decorator(() => [{icon: 'a', onClick: () => undefined}])]);

      decorations()[0].click();

      expect(host.rowClicks).toBe(0);
    });
  });

  describe('a decoration without a click', () => {
    it('is only shown: no button, and a click on it is a click on the row', () => {
      render([decorator(() => [{icon: 'a'}])]);

      expect(decorations()[0].tagName).toBe('SPAN');
      decorations()[0].click();

      expect(host.rowClicks).toBe(1);
    });
  });

  describe('a text', () => {
    it('is drawn as it is, with the classes the feature gives', () => {
      render([decorator(() => [{text: '0..3', cssClass: 'node-cardinality ps-2'}])]);

      const text = decorations()[0];
      expect(text.tagName).toBe('SPAN');
      expect(text.textContent?.trim()).toBe('0..3');
      expect(text.classList).toContain('node-cardinality');
      expect(text.querySelector('mat-icon')).toBeNull();
    });

    it('is drawn when it is an empty string', () => {
      render([decorator(() => [{text: ''}])]);

      expect(decorations().length).toBe(1);
    });
  });

  describe('a Font Awesome icon', () => {
    it('is drawn with the classes and the tooltip of the feature, and is not a Material icon', () => {
      render([decorator(() => [{faIcon: faRecycle, tooltip: 'Reused', cssClass: 'node-status-icon ps-3 pointer'}])]);

      const icon = decorations()[0];
      expect(icon.tagName).toBe('FA-ICON');
      expect(icon.classList).toContain('node-status-icon');
      expect(icon.querySelector('svg')).not.toBeNull();
      expect(icon.querySelector('mat-icon')).toBeNull();
    });

    it('runs what the click does, with the click', () => {
      const clicks: MouseEvent[] = [];
      render([decorator(() => [{faIcon: faRecycle, onClick: event => clicks.push(event)}])]);

      decorations()[0].click();

      expect(clicks.length).toBe(1);
      expect(clicks[0].type).toBe('click');
    });

    it('is only shown without a click: a click on it is a click on the row', () => {
      render([decorator(() => [{faIcon: faRecycle}])]);

      decorations()[0].click();

      expect(host.rowClicks).toBe(1);
    });
  });

  describe('the order', () => {
    it('is the order of the decorators, whatever they show', () => {
      render([
        decorator(() => [{text: 'first'}]),
        decorator(() => [{faIcon: faRecycle, cssClass: 'second'}]),
        decorator(() => [{icon: 'bolt', cssClass: 'third'}, {text: 'fourth'}])
      ]);

      const shown = decorations().map(e => e.textContent?.trim() || e.className);
      expect(shown[0]).toBe('first');
      expect(shown[1]).toContain('second');
      expect(shown[2]).toContain('bolt');
      expect(shown[3]).toBe('fourth');
    });

    it('changes with the decorators, not with the feature', () => {
      const a = decorator(() => [{text: 'a'}]);
      const b = decorator(() => [{text: 'b'}]);

      render([b, a]);

      expect(decorations().map(e => e.textContent?.trim())).toEqual(['b', 'a']);
    });
  });
});
