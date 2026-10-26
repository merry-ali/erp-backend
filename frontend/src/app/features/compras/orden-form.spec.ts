import { TestBed } from "@angular/core/testing";
import { provideHttpClient } from "@angular/common/http";
import {
  provideHttpClientTesting,
  HttpTestingController,
} from "@angular/common/http/testing";
import { provideRouter, Router } from "@angular/router";
import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import { OrdenForm } from "./orden-form";
import { Auth } from "../../auth";
import {
  totalCompra,
  stockTrasRecepcion,
  construirOrden,
} from "./compra-utils";
import { Articulo } from "../../models";
const articulos: Articulo[] = [
  {
    id: 10,
    descripcion: "Amarillo",
    variedad: "AMARILLO",
    precioUnitario: 85,
    stock: 2,
    pesoSacoKg: 50,
    activo: true,
  },
];
describe("Formulario de compras", () => {
  let form: OrdenForm;
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [OrdenForm],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: Auth, useValue: { es: () => true } },
      ],
    });
    form = TestBed.createComponent(OrdenForm).componentInstance;
    http = TestBed.inject(HttpTestingController);
    http.expectOne("/api/articulos").flush([{id:10,nombre:"Amarillo",codigo:"AMARILLO",costoReferencial:85,activo:true}]);
    http
      .expectOne("/api/v1/personas")
      .flush([
        {
          id: 1,
          nombreORazonSocial: "Proveedor",
          roles: ["PROVEEDOR"],
          activo: true,
        },
      ]);
  });
  afterEach(() => http.verify());
  it("obliga a elegir del catálogo y limpia la selección al escribir de nuevo", () => {
    form.seleccionar(0, 10);
    expect(form.filas.at(0).controls.articuloId.value).toBe(10);
    form.filas.at(0).controls.busqueda.setValue("texto libre");
    expect(form.filas.at(0).controls.articuloId.value).toBeNull();
    expect(form.form.invalid).toBe(true);
  });
  it("mantiene al menos una línea y rechaza cantidades fraccionarias", () => {
    form.quitar(0);
    expect(form.filas.length).toBe(1);
    form.agregar();
    form.quitar(1);
    expect(form.filas.length).toBe(1);
    form.filas.at(0).controls.cantidad.setValue(1.5);
    expect(form.filas.at(0).controls.cantidad.invalid).toBe(true);
  });
  it("envía solo ids y cantidades y conserva el formulario ante un 409", () => {
    form.form.controls.proveedorId.setValue(1);
    form.seleccionar(0, 10);
    form.revisar();
    expect(form.confirmando()).toBe(true);
    const router = TestBed.inject(Router);
    const nav = vi.spyOn(router, "navigate");
    form.guardar();
    expect(form.ocupado()).toBe(true);
    expect(form.filas.at(0).controls.articuloId.value).toBe(10);
    expect(form.total()).toBe(85);
    const r = http.expectOne("/api/v1/ordenes-compra");
    expect(r.request.body).toEqual({
      proveedorId: 1,
      condicionesPactadas: "",
      detalles: [{ articuloId: 10, cantidad: 1 }],
    });
    r.flush(
      { message: "Artículo inactivo" },
      { status: 409, statusText: "Conflict" },
    );
    expect(form.form.enabled).toBe(true);
    expect(form.filas.at(0).controls.articuloId.value).toBe(10);
    expect(form.total()).toBe(85);
    expect(form.form.controls.proveedorId.value).toBe(1);
    expect(form.ocupado()).toBe(false);
    expect(nav).not.toHaveBeenCalled();
    expect(form.error()).toContain("Artículo inactivo");
  });
});
describe("Cálculos de la compra", () => {
  it("incluye las líneas repetidas en el stock previsto y en el total", () => {
    const lineas = [
      { articuloId: 10, busqueda: "Amarillo", cantidad: 2 },
      { articuloId: 10, busqueda: "Amarillo", cantidad: 3 },
    ];
    expect(totalCompra(lineas, articulos)).toBe(425);
    expect(stockTrasRecepcion(10, lineas, articulos)).toBe(7);
  });
  it("no construye un pedido vacío, con id sin seleccionar ni cantidad decimal", () => {
    expect(() => construirOrden(1, "", [])).toThrow();
    expect(() =>
      construirOrden(1, "", [
        { articuloId: null, busqueda: "texto", cantidad: 1 },
      ]),
    ).toThrow();
    expect(() =>
      construirOrden(1, "", [
        { articuloId: 10, busqueda: "Amarillo", cantidad: 1.5 },
      ]),
    ).toThrow();
  });
});
