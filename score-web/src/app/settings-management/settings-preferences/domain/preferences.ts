export class PreferencesInfo {
  tableColumnsInfo: TableColumnsInfo;
  treeSettingsInfo: TreeSettingsInfo;
}

export class TableColumnsInfo {
  columnsOfCoreComponentPage: {
    name: string;
    selected: boolean;
  }[] = [
    {name: 'Type', selected: true},
    {name: 'State', selected: true},
    {name: 'DEN', selected: true},
    {name: 'Value Domain', selected: true},
    {name: 'Six Hexadecimal ID', selected: true},
    {name: 'Revision', selected: true},
    {name: 'Owner', selected: true},
    {name: 'Module', selected: true},
    {name: 'Updated On', selected: true}
  ];
}

export class TreeSettingsInfo {
  delimiter: string;
}
