import {BieReportComponent} from './bie-report.component';

describe('BieReportComponent', () => {
  it('should be defined', () => {
    expect(BieReportComponent).toBeTruthy();
  });

  it('quotes every cell of the CSV so a comma, a quote or a line break keeps its column', () => {
    const component = Object.create(BieReportComponent.prototype) as BieReportComponent;
    component.rows = [{
      releaseNum: '10.12', reusingState: 'WIP', reusingGuid: 'g1', reusingPropertyTerm: 'Term',
      displayPath: 'a > b', reusingOwner: 'owner', reusingVersion: '1,0', reusingStatus: 'say "hi"\nnow',
      reusedState: 'Production', reusedGuid: 'g2', reusedPropertyTerm: 'Reused',
      reusedOwner: 'other', reusedVersion: null, reusedStatus: undefined,
    }] as any;

    const lines = component.convertToCsv().split('\n');

    expect(lines[0]).toBe('"Release","State","Guid","PropertyTerm","Path","Owner","Version","Status",'
      + '"ReusedState","ReusedGuid","ReusedPropertyTerm","ReusedOwner","ReusedVersion","ReusedStatus"');
    expect(lines.slice(1).join('\n')).toBe('"10.12","WIP","g1","Term","a > b","owner","1,0","say ""hi""\nnow",'
      + '"Production","g2","Reused","other","",""');
  });
});
