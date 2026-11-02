# Inventario: recepción, lotes y despacho

Almacén registra y evalúa recepciones de compras. Al aceptar una recepción, Inventario crea los lotes; los lotes rechazados no ingresan al stock. Para un pedido aprobado, el responsable de Almacén selecciona los lotes y las cantidades que se entregarán.

## Despacho desde un pedido

`POST /api/ventas/pedidos/{id}/entregar` requiere el permiso `INVENTARIO` y recibe, por ejemplo:

```json
{
  "tipoEntrega": "VEHICULO",
  "vehiculo": "ABC-123",
  "detalles": [
    { "codigoArticulo": "MAIZ-01", "loteId": 21, "cantidad": 5.5 }
  ]
}
```

Para entrega local se envía `tipoEntrega: "LOCAL"` y `vehiculo: null`. En ambos casos, cada detalle debe indicar artículo, lote y cantidad. El backend verifica que el pedido esté aprobado, que el lote esté disponible y corresponda al artículo y almacén asignados a esa línea, que no se retire más del saldo del lote y que las cantidades seleccionadas sumen exactamente el pedido por artículo y almacén. Los pedidos con artículos de varios almacenes generan un movimiento por almacén.

El pedido, los movimientos y el descuento de lotes se procesan dentro de una transacción: un error revierte el despacho completo. La venta rápida mantiene su selección automática de lotes.

## Relaciones

`MovimientoInventario` contiene varios `DetalleMovimiento`; cada detalle identifica un artículo, cantidad, unidad y lote. `DetalleMovimiento.codigoArticulo` conserva el contrato actual de la API y se relaciona con `Articulo.codigo` mediante una clave foránea. Esta asociación no se serializa dentro de la respuesta JSON.
