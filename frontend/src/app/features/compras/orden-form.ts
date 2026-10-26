import { Component, inject, signal, computed } from "@angular/core";
import {
  ReactiveFormsModule,
  FormBuilder,
  Validators,
  FormControl,
  FormGroup,
} from "@angular/forms";
import { Router, RouterLink } from "@angular/router";
import { CurrencyPipe } from "@angular/common";
import { toSignal } from "@angular/core/rxjs-interop";
import { MatAutocompleteModule } from "@angular/material/autocomplete";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { forkJoin } from "rxjs";
import { Api, mensaje } from "../../api";
import { Auth } from "../../auth";
import { Articulo, Persona } from "../../models";
import {
  totalCompra,
  stockTrasRecepcion,
  construirOrden,
} from "./compra-utils";
type Fila = FormGroup<{
  articuloId: FormControl<number | null>;
  busqueda: FormControl<string>;
  cantidad: FormControl<number>;
}>;
@Component({
  imports: [
    ReactiveFormsModule,
    RouterLink,
    CurrencyPipe,
    MatAutocompleteModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  templateUrl: "./orden-form.html",
})
export class OrdenForm {
  private fb = inject(FormBuilder);
  private api = inject(Api);
  private router = inject(Router);
  auth = inject(Auth);
  articulos = signal<Articulo[]>([]);
  proveedores = signal<Persona[]>([]);
  error = signal("");
  ocupado = signal(false);
  cargando = signal(true);
  confirmando = signal(false);
  form = this.fb.group({
    proveedorId: this.fb.control<number | null>(null, [
      Validators.required,
      Validators.min(1),
    ]),
    condicionesPactadas: this.fb.nonNullable.control(
      "",
      Validators.maxLength(255),
    ),
    detalles: this.fb.array<Fila>([], Validators.required),
  });
  valores = toSignal(this.form.valueChanges, {
    initialValue: this.form.getRawValue(),
  });
  get filas() {
    return this.form.controls.detalles;
  }
  lineas = computed(() => {
    this.valores();
    return this.filas.getRawValue();
  });
  total = computed(() => totalCompra(this.lineas(), this.articulos()));
  unidades = computed(() =>
    this.lineas().reduce((s, l) => s + (Number(l.cantidad) || 0), 0),
  );
  proveedor = computed(() => {
    this.valores();
    return this.proveedores().find(
      (p) => p.id === this.form.controls.proveedorId.value,
    );
  });
  constructor() {
    this.agregar();
    forkJoin({
      articulos: this.api.articulos(),
      personas: this.api.personas(),
    }).subscribe({
      next: (r) => {
        this.articulos.set(r.articulos.filter((a) => a.activo));
        this.proveedores.set(
          r.personas.filter((p) => p.activo && p.roles.includes("PROVEEDOR")),
        );
        this.cargando.set(false);
      },
      error: (e) => {
        this.cargando.set(false);
        this.error.set(mensaje(e));
      },
    });
  }
  agregar() {
    if (this.ocupado()) return;
    const fila = this.fb.group({
      articuloId: this.fb.control<number | null>(null, Validators.required),
      busqueda: this.fb.nonNullable.control(""),
      cantidad: this.fb.nonNullable.control(1, [
        Validators.required,
        Validators.min(1),
        Validators.max(1000000),
        Validators.pattern(/^\d+$/),
      ]),
    });
    fila.controls.busqueda.valueChanges.subscribe(() => {
      fila.controls.articuloId.setValue(null, { emitEvent: false });
      fila.updateValueAndValidity();
    });
    this.filas.push(fila);
  }
  quitar(i: number) {
    if (this.filas.length > 1 && !this.ocupado()) this.filas.removeAt(i);
  }
  opciones(i: number) {
    this.valores();
    const b = this.filas.at(i).controls.busqueda.value;
    return this.articulos().filter((a) =>
      (a.descripcion + " " + a.variedad)
        .toLowerCase()
        .includes(String(b ?? "").toLowerCase()),
    );
  }
  seleccionar(i: number, id: number) {
    const a = this.articulos().find((a) => a.id === id);
    if (!a) return;
    const f = this.filas.at(i);
    f.controls.busqueda.setValue(a.descripcion, { emitEvent: false });
    f.controls.articuloId.setValue(a.id);
  }
  articulo(i: number) {
    return this.articulos().find((a) => a.id === this.lineas()[i]?.articuloId);
  }
  subtotal(i: number) {
    const l = this.lineas()[i];
    return l ? totalCompra([l], this.articulos()) : 0;
  }
  stock(i: number) {
    return stockTrasRecepcion(
      this.lineas()[i].articuloId || 0,
      this.lineas(),
      this.articulos(),
    );
  }
  revisar() {
    this.error.set("");
    if (this.form.invalid || !this.filas.length) {
      this.form.markAllAsTouched();
      this.error.set(
        "Selecciona un proveedor, elige cada artículo de la lista y usa cantidades enteras positivas.",
      );
      return;
    }
    if (!this.auth.es("COMPRADOR", "ADMIN")) {
      this.error.set("Solo OPERADOR o SUPERVISOR pueden emitir órdenes.");
      return;
    }
    this.confirmando.set(true);
  }
  guardar() {
    if (this.ocupado() || this.form.invalid) return;
    const raw = this.form.getRawValue();
    const body = construirOrden(
      raw.proveedorId!,
      raw.condicionesPactadas,
      raw.detalles,
    );
    this.ocupado.set(true);
    this.form.disable({ emitEvent: false });
    this.api.crearOrden(body).subscribe({
      next: () => {
        this.ocupado.set(false);
        this.confirmando.set(false);
        void this.router.navigate(["/compras"]);
      },
      error: (e) => {
        this.form.enable({ emitEvent: false });
        this.ocupado.set(false);
        this.confirmando.set(false);
        this.error.set(mensaje(e));
      },
    });
  }
}
