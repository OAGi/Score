import {Subject} from 'rxjs';
import {CodeListDetailComponent} from './code-list-detail.component';

/** The WebSocket topic of a detail page is dropped when the page is left, and not stacked when it is loaded again. */
describe('CodeListDetailComponent WebSocket topic', () => {
  const create = () => {
    const topics: Subject<unknown>[] = [];
    const self = {
      manifestId: 7,
      stompService: {
        watch: vi.fn(() => {
          const topic = new Subject<unknown>();
          topics.push(topic);
          return topic;
        })
      },
      eventSubscription: undefined as { closed: boolean } | undefined,
    };
    return {self, topics};
  };

  it('replaces the previous subscription when it subscribes again', () => {
    const {self, topics} = create();
    CodeListDetailComponent.prototype.subscribeEvent.call(self);
    CodeListDetailComponent.prototype.subscribeEvent.call(self);

    expect(self.stompService.watch).toHaveBeenCalledWith('/topic/code_list/7');
    expect(topics.map(t => t.observed)).toEqual([false, true]);
  });

  it('unsubscribes when the component is destroyed', () => {
    const {self, topics} = create();
    CodeListDetailComponent.prototype.subscribeEvent.call(self);
    CodeListDetailComponent.prototype.ngOnDestroy.call(self);

    expect(topics[0].observed).toBe(false);
  });
});
