import { Component, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Auth } from '../auth';
import { mensaje } from '../api';

@Component({
  imports: [FormsModule, RouterLink],
  template: `
    <main class="public-site">
      <header class="public-nav">
        <a class="brand" routerLink="/" aria-label="Maíz ERP, inicio">
          <span class="brand-mark">M</span>
          <span>MAÍZ <i>/</i> ERP<small>Producción y comercialización</small></span>
        </a>
        <nav aria-label="Navegación principal">
          <a href="#catalogo">Productos</a>
          <a href="#contacto">Contacto</a>
        </nav>
        <a class="staff-link" [routerLink]="auth.token() ? '/panel' : '/acceso'">
          <span class="staff-icon">↗</span>{{ auth.token() ? 'Abrir sistema' : 'Acceso del personal' }}
        </a>
      </header>

      <section class="hero">
        <div class="hero-copy">
          <span class="eyebrow"><span class="eyebrow-dot"></span> DEL CAMPO A TU NEGOCIO</span>
          <h1>Producción que conecta.<br><em>Productos que cumplen.</em></h1>
          <p>Conoce nuestros productos terminados y conversa con nuestro equipo comercial para atender tu negocio.</p>
          <div class="hero-actions">
            <a class="button-primary" href="#catalogo">Explorar productos <span>↓</span></a>
            <a class="button-secondary" href="#contacto">Hablar con ventas <span>↗</span></a>
          </div>
          <div class="hero-note"><span class="note-check">✓</span> Calidad y atención en un solo lugar</div>
        </div>
        <div class="hero-art" aria-label="Ilustración abstracta de cultivos y producción" role="img">
          <div class="art-sun"></div><div class="art-field field-back"></div><div class="art-field field-front"></div>
          <div class="grain-stalk stalk-one"><i></i><i></i><i></i><i></i><b></b></div>
          <div class="grain-stalk stalk-two"><i></i><i></i><i></i><i></i><b></b></div>
          <div class="art-caption"><span>MAÍZ ERP</span><strong>Del origen<br>al producto final.</strong><small>Producción · calidad · despacho</small></div>
          <div class="art-seal"><span>✳</span><small>GESTIÓN<br>INTEGRADA</small></div>
        </div>
      </section>

      <section class="catalog-section" id="catalogo">
        <div class="section-heading">
          <div><span class="eyebrow">NUESTRO CATÁLOGO</span><h2>Productos para tu operación</h2><p>Consulta con el equipo comercial para conocer presentaciones y disponibilidad.</p></div>
          <span class="catalog-count">{{ productos().length }} {{ productos().length === 1 ? 'producto' : 'productos' }}</span>
        </div>
        @if(error()){<p class="catalog-error" role="alert">{{ error() }}</p>}
        <div class="product-grid">
          @for(p of productos();track p.codigo){
            <article class="product-card">
              <div class="product-top"><span class="product-symbol">✳</span><span class="product-code">{{ p.codigo }}</span></div>
              <div><h3>{{ p.nombre }}</h3><p>{{ p.descripcion || 'Consulta con nuestro equipo comercial para conocer más sobre este producto.' }}</p></div>
              <a href="#contacto" class="product-link">Consultar producto <span>↗</span></a>
            </article>
          } @empty {
            @if(!error()){<div class="empty-catalog"><span>✳</span><h3>Estamos preparando el catálogo</h3><p>Vuelve pronto o escríbenos para consultar por nuestros productos.</p><a href="#contacto">Contactar al equipo <span>↗</span></a></div>}
          }
        </div>
      </section>

      <section class="contact-section" id="contacto">
        <div class="contact-copy"><span class="eyebrow">ATENCIÓN COMERCIAL</span><h2>Conversemos sobre lo que necesitas.</h2><p>Déjanos tus datos y un mensaje. La solicitud llegará a nuestro equipo de ventas.</p>
          <div class="contact-points"><span><i>01</i> Cuéntanos qué buscas</span><span><i>02</i> Nuestro equipo te contactará</span><span><i>03</i> Revisamos juntos la propuesta</span></div>
        </div>
        <div class="contact-card"><div class="contact-card-head"><span class="contact-icon">✉</span><div><h3>Enviar una consulta</h3><p>Te responderemos a tu correo.</p></div></div>
          <form (ngSubmit)="enviar()" #f="ngForm">
            <label>Nombre<input name="nombre" [(ngModel)]="contacto.nombre" required maxlength="120" placeholder="Tu nombre"></label>
            <label>Correo electrónico<input name="email" type="email" [(ngModel)]="contacto.email" required email placeholder="nombre@empresa.com"></label>
            <label>¿En qué podemos ayudarte?<textarea name="mensaje" [(ngModel)]="contacto.mensaje" required maxlength="1000" rows="4" placeholder="Escribe tu consulta..."></textarea></label>
            <button class="button-primary send-button" [disabled]="f.invalid||ocupado()">{{ ocupado() ? 'Enviando…' : 'Enviar consulta' }} <span>↗</span></button>
            <p class="form-status" role="status">{{ estado() }}</p>
          </form>
        </div>
      </section>

      <footer class="public-footer"><a class="footer-brand" routerLink="/">MAÍZ <i>/</i> ERP</a><span>Producción y comercialización</span><span>© {{ anio }} Maíz ERP</span></footer>
    </main>
  `,
  styles: [`
    :host{display:block;--forest:#173f31;--forest2:#245840;--lime:#dce9a6;--gold:#d4aa4d;--paper:#f5f6ef;--ink:#18251f;--muted:#68766d;--line:#e1e6dc;font-family:"Segoe UI",Arial,sans-serif;color:var(--ink)}
    .public-site{min-height:100vh;background:var(--paper);overflow:hidden}.public-nav{max-width:1240px;margin:auto;height:88px;padding:0 34px;display:flex;align-items:center;justify-content:space-between;gap:28px}.brand{display:flex;align-items:center;gap:12px;color:var(--ink);font-weight:800;letter-spacing:.4px}.brand-mark{width:42px;height:42px;border-radius:14px;display:grid;place-items:center;background:var(--forest);color:#fff;font-size:21px}.brand i,.footer-brand i{font-style:normal;color:#a6bb68}.brand small{display:block;margin-top:3px;color:var(--muted);font-size:10px;font-weight:500;letter-spacing:.2px}.public-nav nav{display:flex;align-items:center;gap:32px;margin-left:auto;margin-right:20px}.public-nav nav a{font-size:13px;font-weight:600;color:#536259}.public-nav nav a:hover,.product-link:hover{color:var(--forest2)}.staff-link{display:inline-flex;align-items:center;gap:10px;border:1px solid #cbd5c9;padding:11px 15px;border-radius:10px;background:#fff;color:var(--forest);font-weight:700;font-size:12px;box-shadow:0 5px 18px #203b2710;transition:.2s}.staff-link:hover{border-color:var(--forest);transform:translateY(-1px)}.staff-icon{display:grid;place-items:center;width:23px;height:23px;border-radius:7px;background:#edf3e8;font-size:15px}
    .hero{max-width:1240px;min-height:480px;margin:14px auto 0;padding:32px 34px 54px;display:grid;grid-template-columns:1fr .9fr;align-items:center;gap:40px}.hero-copy{padding:10px 0 4px}.eyebrow{display:flex;align-items:center;gap:8px;font-size:10px;letter-spacing:1.7px;font-weight:800;color:#55735a}.eyebrow-dot{width:7px;height:7px;border-radius:50%;background:#91a94e}.hero h1{font-size:clamp(40px,5vw,66px);line-height:1.04;letter-spacing:-2.8px;margin:22px 0 20px;color:#17251f;font-weight:760}.hero h1 em{font-style:normal;color:var(--forest2)}.hero-copy>p{max-width:530px;font-size:16px;line-height:1.7;color:#66746b;margin:0}.hero-actions{display:flex;align-items:center;gap:12px;margin-top:30px;flex-wrap:wrap}.button-primary,.button-secondary{display:inline-flex;align-items:center;justify-content:center;gap:18px;min-height:48px;padding:0 18px;border-radius:10px;font-size:13px;font-weight:700;text-decoration:none;transition:transform .18s,background .18s}.button-primary{border:0;background:var(--forest);color:#fff}.button-primary:hover:not(:disabled){background:#235640;transform:translateY(-2px)}.button-secondary{color:var(--forest);border:1px solid #d4ddd1;background:#fff}.button-secondary:hover{transform:translateY(-2px);border-color:#92a78e}.hero-note{display:flex;align-items:center;gap:9px;margin-top:27px;color:#67756a;font-size:12px}.note-check{width:19px;height:19px;border-radius:50%;display:grid;place-items:center;background:#e4ecd5;color:#496c42;font-size:11px;font-weight:800}
    .hero-art{height:370px;position:relative;overflow:hidden;border-radius:30px;background:linear-gradient(150deg,#e7edcf 0%,#cbd8a5 48%,#809b69 100%);box-shadow:0 22px 55px #294b291d}.hero-art:before{content:"";position:absolute;inset:0;background:linear-gradient(115deg,transparent 35%,#f9f7df35 36%,transparent 73%),radial-gradient(ellipse at 80% 12%,#fff9d18c 0 8%,transparent 28%)}.art-sun{position:absolute;width:112px;height:112px;border-radius:50%;right:20%;top:38px;background:#f1dda0;filter:blur(.2px);opacity:.9}.art-field{position:absolute;left:-12%;right:-12%;height:55%;border-radius:50% 50% 0 0/18% 18% 0 0;transform:rotate(-7deg)}.field-back{bottom:-9%;background:linear-gradient(160deg,#9eb77b,#5c8156)}.field-front{bottom:-31%;height:56%;background:linear-gradient(165deg,#496e4d,#244b3a)}.grain-stalk{position:absolute;bottom:48px;width:8px;height:205px;border-radius:100%;background:#476f43;transform:rotate(-8deg);z-index:2}.stalk-one{left:62%;height:220px}.stalk-two{left:77%;height:178px;transform:rotate(9deg)}.grain-stalk b{position:absolute;width:28px;height:82px;border-radius:50%;background:repeating-linear-gradient(0deg,#d7ae4d 0 8px,#e6c66d 8px 13px);left:-10px;top:22px;transform:rotate(9deg);box-shadow:0 2px 8px #73571e33}.grain-stalk i{position:absolute;width:55px;height:18px;border-radius:100% 0 100% 0;background:#6e914d;left:-47px;top:90px;transform:rotate(20deg)}.grain-stalk i:nth-child(2){top:121px;left:0;transform:rotate(160deg)}.grain-stalk i:nth-child(3){top:151px;left:-42px;transform:rotate(20deg)}.grain-stalk i:nth-child(4){top:174px;left:0;transform:rotate(160deg)}.art-caption{position:absolute;z-index:3;left:28px;bottom:27px;color:#fff}.art-caption span{display:block;font-size:9px;font-weight:800;letter-spacing:2px;color:#e4edc3;margin-bottom:9px}.art-caption strong{font-family:Georgia,serif;font-size:25px;line-height:1.1}.art-caption small{display:block;margin-top:10px;color:#e4ebd7;font-size:10px}.art-seal{position:absolute;z-index:4;top:24px;left:24px;width:82px;height:82px;border:1px solid #ffffff8c;border-radius:50%;display:grid;place-content:center;text-align:center;color:white;background:#ffffff16;backdrop-filter:blur(4px)}.art-seal span{font-size:22px;line-height:1}.art-seal small{font-size:7px;line-height:1.3;letter-spacing:1px;margin-top:3px}
    .catalog-section{max-width:1240px;margin:0 auto;padding:44px 34px 72px}.section-heading{display:flex;justify-content:space-between;align-items:end;gap:24px;margin-bottom:24px}.section-heading h2,.contact-copy h2{font-size:clamp(27px,3vw,38px);letter-spacing:-1.1px;line-height:1.13;margin:11px 0;color:#1b2c23}.section-heading p,.contact-copy>p{margin:0;color:var(--muted);font-size:13px}.catalog-count{border:1px solid var(--line);border-radius:999px;background:#fff;padding:9px 13px;color:#607062;font-size:11px;font-weight:700;white-space:nowrap}.product-grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:16px}.product-card{min-height:224px;border:1px solid #e4e8df;border-radius:16px;background:#fff;padding:21px;display:flex;flex-direction:column;justify-content:space-between;box-shadow:0 8px 26px #263c2c08;transition:transform .2s,box-shadow .2s}.product-card:hover{transform:translateY(-4px);box-shadow:0 16px 36px #263c2c14}.product-top{display:flex;align-items:center;justify-content:space-between}.product-symbol{width:36px;height:36px;border-radius:11px;display:grid;place-items:center;background:#f2f5e9;color:#a77d27;font-size:20px}.product-code{color:#66805d;font-size:9px;font-weight:800;letter-spacing:1.4px}.product-card h3{font-size:18px;margin:20px 0 7px;color:#1d2b22}.product-card p{margin:0;color:#758078;font-size:12px;line-height:1.6}.product-link{display:flex;justify-content:space-between;align-items:center;margin-top:18px;padding-top:13px;border-top:1px solid #edf0e9;color:#456b4b;font-size:11px;font-weight:750}.empty-catalog{grid-column:1/-1;padding:40px;border:1px dashed #cbd6c4;border-radius:16px;text-align:center;background:#fbfcf8}.empty-catalog>span{font-size:25px;color:#a77d27}.empty-catalog h3{margin:12px 0 6px}.empty-catalog p{color:var(--muted);font-size:13px}.empty-catalog a{color:#456b4b;font-weight:700;font-size:12px}.catalog-error{padding:13px 16px;border-radius:10px;background:#fff1ee;color:#9d4037}
    .contact-section{position:relative;background:#e9eddf;padding:68px max(34px,calc((100% - 1172px)/2));display:grid;grid-template-columns:.9fr 1.1fr;align-items:center;gap:clamp(32px,7vw,100px)}.contact-copy h2{max-width:440px;margin-top:15px}.contact-copy>p{max-width:430px;line-height:1.7}.contact-points{display:grid;gap:13px;margin-top:30px}.contact-points span{display:flex;align-items:center;gap:12px;font-size:12px;font-weight:650;color:#405648}.contact-points i{display:grid;place-items:center;width:27px;height:27px;border-radius:50%;background:#fff;color:#719057;font-style:normal;font-size:9px;font-weight:800}.contact-card{max-width:520px;width:100%;justify-self:end;background:#fff;border:1px solid #e4e8df;border-radius:18px;padding:25px;box-shadow:0 17px 45px #253c2912}.contact-card-head{display:flex;align-items:center;gap:12px;margin-bottom:20px}.contact-icon{width:41px;height:41px;border-radius:12px;background:#eff3e5;color:#4f7454;display:grid;place-items:center;font-size:18px}.contact-card-head h3{font-size:16px;margin:0 0 4px}.contact-card-head p{font-size:11px;color:var(--muted);margin:0}.contact-card form{display:grid;gap:13px}.contact-card label{display:grid;gap:7px;color:#59685c;font-size:11px;font-weight:650}.contact-card input,.contact-card textarea{width:100%;border:1px solid #dce3d9;border-radius:9px;background:#fbfcf9;padding:11px 12px;color:#26372b;font:inherit;font-size:12px;outline:none}.contact-card input:focus,.contact-card textarea:focus{border-color:#809b6b;box-shadow:0 0 0 3px #809b6b22}.contact-card textarea{resize:vertical;min-height:90px}.send-button{width:100%;justify-content:space-between;cursor:pointer}.send-button:disabled{opacity:.5;cursor:not-allowed}.form-status{min-height:16px;color:#52764a;font-size:11px;margin:0}.public-footer{max-width:1240px;margin:auto;min-height:78px;padding:18px 34px;display:flex;align-items:center;justify-content:space-between;gap:16px;color:#7b877d;font-size:10px}.footer-brand{color:#293b2d;font-weight:850;letter-spacing:.6px;font-size:12px}
    @media(max-width:850px){.hero{grid-template-columns:1fr .8fr;gap:22px}.hero-art{height:330px}.hero h1{font-size:clamp(38px,6vw,54px)}.contact-section{gap:30px}.public-nav nav{gap:18px}}
    @media(max-width:650px){.public-nav{height:72px;padding:0 18px}.public-nav nav{display:none}.staff-link{font-size:11px;padding:9px 10px}.brand-mark{width:37px;height:37px}.hero{grid-template-columns:1fr;padding:35px 20px 46px;margin:0;gap:28px}.hero h1{font-size:clamp(39px,11vw,55px);letter-spacing:-1.8px}.hero-copy>p{font-size:14px}.hero-art{height:280px;border-radius:22px}.grain-stalk{bottom:22px}.catalog-section{padding:46px 20px 54px}.section-heading{align-items:start;flex-direction:column;gap:12px}.product-grid{grid-template-columns:1fr}.product-card{min-height:190px}.contact-section{padding:48px 20px;grid-template-columns:1fr;gap:28px}.contact-card{justify-self:stretch;max-width:none;padding:20px}.public-footer{padding:18px 20px;flex-wrap:wrap}.public-footer span:last-child{width:100%}}
    @media(prefers-reduced-motion:reduce){*,*::before,*::after{scroll-behavior:auto!important;transition-duration:.01ms!important}}
  `],
})
export class Portal {
  http = inject(HttpClient);
  auth = inject(Auth);
  productos = signal<any[]>([]);
  error = signal('');
  estado = signal('');
  ocupado = signal(false);
  anio = new Date().getFullYear();
  contacto = { nombre: '', email: '', mensaje: '' };

