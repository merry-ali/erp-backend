import { map } from "rxjs";
import { inject, Injectable } from "@angular/core";
import { HttpClient, HttpErrorResponse } from "@angular/common/http";
import { Persona, Articulo, Orden, OrdenRequest } from "./models";
@Injectable({ providedIn: "root" })
export class Api {
  private http = inject(HttpClient);
  personas() {
    return this.http.get<Persona[]>("/api/v1/personas");
  }
  consultarDocumento(tipo: string, numero: string) {
    return this.http.get<{ numeroDocumento: string; nombreORazonSocial: string; direccion: string }>(
      "/api/v1/personas/consulta-documento", { params: { tipo, numero } },
    );
  }
  verificarDocumento(numero: string, excluirId: number | null) {
    const params: Record<string, string> = { numero };
    if (excluirId !== null) params["excluirId"] = String(excluirId);
    return this.http.get<{ existe: boolean }>("/api/v1/personas/verificar-documento", { params });
  }
  guardarPersona(id: number | null, data: unknown) {
    return id
      ? this.http.put<Persona>(`/api/v1/personas/${id}`, data)
      : this.http.post<Persona>("/api/v1/personas", data);
  }
  bajaPersona(id: number) {
    return this.http.delete<void>(`/api/v1/personas/${id}`);
  }
  articulos() {
    return this.http.get<any[]>("/api/articulos").pipe(map(rows=>rows.map(a=>({id:a.id,descripcion:a.nombre,variedad:a.codigo,precioUnitario:a.costoReferencial,stock:0,pesoSacoKg:1,activo:a.activo}))));
  }
  guardarArticulo(id: number | null, data: unknown) {
    return id
      ? this.http.put<Articulo>(`/api/v1/articulos/${id}`, data)
      : this.http.post<Articulo>("/api/v1/articulos", data);
  }
  bajaArticulo(id: number) {
    return this.http.delete<void>(`/api/v1/articulos/${id}`);
  }
  ordenes(params: Record<string, string> = {}) {
    return this.http.get<Orden[]>("/api/v1/ordenes-compra", { params });
  }
  crearOrden(data: OrdenRequest) {
    return this.http.post<Orden>("/api/v1/ordenes-compra", data);
  }
  transicion(id: number, accion: "recibir" | "anular") {
    return this.http.patch<Orden>(`/api/v1/ordenes-compra/${id}/${accion}`, {});
  }
}
export function mensaje(error: unknown): string {
  const e = error as HttpErrorResponse;
  if (e.status === 0)
    return "No se pudo conectar al servidor. Comprueba que el backend esté iniciado.";
  if (e.status === 401)
    return "La sesión terminó o el token no es válido. Vuelve a ingresar.";
  if (e.status === 403)
    return "Tu rol no permite esta operación o el registro pertenece a otro operador.";
  if (e.status === 404)
    return "El proveedor, artículo u orden ya no existe. Actualiza los datos.";
  return (
    e.error?.message ||
    "No se pudo completar la operación. Revisa los datos e inténtalo nuevamente."
  );
}
