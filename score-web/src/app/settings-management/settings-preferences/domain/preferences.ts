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

export interface TableColumnsProperty {
  name: string;
  selected: boolean;
  width: number | string;
}

export class TableColumnsInfo {
  columnsOfCoreComponentPage: TableColumnsProperty[] = [
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

  columnsOfCodeListPage: TableColumnsProperty[] = [
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

  columnsOfAgencyIdListPage: TableColumnsProperty[] = [
    {name: 'State', selected: true, width: 100},
    {name: 'Name', selected: true, width: 0},
    {name: 'Version', selected: true, width: 100},
    {name: 'Revision', selected: true, width: 100},
    {name: 'Owner', selected: true, width: 140},
    {name: 'Module', selected: true, width: 400},
    {name: 'Updated On', selected: true, width: 160}
  ];

  columnsOfReleasePage: TableColumnsProperty[] = [
    {name: 'Release', selected: true, width: 0},
    {name: 'State', selected: true, width: 200},
    {name: 'Created On', selected: true, width: 160},
    {name: 'Updated On', selected: true, width: 160}
  ];

  columnsOfNamespacePage: TableColumnsProperty[] = [
    {name: 'URI', selected: true, width: 0},
    {name: 'Prefix', selected: true, width: 100},
    {name: 'Owner', selected: true, width: 140},
    {name: 'Standard', selected: true, width: 100},
    {name: 'Description', selected: true, width: 400},
    {name: 'Updated On', selected: true, width: 160}
  ];

  columnsOfContextCategoryPage: TableColumnsProperty[] = [
    {name: 'Name', selected: true, width: 0},
    {name: 'Description', selected: true, width: 600},
    {name: 'Updated On', selected: true, width: 160}
  ];

  columnsOfContextSchemePage: TableColumnsProperty[] = [
    {name: 'Name', selected: true, width: 0},
    {name: 'Context Category', selected: true, width: 400},
    {name: 'Scheme ID', selected: true, width: 200},
    {name: 'Agency ID', selected: true, width: 200},
    {name: 'Version', selected: true, width: 200},
    {name: 'Updated On', selected: true, width: 160}
  ];

  columnsOfBusinessContextPage: TableColumnsProperty[] = [
    {name: 'Name', selected: true, width: 0},
    {name: 'Updated On', selected: true, width: 160}
  ];

  columnsOfModuleSetPage: TableColumnsProperty[] = [
    {name: 'Name', selected: true, width: 0},
    {name: 'Description', selected: true, width: '60%'},
    {name: 'Updated On', selected: true, width: 160}
  ];

  columnsOfModuleSetReleasePage: TableColumnsProperty[] = [
    {name: 'Name', selected: true, width: 0},
    {name: 'Release Num', selected: true, width: 300},
    {name: 'Default', selected: true, width: 80},
    {name: 'Updated On', selected: true, width: 160}
  ];

  columnsOfBiePage: TableColumnsProperty[] = [
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

  columnsOfBieReuseReportPage: TableColumnsProperty[] = [
    {name: 'Release', selected: true, width: 100},
    {name: 'Reusing State', selected: true, width: 100},
    {name: 'Reusing Property Term', selected: true, width: 0},
    {name: 'Reusing Owner', selected: true, width: 140},
    {name: 'Reusing Version', selected: true, width: '5%'},
    {name: 'Reusing Status', selected: true, width: '5%'},
    {name: 'Reused State', selected: true, width: 100},
    {name: 'Reused Property Term', selected: true, width: 0},
    {name: 'Reused Owner', selected: true, width: 140},
    {name: 'Reused Version', selected: true, width: '5%'},
    {name: 'Reused Status', selected: true, width: '5%'}
  ];

  columnsOfBiePackagePage: TableColumnsProperty[] = [
    {name: 'State', selected: true, width: 100},
    {name: 'Branch', selected: true, width: 100},
    {name: 'Package Version Name', selected: true, width: 0},
    {name: 'Package Version ID', selected: true, width: 200},
    {name: 'Owner', selected: true, width: 140},
    {name: 'Description', selected: true, width: 400},
    {name: 'Updated On', selected: true, width: 160}
  ];

  columnsOfAccountPage: TableColumnsProperty[] = [
    {name: 'Login ID', selected: true, width: 0},
    {name: 'Role', selected: true, width: 200},
    {name: 'Name', selected: true, width: 400},
    {name: 'Organization', selected: true, width: 400},
    {name: 'Status', selected: true, width: 100}
  ];

  columnsOfPendingAccountPage: TableColumnsProperty[] = [
    {name: 'Preferred Username', selected: true, width: 0},
    {name: 'Email', selected: true, width: 400},
    {name: 'Provider', selected: true, width: 400},
    {name: 'Created On', selected: true, width: 160}
  ];

  columnsOfMessagePage: TableColumnsProperty[] = [
    {name: 'Sender', selected: true, width: 0},
    {name: 'Subject', selected: true, width: '70%'},
    {name: 'Created On', selected: true, width: 160}
  ];

  columnsOfTenantPage: TableColumnsProperty[] = [
    {name: 'Tenant Name', selected: true, width: 0},
    {name: 'Users', selected: true, width: 400},
    {name: 'Business Contexts', selected: true, width: 400}
  ];

  columnsOfOpenApiDocumentPage: TableColumnsProperty[] = [
    {name: 'Title', selected: true, width: 0},
    {name: 'OpenAPI Version', selected: true, width: 140},
    {name: 'Version', selected: true, width: 140},
    {name: 'License Name', selected: true, width: 200},
    {name: 'Description', selected: true, width: 400},
    {name: 'Updated On', selected: true, width: 160}
  ];

  columnsOfBieForOasDocPage: TableColumnsProperty[] = [
    {name: 'Branch', selected: true, width: 80},
    {name: 'DEN', selected: true, width: 0},
    {name: 'Remark', selected: true, width: '12%'},
    {name: 'Verb', selected: true, width: 80},
    {name: 'Array Indicator', selected: true, width: 120},
    {name: 'Suppress Root Indicator', selected: true, width: 120},
    {name: 'Message Body', selected: true, width: 120},
    {name: 'Resource Name', selected: true, width: '15%'},
    {name: 'Operation ID', selected: true, width: '15%'},
    {name: 'Tag Name', selected: true, width: '10%'}
  ];

  columnsOfBusinessTermPage: TableColumnsProperty[] = [
    {name: 'Business Term', selected: true, width: 0},
    {name: 'External Reference URI', selected: true, width: '15%'},
    {name: 'External Reference ID', selected: true, width: '15%'},
    {name: 'Definition', selected: true, width: '40%'},
    {name: 'Updated On', selected: true, width: 160}
  ];

  columnsOfAssignedBusinessTermPage: TableColumnsProperty[] = [
    {name: 'BIE DEN', selected: true, width: 0},
    {name: 'BIE Type', selected: true, width: 100},
    {name: 'Business Term', selected: true, width: 200},
    {name: 'Preferred Business Term', selected: true, width: 200},
    {name: 'External Reference URI', selected: true, width: 200},
    {name: 'External Reference ID', selected: true, width: 200},
    {name: 'Type Code', selected: true, width: 100},
    {name: 'Updated On', selected: true, width: 160}
  ];

  columnsOfLogPage: TableColumnsProperty[] = [
    {name: 'Commit', selected: true, width: 200},
    {name: 'Revision', selected: true, width: 100},
    {name: 'Action', selected: true, width: 0},
    {name: 'Actor', selected: true, width: 140},
    {name: 'Created At', selected: true, width: 160},
  ];
}
