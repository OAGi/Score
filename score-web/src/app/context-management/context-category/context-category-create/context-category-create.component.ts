import { Component, OnInit, inject } from '@angular/core';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {ContextCategoryService} from '../domain/context-category.service';
import {ContextCategoryDetails} from '../domain/context-category';
import {MatSnackBar} from '@angular/material/snack-bar';

@Component({
  standalone: false,
  selector: 'score-context-category-create',
  templateUrl: './context-category-create.component.html',
  styleUrls: ['./context-category-create.component.css']
})
export class ContextCategoryCreateComponent implements OnInit {
  private service = inject(ContextCategoryService);
  private location = inject(Location);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);


  title = 'Create Context Category';
  contextCategory: ContextCategoryDetails;

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
