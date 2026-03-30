import { Component, OnInit, inject } from '@angular/core';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {AuthService} from '../../authentication/auth.service';
import {MessageService} from '../domain/message.service';
import {MessageDetails} from '../domain/messageDetails';
import {finalize} from 'rxjs/operators';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Title} from '@angular/platform-browser';
import {setAppTitleIfPresent} from '../../common/app-title.strategy';

@Component({
  standalone: false,
  selector: 'score-message-view',
  templateUrl: './message-view.component.html',
  styleUrls: ['./message-view.component.css']
})
export class MessageViewComponent implements OnInit {
  private location = inject(Location);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private auth = inject(AuthService);
  private snackBar = inject(MatSnackBar);
  private messageService = inject(MessageService);
  private titleService = inject(Title);


  loading: boolean;
  messageId: number;
  message: MessageDetails = new MessageDetails();

  ngOnInit(): void {
    this.loading = true;
    this.route.paramMap.subscribe(params => {
      this.messageId = Number(params.get('messageId'));
      this.messageService.getMessage(this.messageId).pipe(
        finalize(() => {
          this.loading = false;
        })
      ).subscribe(message => {
        this.message = message;
        setAppTitleIfPresent(this.titleService, this.message.subject, 'Message');
      });
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
