export class ProductInfo {
  productName: string;
  productVersion: string;
}

export class WebPageInfo {
  brand: string;
  favicon: string;
  signInStatement: string;
  componentStateColorSetMap: Map<string, BoxColorSet>;
  releaseStateColorSetMap: Map<string, BoxColorSet>;
  userRoleColorSetMap: Map<string, BoxColorSet>;

  constructor(obj?: WebPageInfo) {
    this.brand = (!!obj) ? obj.brand : '';
    this.favicon = (!!obj) ? obj.favicon : '';
    this.signInStatement = (!!obj) ? obj.signInStatement : '';
    this.componentStateColorSetMap = (!!obj) ? obj.componentStateColorSetMap : new Map();
    this.releaseStateColorSetMap = (!!obj) ? obj.releaseStateColorSetMap : new Map();
    this.userRoleColorSetMap = (!!obj) ? obj.userRoleColorSetMap : new Map();
  }

  getComponentStateColorSet(state: string): BoxColorSet | undefined {
    if (!!this.componentStateColorSetMap &&
      this.componentStateColorSetMap.hasOwnProperty(state)) {
      return this.componentStateColorSetMap[state];
    }
    return new BoxColorSet();
  }

  getReleaseStateColorSet(state: string): BoxColorSet | undefined {
    if (!!this.releaseStateColorSetMap &&
      this.releaseStateColorSetMap.hasOwnProperty(state)) {
      return this.releaseStateColorSetMap[state];
    }
    return new BoxColorSet();
  }

  getUserRoleColorSet(role: string): BoxColorSet | undefined {
    if (!!this.userRoleColorSetMap &&
      this.userRoleColorSetMap.hasOwnProperty(role)) {
      return this.userRoleColorSetMap[role];
    }
    return new BoxColorSet();
  }
}

export class BoxColorSet {
  font: string;
  background: string;
}
