import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-landing-page',
  imports: [RouterLink],
  templateUrl: './landing-page.html',
  styleUrl: './landing-page.scss',

})
export class LandingPage implements OnInit {
  loading = true;

  private imagesToPreload = [
    'background.png',
    'blur.png',
    'nomadia-logo.png',
    'hex1.png',
    'hex2.png',
    'hex3.png'
  ];

  ngOnInit() {
    this.preloadImages();
  }

  private preloadImages() {
    const imagePromises = this.imagesToPreload.map(src => {
      return new Promise((resolve, reject) => {
        const img = new Image();
        img.onload = () => resolve(src);
        img.onerror = () => reject(src);
        img.src = src;
      });
    });

    Promise.all(imagePromises)
      .then(() => {
        this.loading = false;
      })
      .catch((error) => {
        console.warn('Error cargando imagen:', error);
        this.loading = false;
      });
  }
}
