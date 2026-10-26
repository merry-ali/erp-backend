export interface Persona {
  id: number;
  tipoDocumento: string;
  numeroDocumento: string;
  nombreORazonSocial: string;
  direccion: string;
  telefono: string;
  email: string;
  roles: string[];
  activo: boolean;
}
export interface Articulo {
  id: number;
  descripcion: string;
  variedad: string;
  precioUnitario: number;
  stock: number;
  pesoSacoKg: number;
  activo: boolean;
}
export interface Detalle {
  id: number;
  articuloId: number;
  descripcionArticulo: string;
  precioUnitario: number;
  cantidad: number;
  subtotal: number;
}
export interface Orden {
  id: number;
  fecha: string;
  estado: "EMITIDA" | "RECIBIDA" | "ANULADA";
  total: number;
  proveedorId: number;
  nombreProveedor: string;
  numeroDocumentoProveedor: string;
  condicionesPactadas: string;
  detalles: Detalle[];
}
export interface Perfil {
  sub: string;
  email: string;
  roles: string[];
}
export interface OrdenRequest {
  proveedorId: number;
  condicionesPactadas: string;
  detalles: { articuloId: number; cantidad: number }[];
}
