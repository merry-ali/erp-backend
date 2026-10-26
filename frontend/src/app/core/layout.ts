import { Component, inject, signal } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { Auth } from './auth/auth';

@Component({
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="app-shell" [class.nav-collapsed]="collapsed()" [class.nav-open]="mobileOpen()">
      <aside class="app-sidebar">
        <a class="brand" routerLink="/panel" (click)="mobileOpen.set(false)">
          <span class="brand-mark">M</span><span class="brand-copy">Maíz ERP<small>Producción y comercialización</small></span>
        </a>
        <p class="nav-label">ESPACIO DE TRABAJO</p>
        <nav aria-label="Navegación principal">
          <a routerLink="/panel" routerLinkActive="selected" title="Resumen" (click)="mobileOpen.set(false)"><span class="nav-icon">⌂</span><span class="nav-copy">Resumen</span></a>
          @for(n of menu;track n.path){@if(auth.puede(n.modulo)||(n.path==='/personas'&&auth.puede('VENTAS'))){
            @if(n.path==='/ventas'){
              <button class="nav-parent" [class.selected-parent]="ventasOpen()" [title]="n.nombre" [attr.aria-expanded]="ventasOpen()" (click)="ventasOpen.update(v=>!v)"><span class="nav-icon">{{n.icon}}</span><span class="nav-copy">{{n.nombre}}</span><span class="nav-chevron">{{ventasOpen()?'⌄':'›'}}</span></button>
              @if(ventasOpen()){
                <div class="nav-submenu">
                  <a routerLink="/ventas/vender" routerLinkActive="selected" (click)="mobileOpen.set(false)"><span class="submenu-dot">•</span><span class="nav-copy">Nueva venta</span></a>
                  <a routerLink="/ventas/cotizaciones" routerLinkActive="selected" (click)="mobileOpen.set(false)"><span class="submenu-dot">•</span><span class="nav-copy">Cotizaciones</span></a>
                  <a routerLink="/ventas/pedidos" routerLinkActive="selected" (click)="mobileOpen.set(false)"><span class="submenu-dot">•</span><span class="nav-copy">Pedidos</span></a>
                  <a routerLink="/ventas/historial" routerLinkActive="selected" (click)="mobileOpen.set(false)"><span class="submenu-dot">•</span><span class="nav-copy">Consultar ventas</span></a>
                </div>
              }
            }@else{
              <a [routerLink]="n.path" routerLinkActive="selected" [title]="n.nombre" (click)="mobileOpen.set(false)"><span class="nav-icon">{{n.icon}}</span><span class="nav-copy">{{n.nombre}}</span></a>
            }
          }}
          <a routerLink="/" title="Portal público" (click)="mobileOpen.set(false)"><span class="nav-icon">↗</span><span class="nav-copy">Portal público</span></a>
        </nav>
        <div class="sidebar-bottom"><span class="session-role">{{auth.perfil()?.roles?.join(' · ')}}</span><button class="logout-button" (click)="auth.salir()"><span class="nav-icon">⇥</span><span class="nav-copy">Cerrar sesión</span></button></div>
      </aside>
      <button class="nav-backdrop" aria-label="Cerrar menú" (click)="mobileOpen.set(false)"></button>
      <div class="app-workspace">
        <header class="app-topbar">
          <div class="topbar-start"><button class="icon-button menu-toggle" aria-label="Alternar menú" (click)="toggleMenu()">☰</button><span class="topbar-title">Sistema integral de gestión</span></div>
          <div class="topbar-end"><span class="role-pill">{{auth.perfil()?.roles?.join(' · ')}}</span><button class="theme-toggle" [attr.aria-label]="theme()==='dark'?'Cambiar a tema claro':'Cambiar a tema oscuro'" [title]="theme()==='dark'?'Tema claro':'Tema oscuro'" (click)="toggleTheme()"><span>{{theme()==='dark'?'☀':'◐'}}</span><span class="theme-label">{{theme()==='dark'?'Claro':'Oscuro'}}</span></button></div>
        </header>
        <main class="app-content"><router-outlet/></main>
      </div>
    </div>
  `,
})
export class Layout {
  auth = inject(Auth);
  private router = inject(Router);
  collapsed = signal(false);
  mobileOpen = signal(false);
  ventasOpen = signal(this.router.url.startsWith('/ventas'));
  theme = signal<'light'|'dark'>('light');
  menu = [
    { path:'/produccion', nombre:'Producción', icon:'⚙', modulo:'PRODUCCION' },
    { path:'/compras', nombre:'Compras', icon:'⇧', modulo:'COMPRAS' },
    { path:'/inventario', nombre:'Inventario', icon:'▦', modulo:'INVENTARIO' },
    { path:'/ventas', nombre:'Ventas', icon:'▤', modulo:'VENTAS' },
    { path:'/catalogos/articulos', nombre:'Artículos y unidades', icon:'◫', modulo:'INVENTARIO' },
    { path:'/personas', nombre:'Personas y proveedores', icon:'♙', modulo:'COMPRAS' },
    { path:'/administracion', nombre:'Administración y roles', icon:'⚙', modulo:'ADMINISTRACION' },
    { path:'/reportes', nombre:'Reporte de producción', icon:'▥', modulo:'PRODUCCION' },
  ];

  constructor() {
    const saved = localStorage.getItem('maiz-erp-theme');
    const initial = saved === 'dark' ? 'dark' : 'light';
    this.theme.set(initial);
    document.documentElement.dataset['theme'] = initial;
  }

  toggleTheme() {
    const next = this.theme() === 'dark' ? 'light' : 'dark';
    this.theme.set(next);
    document.documentElement.dataset['theme'] = next;
    localStorage.setItem('maiz-erp-theme', next);
  }

  toggleMenu() {
    if (window.matchMedia('(max-width: 760px)').matches) this.mobileOpen.update(v => !v);
    else this.collapsed.update(v => !v);
  }
}
