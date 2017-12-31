import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CloudData, CloudOptions } from 'angular-tag-cloud-module';
import { DanmuService } from '../danmu.service';
import { Observable, Subscription } from 'rxjs/Rx';
import 'rxjs/add/operator/bufferCount'
import 'rxjs/add/operator/filter'

import { Danmu } from '../danmu';

@Component({
  selector: 'app-word-cloud',
  templateUrl: './word-cloud.component.html',
  styleUrls: ['./word-cloud.component.css']
})
export class WordCloudComponent implements OnInit {

  public loading = false;
  private sseStream: Subscription;

  options: CloudOptions = {
    width: 1000,
    height: 400,
    overflow: false
  }

  dataDict: { [key: string]: CloudData; } = {};
  data: CloudData[] = [{ text: "waitting at list 30s", weight: 10 }];
  clicked: CloudData;

  constructor(private danmuService: DanmuService, private route: ActivatedRoute) { }

  update(danmus: Danmu[], nns: Set<String>) {
    danmus.filter(danmu => !nns || nns.has(danmu.nn))
      .forEach(danmu => {
        let words: Array<string> = danmu.kw.length > 0 ? danmu.kw : danmu.tok;
        words.filter(word => word.length < 4)
          .forEach(word => {
            let _data: CloudData = this.dataDict[word] ?
              this.dataDict[word] :
              {
                text: word,
                weight: 0,
                color: '#' + (0x1000000 + (Math.random()) * 0xffffff).toString(16).substr(1, 6)
              };
            _data.weight += 1;
            this.dataDict[word] = _data
          })
      })
    this.data = Array.from(Object.values(this.dataDict))
      .sort((cd1, cd2) => cd2.weight - cd1.weight)
      .slice(0, 88);
    this.loading = false;
  }

  showData(clicked: CloudData) {
    this.clicked = clicked;
  }

  ngOnInit() {
    this.loading = true;
    let bufferTime: number = this.route.snapshot.queryParams.bufferTime ? this.route.snapshot.queryParams.bufferTime : 30;
    let _nns = this.route.snapshot.queryParams.nn;
    let nns: Set<string> = _nns ? new Set(_nns instanceof Array ? _nns : [_nns]) : undefined;
    this.route.params.subscribe((params) => {
      this.sseStream =
        this.danmuService
          .observeMessages(`api/${params.source}.stream?bufferTime=${bufferTime}`)
          .subscribe(danmus => this.update(danmus, nns));
    });

  }

  ngOnDestroy() {
    if (this.sseStream) {
      this.sseStream.unsubscribe();
    }
  }

}
