import { Component, input } from '@angular/core';
import { ORDER_STEPS, OrderStatus } from '../core/store-api';

@Component({
  selector: 'app-order-stepper',
  template: `
    <ol class="order-stepper" aria-label="Progreso del pedido">
      @for (step of steps; track step.value; let i = $index) {
        <li [class.completed]="i < currentIndex()" [class.current]="i === currentIndex()"
            [attr.aria-current]="i === currentIndex() ? 'step' : null">
          <span class="step-marker" aria-hidden="true">
            @if (i < currentIndex()) { <svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="2"><path d="m5 10 3 3 7-7"/></svg> }
            @else { <span class="step-dot"></span> }
          </span>
          <span class="step-label">{{ step.label }}<small>{{ i < currentIndex() ? 'Completado' : i === currentIndex() ? 'Estado actual' : 'Pendiente' }}</small></span>
        </li>
      }
    </ol>
  `
})
export class OrderStepper {
  readonly status = input.required<OrderStatus>();
  readonly steps = ORDER_STEPS;
  currentIndex() { return this.steps.findIndex(step => step.value === this.status()); }
}
