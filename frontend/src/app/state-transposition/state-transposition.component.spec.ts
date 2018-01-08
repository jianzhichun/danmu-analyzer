import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { StateTranspositionComponent } from './state-transposition.component';

describe('StateTranspositionComponent', () => {
  let component: StateTranspositionComponent;
  let fixture: ComponentFixture<StateTranspositionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ StateTranspositionComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(StateTranspositionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
