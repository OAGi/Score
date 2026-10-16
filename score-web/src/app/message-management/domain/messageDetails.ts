import {ScoreUser} from '../../authentication/domain/auth';
import {WhoAndWhen} from '../../basis/basis';

export class CountOfUnreadMessages {
  countOfUnreadMessages: number;
}

export class MessageListEntry {
  messageId: number;
  subject: string;
  body: string;
  read: boolean;

  created: WhoAndWhen;
}

export class MessageDetails {
  messageId: number;
  subject: string;
  body: string;
  bodyContentType: string;
  read: boolean;

  recipient: ScoreUser;
  created: WhoAndWhen;
}
