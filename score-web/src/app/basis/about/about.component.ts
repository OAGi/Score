import {Component, OnInit} from '@angular/core';
import {AboutService} from './domain/about.service';
import {ProductInfo} from './domain/about';
import {projectVersion} from '../../../environments/version';

@Component({
  standalone: false,
  selector: 'score-about',
  templateUrl: './about.component.html',
  styleUrls: ['./about.component.css']
})
export class AboutComponent implements OnInit {

  productInfos: ProductInfo[] = [];

  constructor(private service: AboutService) {
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
