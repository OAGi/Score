export class ValidateRefactoringResponse {
  type: string;
  manifestId: number;
  issueList: IssuedCc[] = [];
}

export class IssuedCc {

  manifestId: number;
  guid: string;
  den: string;
  name: string;

  oagisComponentType: string;
  owner: string;
  state: string;
  revision: string;
  deprecated: boolean;
  lastUpdateUser: string;
  lastUpdateTimestamp: Date;
  releaseNum: string;
  id: number;

  reasons: string[];

}
