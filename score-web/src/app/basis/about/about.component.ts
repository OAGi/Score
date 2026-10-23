import { Component, OnInit, inject, ChangeDetectionStrategy } from '@angular/core';
import {AboutService} from './domain/about.service';
import {ProductInfo} from './domain/about';
import {projectVersion} from '../../../environments/version';

@Component({
  standalone: false,
  selector: 'score-about',
  templateUrl: './about.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./about.component.css']
})
export class AboutComponent implements OnInit {
  private service = inject(AboutService);


  productInfos: ProductInfo[] = [];

  constructor() {
    const webProductInfo = new ProductInfo();
    webProductInfo.productName = 'score-web';
    webProductInfo.productVersion = projectVersion;

    this.productInfos.push(webProductInfo);
  }

  ngOnInit() {
    this.service.getProductInfo().subscribe(resp => {
      this.productInfos.push(...resp);
    });
  }

}
