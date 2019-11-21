import {Component, OnInit} from '@angular/core';
import {CcNodeService} from '../domain/core-component-node.service';
import {Router} from '@angular/router';


@Component({
  selector: 'srt-acc-create',
  templateUrl: './acc-create.component.html',
  styleUrls: ['./acc-create.component.css']
})
export class AccCreateComponent implements OnInit {

  constructor(private service: CcNodeService,
              private router: Router) {
  }

  ngOnInit() {
    this.service.createAcc().subscribe(resp => {
      this.router.navigateByUrl('/core_component/acc/' + 0 + '/' + resp.accId);
    });
  }

}

