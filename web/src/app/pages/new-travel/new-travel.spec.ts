import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NewTravel } from './new-travel';

describe('NewTravel', () => {
  let component: NewTravel;
  let fixture: ComponentFixture<NewTravel>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NewTravel]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NewTravel);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
