import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { TagCloudModule } from 'angular-tag-cloud-module';

import { LoadingModule } from 'ngx-loading';
import { AppComponent } from './app.component';
import { DanmuService } from './danmu.service';
import { HomeComponent } from './home/home.component';
import { WordCloudComponent } from './word-cloud/word-cloud.component';

export const ROUTES: Routes = [
  { path: '', component: HomeComponent },
  { path: 'word-cloud/:source', component: WordCloudComponent }
];

@NgModule({
  declarations: [
    AppComponent,
    WordCloudComponent,
    HomeComponent
  ],
  imports: [
    BrowserModule,
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
