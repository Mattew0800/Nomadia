import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import {Test} from '../test/test';

@Component({
  selector: 'app-messages-page',
  standalone: true,
  imports: [CommonModule, Test],
  templateUrl: './messages-page.html',
  styleUrls: ['./messages-page.scss'],
})
export class MessagesPage {

}
