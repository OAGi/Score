import {Component, OnInit} from '@angular/core';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {AuthService} from '../../authentication/auth.service';
import {MessageService} from '../domain/message.service';
import {MessageDetails} from '../domain/messageDetails';
import {finalize} from 'rxjs/operators';
import {MatSnackBar} from '@angular/material/snack-bar';

@Component({
  selector: 'score-message-view',
  templateUrl: './message-view.component.html',
  styleUrls: ['./message-view.component.css']
})
export class MessageViewComponent implements OnInit {

  loading: boolean;
  messageId: number;
  message: MessageDetails = new MessageDetails();

  constructor(
    private location: Location,
    private router: Router,
    private route: ActivatedRoute,
    private auth: AuthService,
    private snackBar: MatSnackBar,
    private messageService: MessageService) {
  }

  ngOnInit(): void {
    this.loading = true;
    this.route.paramMap.subscribe(params => {
      this.messageId = Number(params.get('messageId'));
      this.messageService.getMessage(this.messageId).pipe(
        finalize(() => {
          this.loading = false;
        })
      ).subscribe(message => this.message = message);
    });
  }

  backToMessages() {
    this.router.navigateByUrl('/message');
  }

  discard() {
    this.loading = true;
    this.messageService.discard(this.messageId).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(_ => {
      this.snackBar.open('Discarded', '', {
        duration: 3000,
      });
      this.backToMessages();
    });
  }

}
