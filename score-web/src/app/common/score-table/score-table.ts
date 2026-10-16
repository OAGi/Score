import {
  ScoreCellDefDirective,
  ScoreCellDirective,
  ScoreColumnDefDirective,
  ScoreHeaderCellDefDirective,
  ScoreHeaderCellDirective
} from './score-column-def.directive';
import {ScoreDetailDefDirective} from './score-detail-def.directive';
import {ScoreTableViewComponent} from './score-table-view.component';

/** Everything a screen needs to use `score-table-view` and to write its columns. */
export const SCORE_TABLE = [
  ScoreTableViewComponent,
  ScoreColumnDefDirective,
  ScoreHeaderCellDefDirective,
  ScoreHeaderCellDirective,
  ScoreCellDefDirective,
  ScoreCellDirective,
  ScoreDetailDefDirective
] as const;
