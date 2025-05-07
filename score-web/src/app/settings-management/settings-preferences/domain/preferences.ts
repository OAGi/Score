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

  columnsOfDataTypePage: TableColumnsProperty[] = [
    {name: 'State', selected: true, width: 104},
    {name: 'DEN', selected: true, width: 0},
    {name: 'Value Domain', selected: true, width: 126},
    {name: 'Six Hexadecimal ID', selected: true, width: 140},
    {name: 'Revision', selected: true, width: 80},
    {name: 'Owner', selected: true, width: 140},
    {name: 'Module', selected: true, width: 400},
    {name: 'Updated On', selected: true, width: 160}
  ];

  columnsOfDataTypeWithoutTypeColumnPage: TableColumnsProperty[] = [
    {name: 'State', selected: true, width: 104},
    {name: 'DEN', selected: true, width: 0},
    {name: 'Value Domain', selected: true, width: 126},
    {name: 'Six Hexadecimal ID', selected: true, width: 140},
    {name: 'Revision', selected: true, width: 80},
    {name: 'Owner', selected: true, width: 140},
    {name: 'Module', selected: true, width: 400},
    {name: 'Updated On', selected: true, width: 160}
  ];

  columnsOfCoreComponentWithoutTypeColumnPage: TableColumnsProperty[] = [
    {name: 'State', selected: true, width: 104},
    {name: 'DEN', selected: true, width: 0},
    {name: 'Revision', selected: true, width: 80},
    {name: 'Owner', selected: true, width: 140},
    {name: 'Module', selected: true, width: 400},
    {name: 'Updated On', selected: true, width: 160}
  ];

  columnsOfCoreComponentPage: TableColumnsProperty[] = [
    {name: 'Type', selected: true, width: 80},
    {name: 'State', selected: true, width: 104},
    {name: 'DEN', selected: true, width: 0},
    {name: 'Revision', selected: true, width: 80},
    {name: 'Owner', selected: true, width: 140},
    {name: 'Module', selected: true, width: 400},
    {name: 'Updated On', selected: true, width: 160}
  ];

  columnsOfCoreComponentForVerbBODPage: TableColumnsProperty[] = [
    {name: 'State', selected: true, width: 104},
    {name: 'DEN', selected: true, width: 0},
    {name: 'Updated On', selected: true, width: 160}
  ];

  columnsOfCoreComponentForNounBODPage: TableColumnsProperty[] = [
    {name: 'State', selected: true, width: 104},
    {name: 'DEN', selected: true, width: 0},
    {name: 'Updated On', selected: true, width: 160}
  ];

  filterTypesOfCoreComponentPage: {
    name: string;
    selected: boolean;
  }[] = [
    {name: 'ACC', selected: true},
    {name: 'ASCCP', selected: true},
    {name: 'BCCP', selected: true},
    // {name: 'ASCC', selected: false},
    // {name: 'BCC', selected: false}
  ];

  filterTypesOfDataTypePage: {
    name: string;
    selected: boolean;
  }[] = [
    {name: 'CDT', selected: true},
    {name: 'BDT', selected: true},
  ];

  columnsOfCoreComponentAccRefactorPage: TableColumnsProperty[] = [
    {name: 'Type', selected: true, width: 80},
    {name: 'State', selected: true, width: 104},
    {name: 'DEN', selected: true, width: 0},
    {name: 'Issue', selected: true, width: '30%'},
    {name: 'Revision', selected: true, width: 80},
    {name: 'Owner', selected: true, width: 140},
    {name: 'Updated On', selected: true, width: 160}
  ];

  columnsOfCodeListPage: TableColumnsProperty[] = [
    {name: 'State', selected: true, width: 104},
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

  columnsOfCodeListValuePage: TableColumnsProperty[] = [
    {name: 'Value', selected: true, width: 0},
    {name: 'Meaning', selected: true, width: '10%'},
    {name: 'Deprecated', selected: true, width: 100},
    {name: 'Definition', selected: true, width: '50%'},
    {name: 'Definition Source', selected: true, width: '15%'}
  ];

  columnsOfAgencyIdListPage: TableColumnsProperty[] = [
    {name: 'State', selected: true, width: 104},
    {name: 'Name', selected: true, width: 0},
    {name: 'Version', selected: true, width: 100},
    {name: 'Revision', selected: true, width: 100},
    {name: 'Owner', selected: true, width: 140},
    {name: 'Module', selected: true, width: 400},
    {name: 'Updated On', selected: true, width: 160}
  ];

  columnsOfAgencyIdListValuePage: TableColumnsProperty[] = [
    {name: 'Value', selected: true, width: 0},
    {name: 'Meaning', selected: true, width: '10%'},
    {name: 'Deprecated', selected: true, width: 100},
    {name: 'Definition', selected: true, width: '50%'},
    {name: 'Definition Source', selected: true, width: '15%'}
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
    {name: 'Description', selected: true, width: '60%'},
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

  columnsOfContextSchemeValuePage: TableColumnsProperty[] = [
    {name: 'Value', selected: true, width: 0},
    {name: 'Meaning', selected: true, width: '50%'}
  ];

  columnsOfBusinessContextPage: TableColumnsProperty[] = [
    {name: 'Name', selected: true, width: 0},
    {name: 'Updated On', selected: true, width: 160}
  ];

  columnsOfBusinessContextWithTenantPage: TableColumnsProperty[] = [
    {name: 'Name', selected: true, width: 0},
    {name: 'Tenant', selected: true, width: 400},
    {name: 'Updated On', selected: true, width: 160}
  ];

  columnsOfBusinessContextValuePage: TableColumnsProperty[] = [
    {name: 'Context Category', selected: true, width: 0},
    {name: 'Context Scheme', selected: true, width: '33%'},
    {name: 'Context Scheme Value', selected: true, width: '33%'}
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

  columnsOfLibraryPage: TableColumnsProperty[] = [
    {name: 'Name', selected: true, width: 0},
    {name: 'Type', selected: true, width: 140},
    {name: 'Organization', selected: true, width: 400},
    {name: 'Domain', selected: true, width: 200},
    {name: 'Description', selected: true, width: '30%'},
    {name: 'Updated On', selected: true, width: 160}
  ];

  columnsOfBiePage: TableColumnsProperty[] = [
    {name: 'State', selected: true, width: 104},
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
    {name: 'Reusing DEN', selected: true, width: 0},
    {name: 'Reusing Owner', selected: true, width: 140},
    {name: 'Reusing Version', selected: true, width: '5%'},
    {name: 'Reusing Status', selected: true, width: '5%'},
    {name: 'Reusing Remark', selected: false, width: '5%'},
    {name: 'Reused State', selected: true, width: 100},
    {name: 'Reused DEN', selected: true, width: 0},
    {name: 'Reused Owner', selected: true, width: 140},
    {name: 'Reused Version', selected: true, width: '5%'},
    {name: 'Reused Status', selected: true, width: '5%'},
    {name: 'Reused Remark', selected: false, width: '5%'}
  ];

  columnsOfBieUpliftReportPage: TableColumnsProperty[] = [
    {name: 'Type', selected: true, width: 80},
    {name: 'Path', selected: true, width: 0},
    {name: 'Context Definition', selected: true, width: '40%'},
    {name: 'Matched', selected: true, width: '5%'},
    {name: 'Reused', selected: true, width: '5%'},
    {name: 'Issue', selected: true, width: '10%'},
  ];

  columnsOfBiePackagePage: TableColumnsProperty[] = [
    {name: 'State', selected: true, width: 104},
    {name: 'Branch', selected: true, width: 100},
    {name: 'Package Version Name', selected: true, width: 0},
    {name: 'Package Version ID', selected: true, width: 200},
    {name: 'Owner', selected: true, width: 140},
    {name: 'Description', selected: true, width: '40%'},
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
    {name: 'Subject', selected: true, width: 0},
    {name: 'Created On', selected: true, width: 160}
  ];

  columnsOfTenantPage: TableColumnsProperty[] = [
    {name: 'Tenant Name', selected: true, width: 0},
    {name: 'Users', selected: true, width: 400},
    {name: 'Business Contexts', selected: true, width: 400}
  ];

  columnsOfTenantManagementForAccountPage: TableColumnsProperty[] = [
    {name: 'Login ID', selected: true, width: 0},
    {name: 'Role', selected: true, width: 200},
    {name: 'Name', selected: true, width: 400},
    {name: 'Organization', selected: true, width: 400},
    {name: 'Status', selected: true, width: 100},
    {name: 'Manage', selected: true, width: 120}
  ];

  columnsOfTenantManagementForBusinessContextPage: TableColumnsProperty[] = [
    {name: 'Name', selected: true, width: 0},
    {name: 'Updated On', selected: true, width: 160},
    {name: 'Manage', selected: true, width: 120}
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
