import { TestBed, inject } from '@angular/core/testing';

import { DanmuService } from './danmu.service';

describe('DanmuService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DanmuService]
    });
  });

  it('should be created', inject([DanmuService], (service: DanmuService) => {
    expect(service).toBeTruthy();
  }));
});
