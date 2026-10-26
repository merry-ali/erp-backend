import { Articulo, OrdenRequest } from "../../models";
export interface LineaFormulario {
  articuloId: number | null;
  busqueda: string;
  cantidad: number;
}
export function totalCompra(
  lineas: LineaFormulario[],
  articulos: Articulo[],
): number {
  return (
    lineas.reduce(
      (total, l) =>
        total +
        Math.round(
          (articulos.find((a) => a.id === l.articuloId)?.precioUnitario || 0) *
            100,
        ) *
          l.cantidad,
      0,
    ) / 100
  );
}
export function stockTrasRecepcion(
  id: number,
  lineas: LineaFormulario[],
  articulos: Articulo[],
): number {
  return (
    (articulos.find((a) => a.id === id)?.stock || 0) +
    lineas
      .filter((l) => l.articuloId === id)
      .reduce((s, l) => s + l.cantidad, 0)
  );
}
export function construirOrden(
  proveedorId: number,
  condicionesPactadas: string,
  lineas: LineaFormulario[],
): OrdenRequest {
  if (
    !Number.isInteger(proveedorId) ||
    proveedorId <= 0 ||
    !lineas.length ||
    lineas.some(
      (l) =>
        !l.articuloId ||
        !Number.isInteger(l.articuloId) ||
        !Number.isInteger(l.cantidad) ||
        l.cantidad < 1 ||
        l.cantidad > 1000000,
    )
  )
    throw new Error("Proveedor o detalle inválido");
  return {
    proveedorId,
    condicionesPactadas,
    detalles: lineas.map((l) => ({
      articuloId: l.articuloId!,
      cantidad: l.cantidad,
    })),
  };
}