  constructor() {
    this.http.get<any[]>('/api/articulos/catalogo-publico').subscribe({
      next: r => this.productos.set(r),
      error: e => this.error.set(mensaje(e)),
    });
  }

  enviar() {
    if (this.ocupado()) return;
    this.ocupado.set(true);
    this.estado.set('');
    this.http.post('/api/portal/contacto', this.contacto).subscribe({
      next: () => {
        this.estado.set('Solicitud registrada. Gracias por contactarnos.');
        this.contacto = { nombre: '', email: '', mensaje: '' };
        this.ocupado.set(false);
      },
      error: e => {
        this.estado.set(mensaje(e));
        this.ocupado.set(false);
      },
    });
  }
}

@Component({
  imports: [RouterLink],
  template: `
    <main class="access-page">
      <header class="access-nav"><a class="brand" routerLink="/"><span class="brand-mark">M</span><span>MAÍZ <i>/</i> ERP<small>Producción y comercialización</small></span></a><a routerLink="/" class="back-link">← Volver al portal</a></header>
      <section class="access-layout">
        <div class="access-intro"><span class="eyebrow"><span></span> PORTAL INTERNO</span><h1>Todo tu equipo,<br><em>en una sola operación.</em></h1><p>Ingresa al sistema de gestión para continuar con las tareas de tu área.</p>
          <div class="module-list"><span><i>01</i> Ventas y pedidos</span><span><i>02</i> Inventario y despacho</span><span><i>03</i> Compras y producción</span></div>
          <div class="access-art"><div class="access-orbit orbit-one"></div><div class="access-orbit orbit-two"></div><div class="access-logo">M</div><span class="access-leaf leaf-one">✳</span><span class="access-leaf leaf-two">✳</span><div class="access-caption"><small>MAÍZ ERP</small><strong>Una operación<br>conectada.</strong></div></div>
        </div>
        <div class="login-card"><div class="login-card-top"><span class="login-icon">↗</span><span class="secure-label"><i></i> Acceso seguro</span></div><span class="eyebrow">BIENVENIDO DE VUELTA</span><h2>Acceso del personal</h2><p class="login-description">Usa la cuenta asignada por el administrador para ingresar al sistema.</p><button class="login-button" (click)="ingresar()">Continuar al inicio de sesión <span>→</span></button><div class="login-divider"><span>Autenticación institucional</span></div><div class="keycloak-note"><span class="key-icon">✓</span><p>Tu cuenta se valida de forma segura. Los módulos que verás dependen de los permisos de tu usuario.</p></div><a routerLink="/" class="return-link">← Regresar al catálogo público</a></div>
      </section>
      <footer class="access-footer"><span>© {{ anio }} Maíz ERP</span><span>Acceso protegido para personal autorizado</span></footer>
    </main>
  `,
  styles: [`
    :host{display:block;min-height:100vh;--forest:#173f31;--forest2:#245840;--gold:#d4aa4d;--ink:#18251f;--muted:#68766d;font-family:"Segoe UI",Arial,sans-serif;color:var(--ink)}.access-page{min-height:100vh;background:#f4f5ef;position:relative;overflow:hidden}.access-page:before{content:"";position:absolute;width:620px;height:620px;border-radius:50%;right:-220px;top:-270px;background:#e6ebd9}.access-nav{height:86px;max-width:1180px;margin:auto;padding:0 28px;display:flex;align-items:center;justify-content:space-between;position:relative;z-index:1}.brand{display:flex;align-items:center;gap:11px;color:var(--ink);font-weight:800;letter-spacing:.4px;text-decoration:none}.brand-mark{width:40px;height:40px;border-radius:13px;display:grid;place-items:center;background:var(--forest);color:#fff;font-size:20px}.brand i{font-style:normal;color:#a6bb68}.brand small{display:block;margin-top:3px;color:var(--muted);font-size:10px;font-weight:500}.back-link,.return-link{color:#627269;font-size:12px;font-weight:650;text-decoration:none}.back-link:hover,.return-link:hover{color:var(--forest2)}.access-layout{position:relative;z-index:1;max-width:1080px;min-height:calc(100vh - 164px);margin:0 auto;padding:42px 28px 56px;display:grid;grid-template-columns:1.15fr .85fr;align-items:center;gap:clamp(42px,9vw,120px)}.access-intro{max-width:530px}.eyebrow{display:flex;align-items:center;gap:8px;font-size:10px;letter-spacing:1.6px;font-weight:800;color:#56735b}.eyebrow span{width:7px;height:7px;border-radius:50%;background:#91a94e}.access-intro h1{font-size:clamp(39px,5vw,61px);line-height:1.06;letter-spacing:-2.2px;margin:20px 0;color:#17251f}.access-intro h1 em{font-style:normal;color:var(--forest2)}.access-intro>p{max-width:430px;color:#6b786f;font-size:14px;line-height:1.7}.module-list{display:grid;gap:12px;margin-top:25px}.module-list span{display:flex;align-items:center;gap:11px;color:#48594d;font-size:12px;font-weight:650}.module-list i{width:25px;height:25px;border-radius:8px;background:#e6ecd9;color:#56784f;display:grid;place-items:center;font-size:9px;font-style:normal;font-weight:800}.access-art{position:relative;height:147px;margin-top:27px;border-radius:17px;overflow:hidden;background:linear-gradient(120deg,#1e4c38,#678051);box-shadow:0 13px 32px #27452c1e}.access-art:after{content:"";position:absolute;inset:0;background:linear-gradient(95deg,transparent,#ffffff12)}.access-orbit{position:absolute;border:1px solid #ffffff3b;border-radius:50%;transform:rotate(-24deg)}.orbit-one{width:300px;height:160px;right:-17px;top:-9px}.orbit-two{width:240px;height:100px;right:10px;top:26px}.access-logo{position:absolute;right:64px;top:25px;width:86px;height:86px;border-radius:28px;display:grid;place-items:center;color:white;background:#ffffff18;border:1px solid #ffffff48;font-weight:800;font-size:40px;transform:rotate(7deg)}.access-leaf{position:absolute;color:#d7b455;font-size:26px}.leaf-one{right:180px;top:20px}.leaf-two{right:18px;bottom:13px;font-size:17px}.access-caption{position:absolute;z-index:2;left:20px;bottom:17px;color:white}.access-caption small{display:block;font-size:8px;letter-spacing:1.5px;color:#dbe6be;font-weight:800}.access-caption strong{display:block;margin-top:5px;font-size:16px;line-height:1.1}.login-card{width:100%;max-width:390px;justify-self:end;background:white;border:1px solid #e3e8de;border-radius:20px;padding:30px;box-shadow:0 20px 55px #263c2c15}.login-card-top{display:flex;align-items:center;justify-content:space-between;margin-bottom:28px}.login-icon{width:44px;height:44px;border-radius:13px;background:#edf2e5;color:#47704c;display:grid;place-items:center;font-size:20px;font-weight:700}.secure-label{display:flex;align-items:center;gap:7px;color:#66766a;font-size:10px;font-weight:650}.secure-label i{width:7px;height:7px;border-radius:50%;background:#65a574;box-shadow:0 0 0 3px #65a57420}.login-card h2{font-size:27px;letter-spacing:-.7px;margin:10px 0;color:#1b2b22}.login-description{font-size:12px;line-height:1.7;color:#748077;margin:0 0 24px}.login-button{width:100%;height:49px;display:flex;align-items:center;justify-content:space-between;padding:0 16px;border:0;border-radius:10px;background:var(--forest);color:white;font-weight:700;font-size:12px;cursor:pointer;transition:.18s}.login-button:hover{background:#245840;transform:translateY(-1px)}.login-button span{font-size:18px}.login-divider{display:flex;align-items:center;gap:10px;margin:22px 0;color:#8b968d;font-size:9px}.login-divider:before,.login-divider:after{content:"";height:1px;flex:1;background:#e8ece5}.keycloak-note{display:flex;gap:10px;padding:12px;border:1px solid #edf0e9;border-radius:10px;background:#fafbf8}.key-icon{flex:0 0 20px;width:20px;height:20px;border-radius:50%;display:grid;place-items:center;background:#e7efdb;color:#547b4f;font-weight:800;font-size:11px}.keycloak-note p{margin:0;color:#6f7c71;font-size:10px;line-height:1.55}.return-link{display:block;margin-top:22px;font-size:10px}.access-footer{position:relative;z-index:1;min-height:78px;max-width:1180px;margin:auto;padding:18px 28px;display:flex;align-items:center;justify-content:space-between;color:#859087;font-size:10px}.access-footer span:last-child{color:#607064}@media(max-width:760px){.access-nav{height:74px;padding:0 19px}.access-layout{min-height:auto;padding:45px 20px 35px;grid-template-columns:1fr;gap:32px}.access-intro h1{font-size:clamp(38px,10vw,54px)}.access-art{height:130px}.login-card{max-width:none;justify-self:stretch;padding:24px}.access-footer{padding:17px 20px;gap:10px;align-items:flex-start;flex-direction:column}}
  `],
})
export class AccesoPersonal {
  private auth = inject(Auth);
  anio = new Date().getFullYear();

  constructor() {
    if (this.auth.token()) location.assign('/panel');
  }

  ingresar() {
    void this.auth.login('/panel');
  }
}
