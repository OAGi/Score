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

  columnsOfCodeListPage: {
    name: string;
    selected: boolean;
  }[] = [
    {name: 'State', selected: true},
    {name: 'Name', selected: true},
    {name: 'Based Code List', selected: true},
    {name: 'Agency ID', selected: true},
    {name: 'Version', selected: true},
    {name: 'Extensible', selected: true},
    {name: 'Revision', selected: true},
    {name: 'Owner', selected: true},
    {name: 'Module', selected: true},
    {name: 'Updated On', selected: true}
  ];

  columnsOfAgencyIdListPage: {
    name: string;
    selected: boolean;
  }[] = [
    {name: 'State', selected: true},
    {name: 'Name', selected: true},
    {name: 'Version', selected: true},
    {name: 'Revision', selected: true},
    {name: 'Owner', selected: true},
    {name: 'Module', selected: true},
    {name: 'Updated On', selected: true}
  ];

  columnsOfReleasePage: {
    name: string;
    selected: boolean;
  }[] = [
    {name: 'Release', selected: true},
    {name: 'State', selected: true},
    {name: 'Created On', selected: true},
    {name: 'Updated On', selected: true}
  ];

  columnsOfNamespacePage: {
    name: string;
    selected: boolean;
  }[] = [
    {name: 'URI', selected: true},
    {name: 'Prefix', selected: true},
    {name: 'Owner', selected: true},
    {name: 'Standard', selected: true},
    {name: 'Description', selected: true},
    {name: 'Updated On', selected: true}
  ];

  columnsOfContextCategoryPage: {
    name: string;
    selected: boolean;
  }[] = [
    {name: 'Name', selected: true},
    {name: 'Description', selected: true},
    {name: 'Updated On', selected: true}
  ];

  columnsOfContextSchemePage: {
    name: string;
    selected: boolean;
  }[] = [
    {name: 'Name', selected: true},
    {name: 'Context Category', selected: true},
    {name: 'Scheme ID', selected: true},
    {name: 'Agency ID', selected: true},
    {name: 'Version', selected: true},
    {name: 'Updated On', selected: true}
  ];

  columnsOfBusinessContextPage: {
    name: string;
    selected: boolean;
  }[] = [
    {name: 'Name', selected: true},
    {name: 'Updated On', selected: true}
  ];

  columnsOfModuleSetPage: {
    name: string;
    selected: boolean;
  }[] = [
    {name: 'Name', selected: true},
    {name: 'Description', selected: true},
    {name: 'Updated On', selected: true}
  ];

  columnsOfModuleSetReleasePage: {
    name: string;
    selected: boolean;
  }[] = [
    {name: 'Name', selected: true},
    {name: 'Release Num', selected: true},
    {name: 'Default', selected: true},
    {name: 'Updated On', selected: true}
  ];

  columnsOfBiePage: {
    name: string;
    selected: boolean;
  }[] = [
    {name: 'State', selected: true},
    {name: 'Branch', selected: true},
    {name: 'DEN', selected: true},
    {name: 'Owner', selected: true},
    {name: 'Business Contexts', selected: true},
    {name: 'Version', selected: true},
    {name: 'Status', selected: true},
    {name: 'Business Term', selected: true},
    {name: 'Remark', selected: true},
    {name: 'Updated On', selected: true}
  ];

  columnsOfBiePackagePage: {
    name: string;
    selected: boolean;
  }[] = [
    {name: 'State', selected: true},
    {name: 'Branch', selected: true},
    {name: 'Package Version Name', selected: true},
    {name: 'Package Version ID', selected: true},
    {name: 'Owner', selected: true},
    {name: 'Description', selected: true},
    {name: 'Updated On', selected: true}
  ];

  columnsOfAccountPage: {
    name: string;
    selected: boolean;
  }[] = [
    {name: 'Login ID', selected: true},
    {name: 'Role', selected: true},
    {name: 'Name', selected: true},
    {name: 'Organization', selected: true},
    {name: 'Status', selected: true}
  ];

  columnsOfPendingAccountPage: {
    name: string;
    selected: boolean;
  }[] = [
    {name: 'Preferred Username', selected: true},
    {name: 'Email', selected: true},
    {name: 'Provider', selected: true},
    {name: 'Created On', selected: true}
  ];

  columnsOfMessagePage: {
    name: string;
    selected: boolean;
  }[] = [
    {name: 'Sender', selected: true},
    {name: 'Subject', selected: true},
    {name: 'Created On', selected: true}
  ];

  columnsOfTenantPage: {
    name: string;
    selected: boolean;
  }[] = [
    {name: 'Tenant Name', selected: true},
    {name: 'Users', selected: true},
    {name: 'Business Contexts', selected: true}
  ];

  columnsOfOpenApiDocumentPage: {
    name: string;
    selected: boolean;
  }[] = [
    {name: 'Title', selected: true},
    {name: 'OpenAPI Version', selected: true},
    {name: 'Version', selected: true},
    {name: 'License Name', selected: true},
    {name: 'Description', selected: true},
    {name: 'Updated On', selected: true}
  ];

  columnsOfBusinessTermPage: {
    name: string;
    selected: boolean;
  }[] = [
    {name: 'Business Term', selected: true},
    {name: 'External Reference URI', selected: true},
    {name: 'External Reference ID', selected: true},
    {name: 'Definition', selected: true},
    {name: 'Updated On', selected: true}
  ];

  columnsOfAssignedBusinessTermPage: {
    name: string;
    selected: boolean;
  }[] = [
    {name: 'BIE DEN', selected: true},
    {name: 'BIE Type', selected: true},
    {name: 'Business Term', selected: true},
    {name: 'Preferred Business Term', selected: true},
    {name: 'External Reference URI', selected: true},
    {name: 'External Reference ID', selected: true},
    {name: 'Type Code', selected: true},
    {name: 'Updated On', selected: true}
  ];
}

export class TreeSettingsInfo {
  delimiter: string;
}
