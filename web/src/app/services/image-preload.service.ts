import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ImagePreloadService {
  private imageCache: Map<string, HTMLImageElement> = new Map();

  private tripTypeImages = [
    'TURISMO',
    'AVENTURA',
    'GASTRONOMICO',
    'EDUCATIVO',
    'FAMILIAR',
    'RELAX',
    'ROMANTICO',
    'CULTURAL',
    'PLAYA',
    'DEPORTIVO',
    'VOLUNTARIADO',
    'FIESTA',
    'PROFESIONAL'
  ];

  constructor() {
    this.preloadTripTypeImages();
  }

  private preloadTripTypeImages(): void {
    this.tripTypeImages.forEach(type => {
      const imgPath = `tripTypes/${type}.webp`;
      this.preloadImage(imgPath);
    });
  }

  private preloadImage(src: string): void {
    if (!this.imageCache.has(src)) {
      const img = new Image();
      img.src = src;
      img.onload = () => {
        this.imageCache.set(src, img);
      };
      img.onerror = () => {
        console.warn(`Failed to preload image: ${src}`);
      };
    }
  }

  public getImage(src: string): HTMLImageElement | undefined {
    return this.imageCache.get(src);
  }

  public isImageLoaded(src: string): boolean {
    return this.imageCache.has(src);
  }
}

