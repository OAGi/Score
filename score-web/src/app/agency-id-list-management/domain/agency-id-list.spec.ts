import {withDefaultFlags} from './agency-id-list';

describe('withDefaultFlags', () => {
  it('reads the flags the server returns as isDeveloperDefault and isUserDefault', () => {
    const value = withDefaultFlags({guid: 'g', value: 'v', isDeveloperDefault: true, isUserDefault: false});

    expect(value.developerDefault).toBe(true);
    expect(value.userDefault).toBe(false);
    expect((value as any).isDeveloperDefault).toBeUndefined();
    expect((value as any).isUserDefault).toBeUndefined();
    expect(value.guid).toBe('g');
  });

  it('keeps the flags of a value that already uses the web names', () => {
    const value = withDefaultFlags({developerDefault: false, userDefault: true});

    expect(value.developerDefault).toBe(false);
    expect(value.userDefault).toBe(true);
  });
});
