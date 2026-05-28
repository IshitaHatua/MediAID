import { Component, HostListener, signal } from '@angular/core';
import { RouterOutlet, NavigationEnd, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from './shared/components/navbar/navbar.component';
import { SidebarComponent } from './shared/components/sidebar/sidebar.component';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule, NavbarComponent, SidebarComponent],
  template: `
    <ng-container *ngIf="auth.isLoggedIn(); else publicLayout">
      <app-navbar (toggleSidebar)="toggleSidebar()"></app-navbar>
      <div class="app-shell" [class.sidebar-open]="sidebarOpen()">
        <div class="sidebar-backdrop" *ngIf="sidebarOpen() && isMobile()" (click)="closeSidebar()"></div>
        <app-sidebar></app-sidebar>
        <main class="main-content">
          <router-outlet></router-outlet>
        </main>
      </div>
    </ng-container>
    <ng-template #publicLayout>
      <router-outlet></router-outlet>
    </ng-template>
  `,
  styles: [`
    .app-shell {
      display: flex;
      margin-top: 64px;
      min-height: calc(100vh - 64px);
      position: relative;
    }
    .main-content {
      flex: 1;
      padding: 28px 32px;
      min-height: calc(100vh - 64px);
      overflow-y: auto;
      overflow-x: hidden;
      max-width: calc(100vw - 230px);

      background-color: #0d1526;
      background-image:
        radial-gradient(rgba(99,102,241,0.12) 1px, transparent 1px);
      background-size: 28px 28px;
      background-attachment: local;
    }

    .sidebar-backdrop {
      position: fixed;
      top: 64px; left: 0; right: 0; bottom: 0;
      background: rgba(0,0,0,0.5);
      z-index: 90;
      animation: fadeIn 0.18s ease-out;
    }
    @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }

    @media (max-width: 1024px) {
      .main-content { padding: 22px 20px; }
    }

    @media (max-width: 768px) {
      .main-content {
        padding: 18px 14px;
        max-width: 100vw;
        width: 100vw;
      }
      app-sidebar {
        position: fixed;
        top: 64px;
        left: 0;
        bottom: 0;
        z-index: 95;
        transform: translateX(-100%);
        transition: transform 0.22s ease-out;
        box-shadow: 0 8px 32px rgba(0,0,0,0.5);
      }
      .app-shell.sidebar-open app-sidebar {
        transform: translateX(0);
      }
    }

    @media (max-width: 480px) {
      .main-content { padding: 14px 10px; }
    }
  `]
})
export class App {
  sidebarOpen = signal(false);
  isMobile = signal(false);

  constructor(public auth: AuthService, private router: Router) {
    this.updateIsMobile();
    this.router.events.subscribe(e => {
      if (e instanceof NavigationEnd && this.isMobile()) this.sidebarOpen.set(false);
    });
  }

  @HostListener('window:resize')
  onResize() { this.updateIsMobile(); }

  private updateIsMobile() {
    const mobile = window.innerWidth <= 768;
    this.isMobile.set(mobile);
    if (!mobile) this.sidebarOpen.set(false);
  }

  toggleSidebar() { this.sidebarOpen.update(v => !v); }
  closeSidebar() { this.sidebarOpen.set(false); }
}
