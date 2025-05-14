import {Component, OnInit} from '@angular/core';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {ContextCategoryService} from '../domain/context-category.service';
import {ContextCategoryDetails} from '../domain/context-category';
import {MatSnackBar} from '@angular/material/snack-bar';

@Component({
  selector: 'score-context-category-create',
  templateUrl: './context-category-create.component.html',
  styleUrls: ['./context-category-create.component.css']
})
export class ContextCategoryCreateComponent implements OnInit {

  title = 'Create Context Category';
  contextCategory: ContextCategoryDetails;

  constructor(private service: ContextCategoryService,
              private location: Location,
              private route: ActivatedRoute,
              private router: Router,
              private snackBar: MatSnackBar) {
  }

  ngOnInit() {
    this.contextCategory = new ContextCategoryDetails();
  }

  isDisabled(contextCategory: ContextCategoryDetails) {
    return contextCategory.name === undefined || contextCategory.name === '';
  }

  back() {
    this.location.back();
  }

  create() {
    this.service.create(this.contextCategory.name, this.contextCategory.description).subscribe(_ => {
      this.snackBar.open('Created', '', {
        duration: 3000,
      });
      this.router.navigateByUrl('/context_management/context_category');
    });
  }

}
