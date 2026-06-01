import { Component, HostListener, signal } from '@angular/core';
import { RouterOutlet, NavigationEnd, Router } from '@angular/router';

import { NavbarComponent } from './shared/components/navbar/navbar.component';
import { SidebarComponent } from './shared/components/sidebar/sidebar.component';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent, SidebarComponent],
  template: `
    @if (auth.isLoggedIn()) {
      <app-navbar (toggleSidebar)="toggleSidebar()"></app-navbar>
      <div class="app-shell" [class.sidebar-open]="sidebarOpen()">
        @if (sidebarOpen() && isMobile()) {
          <div class="sidebar-backdrop" (click)="closeSidebar()"></div>
        }
        <app-sidebar></app-sidebar>
        <main class="main-content">
          <router-outlet></router-outlet>
        </main>
      </div>
    } @else {
      <router-outlet></router-outlet>
    }
  `,
  styleUrl: './app.css',
})
export class App {
  sidebarOpen = signal(false);
  isMobile = signal(false);

  constructor(
    public auth: AuthService,
    private router: Router,
  ) {
    this.updateIsMobile();
    this.router.events.subscribe((e) => {
      if (e instanceof NavigationEnd && this.isMobile()) this.sidebarOpen.set(false);
    });
  }

  @HostListener('window:resize')
  onResize() {
    this.updateIsMobile();
  }

  private updateIsMobile() {
    const mobile = window.innerWidth <= 768;
    this.isMobile.set(mobile);
    if (!mobile) this.sidebarOpen.set(false);
  }

  toggleSidebar() {
    this.sidebarOpen.update((v) => !v);
  }
  closeSidebar() {
    this.sidebarOpen.set(false);
  }
}
