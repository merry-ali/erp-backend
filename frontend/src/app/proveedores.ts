import { Component, inject, signal, computed } from "@angular/core";
import { ReactiveFormsModule, FormBuilder, Validators } from "@angular/forms";
import { Api, mensaje } from "./api";
import { Auth } from "./auth";
import { Persona } from "./models";
type PersonaFormValue = {
  tipoDocumento: string;
  numeroDocumento: string;
  nombreORazonSocial: string;
  direccion: string;
  telefono: string;
  email: string;
  rol: string;
  activo: boolean;
};
@Component({
  imports: [ReactiveFormsModule],
  styles: [`.documento-input-row{display:flex;gap:8px;align-items:center}.documento-input-row input{flex:1;min-width:0;width:auto}.documento-input-row button{white-space:nowrap}`],
  template: `<div class="page-title">
      <div>
        <span class="eyebrow">PERSONAS</span>
        <h1>Personas y proveedores</h1>
        <p class="muted">Mantén al día los contactos de tu abastecimiento.</p>
      </div>
      @if ((auth.puede('COMPRAS')||auth.puede('VENTAS')||auth.es('ADMIN'))) {
        <button class="primary" (click)="nuevo()">＋ Registrar persona</button>
      }
    </div>
    @if (error()) {
      <div class="alert error" role="alert">{{ error() }}</div>
    }
    @if (exito()) {
      <div class="alert success" role="status">{{ exito() }}</div>
    }
    <section class="card">
      <div class="toolbar">
        <input
          aria-label="Buscar persona"
          placeholder="Buscar por nombre o documento…"
          (input)="busqueda.set($any($event.target).value)"
        /><span>{{ filtradas().length }} registros</span>
      </div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Nombre / razón social</th>
              <th>Documento</th>
              <th>Contacto</th>
              <th>Rol</th>
              <th>Estado</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            @for (p of filtradas(); track p.id) {
              <tr>
                <td>
                  <b>{{ p.nombreORazonSocial }}</b>
                </td>
                <td>{{ p.tipoDocumento }} {{ p.numeroDocumento }}</td>
                <td>
                  {{ p.email || "Sin correo"
                  }}<small class="block muted">{{ p.telefono }}</small>
                </td>
                <td>{{ p.roles.join(", ") }}</td>
                <td>
                  <span class="badge" [class.inactiva]="!p.activo">{{
                    p.activo ? "ACTIVO" : "INACTIVO"
                  }}</span>
                </td>
                <td>
                  @if ((auth.puede('COMPRAS')||auth.puede('VENTAS')||auth.es('ADMIN'))) {
                    <button class="ghost" (click)="editar(p)">Editar</button>
                    @if (p.activo) {
                      <button
                        class="danger-link"
                        (click)="baja(p)"
                        [disabled]="ocupado()"
                      >
                        Dar de baja
                      </button>
                    }
                  }
                </td>
              </tr>
            } @empty {
              <tr>
                <td colspan="6" class="empty">
                  No hay personas con esa búsqueda.
                </td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    </section>
    @if (abierto()) {
      <div class="modal-backdrop">
        <section
          class="modal"
          role="dialog"
          aria-modal="true"
          aria-labelledby="persona-title"
        >
          <div class="section-title">
            <h2 id="persona-title">
              {{ id() ? "Editar persona" : "Nueva persona" }}
            </h2>
            <button
              class="ghost"
              (click)="abierto.set(false)"
              [disabled]="ocupado()"
            >
              Cerrar
            </button>
          </div>
          <form [formGroup]="form" (ngSubmit)="guardar()">
            <div class="form-grid">
              <label
                >Tipo de documento<select formControlName="tipoDocumento" (change)="detectarTipoDocumento()">
                  <option>DNI</option>
                  <option>RUC</option>
                  <option>CE</option>
                  <option>PASAPORTE</option>
                </select></label
              ><label
                >Número de documento<span class="documento-input-row"><input
                  formControlName="numeroDocumento"
                  maxlength="20" (input)="detectarTipoDocumento()" /><button type="button" class="primary" (click)="consultarDocumento()" [disabled]="consultando()">{{ consultando() ? "Consultando…" : "Consultar" }}</button></span></label
              ><label class="span2"
                >Nombre o razón social<input
                  formControlName="nombreORazonSocial"
                  maxlength="150" /></label
              ><label
                >Correo<input
                  formControlName="email"
                  type="email"
                  maxlength="120" /></label
              ><label
                >Teléfono<input
                  formControlName="telefono"
                  maxlength="20" /></label
              ><label class="span2"
                >Dirección<input
                  formControlName="direccion"
                  maxlength="200" /></label
              ><label
                >Rol<select formControlName="rol">
                  <option>PROVEEDOR</option>
                  <option>CLIENTE</option>
                  <option value="AMBOS">CLIENTE Y PROVEEDOR</option>
                </select></label
              ><label
                >Estado<select formControlName="activo">
                  <option [ngValue]="true">Activo</option>
                  <option [ngValue]="false">Inactivo</option>
                </select></label
              >
            </div>
            @if (mensajeConsulta()) {
              <p class="muted" role="status">{{ mensajeConsulta() }}</p>
            }
            @if (mensajeFormulario()) {
              <p class="field-error" role="alert">{{ mensajeFormulario() }}</p>
            }
            @if (form.invalid && form.touched) {
              <p class="field-error">
                Completa documento, nombre y un correo válido si lo indicas.
              </p>
            }
            <button class="primary wide" [disabled]="ocupado()">
              {{ ocupado() ? "Guardando…" : "Guardar persona" }}
            </button>
          </form>
        </section>
      </div>
    }`,
})
export class Proveedores {
  api = inject(Api);
  auth = inject(Auth);
  fb = inject(FormBuilder);
  lista = signal<Persona[]>([]);
  busqueda = signal("");
  abierto = signal(false);
  id = signal<number | null>(null);
  error = signal("");
  exito = signal("");
  ocupado = signal(false);
  consultando = signal(false);
  mensajeConsulta = signal("");
  mensajeFormulario = signal("");
  filtradas = computed(() =>
    this.lista().filter((p) =>
      (p.nombreORazonSocial + " " + p.numeroDocumento)
        .toLowerCase()
        .includes(this.busqueda().toLowerCase()),
    ),
  );
  form = this.fb.nonNullable.group({
    tipoDocumento: ["RUC", Validators.required],
    numeroDocumento: ["", [Validators.required, Validators.maxLength(20)]],
    nombreORazonSocial: ["", [Validators.required, Validators.maxLength(150)]],
    direccion: [""],
    telefono: [""],
    email: ["", Validators.email],
    rol: ["PROVEEDOR"],
    activo: [true],
  });
  constructor() {
    this.cargar();
  }
  cargar() {
    this.api
      .personas()
      .subscribe({
        next: (r) => this.lista.set(r),
        error: (e) => this.error.set(mensaje(e)),
      });
  }
  nuevo() {
    this.id.set(null);
    this.form.reset({ tipoDocumento: "RUC", rol: "PROVEEDOR", activo: true });
    this.error.set("");
    this.exito.set("");
    this.mensajeConsulta.set("");
    this.mensajeFormulario.set("");
    this.abierto.set(true);
  }
  editar(p: Persona) {
    this.id.set(p.id);
    this.form.reset({ ...p, rol: p.roles.length > 1 ? "AMBOS" : p.roles[0] });
    this.error.set("");
    this.mensajeConsulta.set("");
    this.mensajeFormulario.set("");
    this.abierto.set(true);
  }
  detectarTipoDocumento() {
    if (!['DNI', 'RUC'].includes(this.form.controls.tipoDocumento.value)) return;
    const numero = this.form.controls.numeroDocumento.value.trim();
    if (/^\d{8}$/.test(numero)) this.form.controls.tipoDocumento.setValue("DNI");
    else if (/^\d{11}$/.test(numero)) this.form.controls.tipoDocumento.setValue("RUC");
  }
  consultarDocumento() {
    const { tipoDocumento, numeroDocumento } = this.form.getRawValue();
    this.mensajeFormulario.set("");
    if (!['DNI', 'RUC'].includes(tipoDocumento)) {
      this.mensajeConsulta.set("La consulta en línea está disponible para DNI y RUC. Puedes completar los datos manualmente.");
      return;
    }
    const numero = numeroDocumento.trim();
    const valido = tipoDocumento === 'DNI' ? /^\d{8}$/.test(numero) : /^\d{11}$/.test(numero);
    if (!valido) {
      this.mensajeConsulta.set(tipoDocumento === 'DNI'
        ? "Ingresa un DNI válido de 8 dígitos."
        : "Ingresa un RUC válido de 11 dígitos.");
      return;
    }
    this.consultando.set(true);
    this.mensajeConsulta.set("");
    this.api.consultarDocumento(tipoDocumento, numero).subscribe({
      next: (r) => {
        this.consultando.set(false);
        this.form.patchValue({ nombreORazonSocial: r.nombreORazonSocial, direccion: r.direccion || this.form.controls.direccion.value });
        this.mensajeConsulta.set("Datos encontrados. Verifica la información antes de guardar.");
      },
      error: (e) => {
        this.consultando.set(false);
        this.mensajeConsulta.set(this.mensajeErrorConsulta(e));
      },
    });
  }
  private mensajeErrorConsulta(error: any): string {
    const status = error?.status;
    if (status === 0) return "No se pudo conectar con el servidor. Puedes completar los datos manualmente y volver a intentar después.";
    if (status === 401) return "Tu sesión venció. Inicia sesión otra vez para consultar el documento.";
    if (status === 403) return "Tu usuario no tiene permiso para consultar documentos.";
    if (status === 404) return "No se encontró el documento o API Perú rechazó la consulta. Verifica el número, el token y el plan de API Perú; también puedes completar los datos manualmente.";
    if (status === 429) return "Se alcanzó el límite de consultas. Espera un momento o completa los datos manualmente.";
    if (status === 503) return error?.error?.detail || error?.error?.message || "El servicio de consulta no está disponible. Puedes completar los datos manualmente.";
    if (status === 502) return "API Perú no respondió correctamente. Intenta más tarde o completa los datos manualmente.";
    return error?.error?.detail || error?.error?.message || mensaje(error);
  }
  guardar() {
    this.mensajeFormulario.set("");
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = { ...this.form.getRawValue(), numeroDocumento: this.form.controls.numeroDocumento.value.trim() };
    this.ocupado.set(true);
    this.api.verificarDocumento(v.numeroDocumento, this.id()).subscribe({
      next: ({ existe }) => {
        if (existe) {
          this.ocupado.set(false);
          this.mensajeFormulario.set("Ya existe una persona registrada con ese número de documento. Revisa el número o edita el registro existente.");
          return;
        }
        this.guardarVerificado(v);
      },
      error: (e) => {
        this.ocupado.set(false);
        this.mensajeFormulario.set(mensaje(e));
      },
    });
  }
  private guardarVerificado(v: PersonaFormValue) {
    this.api.guardarPersona(this.id(), {
        ...v,
        roles: v.rol === "AMBOS" ? ["CLIENTE", "PROVEEDOR"] : [v.rol],
      })
      .subscribe({
        next: () => {
          this.ocupado.set(false);
          this.abierto.set(false);
          this.exito.set("Persona guardada.");
          this.cargar();
        },
        error: (e) => {
          this.ocupado.set(false);
          this.mensajeFormulario.set(mensaje(e));
        },
      });
  }
  baja(p: Persona) {
    if (
      !confirm(
        "¿Dar de baja a " +
          p.nombreORazonSocial +
          "? No podrá usarse en nuevas compras.",
      )
    )
      return;
    this.ocupado.set(true);
    this.api.bajaPersona(p.id).subscribe({
      next: () => {
        this.ocupado.set(false);
        this.exito.set("La persona quedó inactiva.");
        this.cargar();
      },
      error: (e) => {
        this.ocupado.set(false);
        this.error.set(mensaje(e));
      },
    });
  }
}
