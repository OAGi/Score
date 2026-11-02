export class TenantList {
  tenantId: number;
  name: string;
}

export class TenantInfo {
  tenantId: number;
  name: string;
  usersCount: number;
  businessCtxCount: number;
}

export class BusinessTenantContext {
  businessCtxId: number;
  name: string;
  checked: boolean;
}

