export class PreferencesInfo {
  viewSettingsInfo: ViewSettingsInfo;
  tableColumnsInfo: TableColumnsInfo;

  constructor() {
    this.viewSettingsInfo = new ViewSettingsInfo();
    this.tableColumnsInfo = new TableColumnsInfo();
  }
}

export class ViewSettingsInfo {
  pageSettings: PageSettings;
  treeSettings: TreeSettings;

  constructor() {
    this.pageSettings = new PageSettings();
    this.treeSettings = new TreeSettings();
  }
}

export class PageSettings {
  browserViewMode: boolean = false;
}

export class TreeSettings {
  delimiter: string = '/';
}

export class TableColumnsInfo {
  columnsOfCoreComponentPage: {
    name: string;
    selected: boolean;
    width: number | string;
  }[] = [
    {name: 'Type', selected: true, width: 100},
    {name: 'State', selected: true, width: 100},
    {name: 'DEN', selected: true, width: 0},
    {name: 'Value Domain', selected: true, width: 120},
    {name: 'Six Hexadecimal ID', selected: true, width: 140},
    {name: 'Revision', selected: true, width: 80},
    {name: 'Owner', selected: true, width: 140},
    {name: 'Module', selected: true, width: 400},
    {name: 'Updated On', selected: true, width: 160}
  ];

  filterTypesOfCoreComponentPage: {
    name: string;
    selected: boolean;
  }[] = [
    {name: 'ACC', selected: true},
    {name: 'ASCCP', selected: true},
    {name: 'BCCP', selected: true},
    {name: 'CDT', selected: true},
    {name: 'BDT', selected: true},
    {name: 'ASCC', selected: false},
    {name: 'BCC', selected: false}
  ];

  columnsOfCodeListPage: {
    name: string;
    selected: boolean;
    width: number | string;
  }[] = [
    {name: 'State', selected: true, width: 100},
    {name: 'Name', selected: true, width: 0},
    {name: 'Based Code List', selected: true, width: 200},
    {name: 'Agency ID', selected: true, width: 100},
    {name: 'Version', selected: true, width: 100},
    {name: 'Extensible', selected: true, width: 100},
    {name: 'Revision', selected: true, width: 100},
    {name: 'Owner', selected: true, width: 140},
    {name: 'Module', selected: true, width: 400},
    {name: 'Updated On', selected: true, width: 160}
  ];

  columnsOfAgencyIdListPage: {
    name: string;
    selected: boolean;
    width: number | string;
  }[] = [
    {name: 'State', selected: true, width: 100},
    {name: 'Name', selected: true, width: 0},
    {name: 'Version', selected: true, width: 100},
    {name: 'Revision', selected: true, width: 100},
    {name: 'Owner', selected: true, width: 140},
    {name: 'Module', selected: true, width: 400},
    {name: 'Updated On', selected: true, width: 160}
  ];

  columnsOfReleasePage: {
    name: string;
    selected: boolean;
    width: number | string;
  }[] = [
    {name: 'Release', selected: true, width: 0},
    {name: 'State', selected: true, width: 200},
    {name: 'Created On', selected: true, width: 160},
    {name: 'Updated On', selected: true, width: 160}
  ];

  columnsOfNamespacePage: {
    name: string;
    selected: boolean;
    width: number | string;
  }[] = [
    {name: 'URI', selected: true, width: 0},
    {name: 'Prefix', selected: true, width: 100},
    {name: 'Owner', selected: true, width: 140},
    {name: 'Standard', selected: true, width: 100},
    {name: 'Description', selected: true, width: 400},
    {name: 'Updated On', selected: true, width: 160}
  ];

  columnsOfContextCategoryPage: {
    name: string;
    selected: boolean;
    width: number | string;
  }[] = [
    {name: 'Name', selected: true, width: 0},
    {name: 'Description', selected: true, width: 600},
    {name: 'Updated On', selected: true, width: 160}
  ];

  columnsOfContextSchemePage: {
    name: string;
    selected: boolean;
    width: number | string;
  }[] = [
    {name: 'Name', selected: true, width: 0},
    {name: 'Context Category', selected: true, width: 400},
    {name: 'Scheme ID', selected: true, width: 200},
    {name: 'Agency ID', selected: true, width: 200},
    {name: 'Version', selected: true, width: 200},
    {name: 'Updated On', selected: true, width: 160}
  ];

  columnsOfBusinessContextPage: {
    name: string;
    selected: boolean;
    width: number | string;
  }[] = [
    {name: 'Name', selected: true, width: 0},
    {name: 'Updated On', selected: true, width: 160}
  ];

  columnsOfModuleSetPage: {
    name: string;
    selected: boolean;
    width: number | string;
  }[] = [
    {name: 'Name', selected: true, width: 0},
    {name: 'Description', selected: true, width: '60%'},
    {name: 'Updated On', selected: true, width: 160}
  ];

