import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { Danmu } from './danmu';

declare var EventSource;

@Injectable()
export class DanmuService {

  constructor() { }

  observeMessages(sseUrl: string): Observable<Array<Danmu>> {
    return new Observable<Array<Danmu>>(obs => {
      const es = new EventSource(sseUrl);
      es.addEventListener('message', (evt) => {
        let dammus: Array<Danmu> = JSON.parse(evt.data);
        obs.next(dammus);
      });
      return () => es.close();
    });
  }
}
