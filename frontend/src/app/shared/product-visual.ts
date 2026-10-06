import { Component, input } from '@angular/core';
import { Product } from '../core/store-api';

@Component({
  selector: 'app-product-visual',
  template: `
    @if (product().imagenUrl) {
      <div class="product-photo"><img [src]="product().imagenUrl" [alt]="product().nombre" loading="lazy"></div>
    } @else {
      <div class="product-photo product-photo-empty" role="img" [attr.aria-label]="product().nombre">
        <span>{{ product().nombre.slice(0, 2).toUpperCase() }}</span>
      </div>
    }
  `
})
export class ProductVisual {
  readonly product = input.required<Product>();
}
