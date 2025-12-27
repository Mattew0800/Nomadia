import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserProfileEdit } from './user-profile-edit';

describe('UserProfileEdit', () => {
  let component: UserProfileEdit;
  let fixture: ComponentFixture<UserProfileEdit>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserProfileEdit]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UserProfileEdit);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
