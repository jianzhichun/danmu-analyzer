import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { TagCloudModule } from 'angular-tag-cloud-module';
import { LoadingModule } from 'ngx-loading';
import { NgxGraphModule } from '@swimlane/ngx-graph';
import { NgxChartsModule } from '@swimlane/ngx-charts';

import { AppComponent } from './app.component';
import { DanmuService } from './danmu.service';
import { HomeComponent } from './home/home.component';
import { WordCloudComponent } from './word-cloud/word-cloud.component';
import { StateTranspositionComponent } from './state-transposition/state-transposition.component';

export const ROUTES: Routes = [
  { path: 'view', component: HomeComponent },
  { path: 'view/word-cloud/:source', component: WordCloudComponent },
  { path: 'view/state-transposition/:source', component: StateTranspositionComponent }
];

@NgModule({
  declarations: [
    AppComponent,
    WordCloudComponent,
    HomeComponent,
    StateTranspositionComponent
  ],
  imports: [
    NgxGraphModule,
    NgxChartsModule,
    BrowserModule,
    BrowserAnimationsModule,
    TagCloudModule,
    LoadingModule,
    RouterModule.forRoot(ROUTES)
  ],
  providers: [
    DanmuService
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
