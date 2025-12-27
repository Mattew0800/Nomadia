import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Activitylist } from './activity-list';

describe('Activitylist', () => {
  let component: Activitylist;
  let fixture: ComponentFixture<Activitylist>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Activitylist]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Activitylist);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
