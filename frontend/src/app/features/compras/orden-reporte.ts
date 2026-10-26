import { Component, inject, signal, computed } from "@angular/core";
import { ReactiveFormsModule, FormBuilder } from "@angular/forms";
import { RouterLink } from "@angular/router";
import { CurrencyPipe, DatePipe } from "@angular/common";
import { Subject, startWith, switchMap, catchError, of, tap } from "rxjs";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { Api, mensaje } from "../../api";
import { Auth } from "../../auth";
import { Orden, Persona } from "../../models";
@Component({
  imports: [ReactiveFormsModule, RouterLink, CurrencyPipe, DatePipe],
  templateUrl: "./orden-reporte.html",
})
export class OrdenReporte {
  api = inject(Api);
  auth = inject(Auth);
  fb = inject(FormBuilder);
  ordenes = signal<Orden[]>([]);
  proveedores = signal<Persona[]>([]);
  error = signal("");
  exito = signal("");
  cargando = signal(false);
  ocupado = signal(false);
  cambio = signal<{ orden: Orden; accion: "recibir" | "anular" } | null>(null);
  private refrescar$ = new Subject<void>();
  private parametros: Record<string, string> = {};
  filtros = this.fb.nonNullable.group({
    estado: [""],
    proveedorId: [""],
    inicio: [""],
    fin: [""],
  });
  monto = computed(() => this.ordenes().reduce((s, o) => s + o.total, 0));
  promedio = computed(() =>
    this.ordenes().length ? this.monto() / this.ordenes().length : 0,
  );
  constructor() {
    this.api
      .personas()
      .subscribe({
        next: (p) =>
          this.proveedores.set(p.filter((p) => p.roles.includes("PROVEEDOR"))),
        error: (e) => this.error.set(mensaje(e)),
      });
    this.refrescar$
      .pipe(
        startWith(undefined),
        tap(() => {
          this.cargando.set(true);
          this.error.set("");
        }),
        switchMap(() =>
          this.api.ordenes(this.parametros).pipe(
            catchError((e) => {
              this.error.set(mensaje(e));
              return of([] as Orden[]);
            }),
          ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((r) => {
        this.ordenes.set(r);
        this.cargando.set(false);
      });
  }
  buscar() {
    const f = this.filtros.getRawValue();
    if (f.inicio && f.fin && f.inicio > f.fin) {
      this.error.set("La fecha inicial debe ser anterior o igual a la final.");
      return;
    }
    this.parametros = {};
    if (f.estado) this.parametros["estado"] = f.estado;
    if (f.proveedorId) this.parametros["proveedorId"] = f.proveedorId;
    if (f.inicio) this.parametros["inicio"] = f.inicio + "T00:00:00";
    if (f.fin) this.parametros["fin"] = f.fin + "T23:59:59.999999999";
    this.refrescar$.next();
  }
  limpiar() {
    this.filtros.reset();
    this.buscar();
  }
  confirmar(o: Orden, accion: "recibir" | "anular") {
    this.cambio.set({ orden: o, accion });
  }
  ejecutar() {
    const c = this.cambio();
    if (!c || this.ocupado()) return;
    this.ocupado.set(true);
    this.api.transicion(c.orden.id, c.accion).subscribe({
      next: () => {
        this.ocupado.set(false);
        this.cambio.set(null);
        this.exito.set(
          c.accion === "recibir"
            ? "Recepción registrada. El inventario fue actualizado."
            : "Orden anulada. No se modificó el stock.",
        );
        this.refrescar$.next();
      },
      error: (e) => {
        this.ocupado.set(false);
        this.cambio.set(null);
        this.error.set(mensaje(e));
      },
    });
  }
}