  columnsOfModuleSetReleasePage: {
    name: string;
    selected: boolean;
    width: number | string;
  }[] = [
    {name: 'Name', selected: true, width: 0},
    {name: 'Release Num', selected: true, width: 300},
    {name: 'Default', selected: true, width: 80},
    {name: 'Updated On', selected: true, width: 160}
  ];

  columnsOfBiePage: {
    name: string;
    selected: boolean;
    width: number | string;
  }[] = [
    {name: 'State', selected: true, width: 100},
    {name: 'Branch', selected: true, width: 100},
    {name: 'DEN', selected: true, width: 0},
    {name: 'Owner', selected: true, width: 140},
    {name: 'Business Contexts', selected: true, width: 140},
    {name: 'Version', selected: true, width: 100},
    {name: 'Status', selected: true, width: 100},
    {name: 'Business Term', selected: true, width: 120},
    {name: 'Remark', selected: true, width: 200},
    {name: 'Updated On', selected: true, width: 160}
  ];

  columnsOfBiePackagePage: {
    name: string;
    selected: boolean;
    width: number | string;
  }[] = [
    {name: 'State', selected: true, width: 100},
    {name: 'Branch', selected: true, width: 100},
    {name: 'Package Version Name', selected: true, width: 0},
    {name: 'Package Version ID', selected: true, width: 200},
    {name: 'Owner', selected: true, width: 140},
    {name: 'Description', selected: true, width: 400},
    {name: 'Updated On', selected: true, width: 160}
  ];

  columnsOfAccountPage: {
    name: string;
    selected: boolean;
    width: number | string;
  }[] = [
    {name: 'Login ID', selected: true, width: 0},
    {name: 'Role', selected: true, width: 200},
    {name: 'Name', selected: true, width: 400},
    {name: 'Organization', selected: true, width: 400},
    {name: 'Status', selected: true, width: 100}
  ];

  columnsOfPendingAccountPage: {
    name: string;
    selected: boolean;
    width: number | string;
  }[] = [
    {name: 'Preferred Username', selected: true, width: 0},
    {name: 'Email', selected: true, width: 400},
    {name: 'Provider', selected: true, width: 400},
    {name: 'Created On', selected: true, width: 160}
  ];

  columnsOfMessagePage: {
    name: string;
    selected: boolean;
    width: number | string;
  }[] = [
    {name: 'Sender', selected: true, width: 0},
    {name: 'Subject', selected: true, width: '70%'},
    {name: 'Created On', selected: true, width: 160}
  ];

  columnsOfTenantPage: {
    name: string;
    selected: boolean;
    width: number | string;
  }[] = [
    {name: 'Tenant Name', selected: true, width: 0},
    {name: 'Users', selected: true, width: 400},
    {name: 'Business Contexts', selected: true, width: 400}
  ];

  columnsOfOpenApiDocumentPage: {
    name: string;
    selected: boolean;
    width: number | string;
  }[] = [
    {name: 'Title', selected: true, width: 0},
    {name: 'OpenAPI Version', selected: true, width: 140},
    {name: 'Version', selected: true, width: 140},
    {name: 'License Name', selected: true, width: 200},
    {name: 'Description', selected: true, width: 400},
    {name: 'Updated On', selected: true, width: 160}
  ];

  columnsOfBusinessTermPage: {
    name: string;
    selected: boolean;
    width: number | string;
  }[] = [
    {name: 'Business Term', selected: true, width: 0},
    {name: 'External Reference URI', selected: true, width: '15%'},
    {name: 'External Reference ID', selected: true, width: '15%'},
    {name: 'Definition', selected: true, width: '40%'},
    {name: 'Updated On', selected: true, width: 160}
  ];

  columnsOfAssignedBusinessTermPage: {
    name: string;
    selected: boolean;
    width: number | string;
  }[] = [
    {name: 'BIE DEN', selected: true, width: 0},
    {name: 'BIE Type', selected: true, width: 100},
    {name: 'Business Term', selected: true, width: 200},
    {name: 'Preferred Business Term', selected: true, width: 200},
    {name: 'External Reference URI', selected: true, width: 200},
    {name: 'External Reference ID', selected: true, width: 200},
    {name: 'Type Code', selected: true, width: 100},
    {name: 'Updated On', selected: true, width: 160}
  ];

  columnsOfLogPage: {
    name: string;
    selected: boolean;
    width: number | string;
  }[] = [
    {name: 'Commit', selected: true, width: 200},
    {name: 'Revision', selected: true, width: 100},
    {name: 'Action', selected: true, width: 0},
    {name: 'Actor', selected: true, width: 140},
    {name: 'Created At', selected: true, width: 160},
  ];
}
