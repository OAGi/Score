export class PreferencesInfo {
  viewSettingsInfo: ViewSettingsInfo;

  constructor() {
    this.viewSettingsInfo = new ViewSettingsInfo();
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
