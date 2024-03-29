import {Component, OnInit} from '@angular/core';
import {AboutService} from './domain/about.service';
import {ProductInfo} from './domain/about';
// @ts-ignore
import pkg from '../../../../package.json';

@Component({
  selector: 'score-about',
  templateUrl: './about.component.html',
  styleUrls: ['./about.component.css']
})
export class AboutComponent implements OnInit {

  productInfos: ProductInfo[] = [];

  constructor(private service: AboutService) {
    const webProductInfo = new ProductInfo();
    webProductInfo.productName = 'score-web';
    webProductInfo.productVersion = pkg.version.replace('-rc', '');

    this.productInfos.push(webProductInfo);
  }

  ngOnInit() {
    this.service.getProductInfo().subscribe(resp => {
      this.productInfos.push(...resp);
    });
  }

}
