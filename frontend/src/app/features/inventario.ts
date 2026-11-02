import { Component, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { forkJoin } from 'rxjs';
import { mensaje } from '../api';

@Component({
  imports: [FormsModule],
  templateUrl: './inventario.html',
  styleUrl: './inventario.css',
  styles: [`.dispatch-order-lines{display:flex;gap:6px;flex-wrap:wrap;grid-column:1/-1}.dispatch-order>strong{align-self:center}`],
})
export class Inventario {
  seccion = signal('recepciones');
  cargando = signal(false);
  busqueda = '';
  filtroAlmacen = '';
  filtroTipo = '';
  readonly apartados = [
    { id: 'recepciones', nombre: 'Recepciones', detalle: 'Entradas de compras' },
    { id: 'existencias', nombre: 'Existencias y lotes', detalle: 'Disponibilidad por almacén' },
    { id: 'despachos', nombre: 'Despachos', detalle: 'Entregas de ventas' },
    { id: 'historial', nombre: 'Historial', detalle: 'Trazabilidad de movimientos' },
  ];
  http = inject(HttpClient);
  route = inject(ActivatedRoute);
  compras = signal<any[]>([]);
  articulos = signal<any[]>([]);
  almacenes = signal<any[]>([]);
  movimientos = signal<any[]>([]);
  lotes = signal<any[]>([]);
  pedidos = signal<any[]>([]);
  pedidoDestinoId = signal<number | null>(null);
  error = signal('');
  exito = signal('');
  ocupado = signal(false);
  seleccion: Record<string, number> = {};
  compraId = 0;
  almacenId = 0;
  pesajeKg = 1;
  humedadPct = 0;
  impurezaPct = 0;

  constructor() {
    this.route.queryParamMap.subscribe(params => {
      if (params.get('seccion') === 'despachos') this.seccion.set('despachos');
      const id = Number(params.get('pedidoId'));
      this.pedidoDestinoId.set(Number.isInteger(id) && id > 0 ? id : null);
      this.enfocarPedidoDestino();
    });
    this.cargar();
  }

  private enfocarPedidoDestino() {
    const id = this.pedidoDestinoId();
    if (id) setTimeout(() => document.getElementById(`pedido-despacho-${id}`)?.scrollIntoView({ behavior: 'smooth', block: 'start' }), 300);
  }

  cargar() {
    this.cargando.set(true);
    forkJoin({
      c: this.http.get<any[]>('/api/v1/ordenes-compra'),
      a: this.http.get<any[]>('/api/articulos'),
      s: this.http.get<any[]>('/api/gestion/almacenes'),
      m: this.http.get<any[]>('/api/inventario/movimientos'),
      l: this.http.get<any[]>('/api/reportes/stock'),
      v: this.http.get<any[]>('/api/ventas/pedidos'),
    }).subscribe({
      next: r => {
        this.cargando.set(false);
        this.compras.set(r.c.filter(c => c.estado === 'EMITIDA'));
        this.articulos.set(r.a);
        this.almacenes.set(r.s.filter(a => a.activo !== false));
        this.movimientos.set(r.m);
        this.lotes.set(r.l);
        this.pedidos.set(r.v.filter(p => p.estado === 'APROBADO'));
        this.enfocarPedidoDestino();
        this.error.set('');
      },
      error: e => { this.cargando.set(false); this.error.set(mensaje(e)); },
    });
  }

  recepciones() { return this.movimientos().filter(m => m.tipoMovimiento === 'RECEPCION').sort((a, b) => b.id - a.id); }
  pendientes() { return this.recepciones().filter(m => m.estado === 'BORRADOR'); }
  comprasDisponibles() { return this.compras().filter(c => !this.pendientes().some(m => m.ordenCompraId === c.id)); }
  lotesActivos() { return this.lotes().filter(l => l.estado === 'DISPONIBLE' && Number(l.cantidadDisponible) > 0); }
  articulo(codigo: string) { return this.articulos().find(a => a.codigo === codigo)?.nombre || codigo; }
  unidad(codigo: string) { return this.articulos().find(a => a.codigo === codigo)?.unidadMedida?.codigo || ''; }
  unidadPorId(id: number) { return this.articulos().find(a => a.id === id)?.unidadMedida?.codigo || ''; }
  cantidad(n: any) { return new Intl.NumberFormat('es-PE', { maximumFractionDigits: 4 }).format(Number(n || 0)); }
  fecha(valor: string) {
    if (!valor) return 'Sin fecha';
    return new Intl.DateTimeFormat('es-PE', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(valor));
  }
  estado(m: any) {
    if (m.estado === 'ANULADO') return 'Anulado';
    if (m.estado === 'BORRADOR') return m.tipoMovimiento === 'RECEPCION' ? 'Pendiente de revisión' : 'Pendiente de entrega';
    if (m.tipoMovimiento === 'RECEPCION') return m.aceptado === true ? 'Aceptada · ingresó al stock' : m.aceptado === false ? 'Rechazada · sin ingreso' : 'Confirmada';
    return m.tipoMovimiento === 'DESPACHO' ? 'Entrega confirmada' : 'Devolución registrada';
  }
  tono(m: any) { return m.estado === 'ANULADO' || m.aceptado === false ? 'rechazado' : m.estado === 'BORRADOR' ? 'pendiente' : 'aceptado'; }
  tipo(m: any) { return ({ RECEPCION: 'Recepción de compra', DESPACHO: 'Despacho de venta', DEVOLUCION: 'Devolución de venta' } as Record<string, string>)[m.tipoMovimiento] || m.tipoMovimiento; }
  lotesFiltrados() {
    const q = this.busqueda.trim().toLocaleLowerCase();
    return this.lotes().filter(l => (!this.filtroAlmacen || String(l.almacenId) === this.filtroAlmacen)
      && (!q || [l.id, l.codigoArticulo, this.articulo(l.codigoArticulo)].join(' ').toLocaleLowerCase().includes(q)))
      .sort((a, b) => b.id - a.id);
  }
  historial() { return this.movimientos().filter(m => !this.filtroTipo || m.tipoMovimiento === this.filtroTipo).sort((a, b) => b.id - a.id); }

  detalles() { return this.compras().find(c => c.id === +this.compraId)?.detalles || []; }

  cambiarCompra(id: number) {
    this.compraId = Number(id);
    this.almacenId = 0;
    const opciones = this.almacenesCompatiblesRecepcion();
    if (opciones.length === 1) this.almacenId = opciones[0].id;
  }

  almacenesCompatiblesRecepcion() {
    const tipos = new Set(this.detalles().map((d: any) => {
      const tipo = this.articulos().find(a => a.id === d.articuloId)?.tipoArticulo;
      return this.tipoAlmacen(tipo);
    }).filter(Boolean));
    if (!tipos.size) return this.almacenes();
    return this.almacenes().filter(a => a.tipo === 'GENERAL' || (tipos.size === 1 && tipos.has(a.tipo)));
  }

  compraMezclaTipos() {
    const tipos = this.detalles().map((d: any) => this.articulos().find(a => a.id === d.articuloId)?.tipoArticulo).filter(Boolean);
    return new Set(tipos).size > 1;
  }

  private tipoAlmacen(tipo: string | undefined) {
    return ({
      MATERIA_PRIMA: 'MATERIA_PRIMA',
      PRODUCTO_TERMINADO: 'PRODUCTO_TERMINADO',
      INSUMO: 'INSUMOS',
      EMPAQUE: 'EMPAQUE',
      SUBPRODUCTO: 'GENERAL',
    } as Record<string, string>)[tipo || ''] || null;
  }

  registrar() {
    const detalles = this.detalles().map((d: any) => {
      const a = this.articulos().find(x => x.id === d.articuloId);
      return { codigoArticulo: a?.codigo, cantidad: d.cantidad, unidadMedida: a?.unidadMedida.codigo };
    });
    this.enviar('/api/inventario/recepciones', {
      ordenCompraId: +this.compraId, almacenId: +this.almacenId,
      pesajeKg: this.pesajeKg, humedadPct: this.humedadPct,
      impurezaPct: this.impurezaPct, detalles,
    }, 'Recepción registrada. Confirma el control de calidad para ingresar el stock.');
  }

  enviar(url: string, body: any, mensajeExito: string) {
    if (this.ocupado()) return;
    this.ocupado.set(true);
    this.error.set('');
    this.exito.set('');
    this.http.post(url, body).subscribe({
      next: () => {
        this.ocupado.set(false);
        this.error.set('');
        this.exito.set(mensajeExito);
        if (url.endsWith('/recepciones')) { this.compraId = 0; this.almacenId = 0; }
        this.seleccion = {};
        this.cargar();
      },
      error: e => { this.ocupado.set(false); this.error.set(mensaje(e)); },
    });
  }

  lineasPedido(p: any) {
    const grupos = new Map<string, any>();
    for (const d of p.detalles || []) {
      const almacenId = Number(d.almacenId || p.almacenId);
      const clave = `${almacenId}:${d.codigoArticulo}`;
      const actual = grupos.get(clave);
      if (actual) actual.cantidad += Number(d.cantidad);
      else grupos.set(clave, { clave, almacenId, codigoArticulo: d.codigoArticulo, cantidad: Number(d.cantidad) });
    }
    return [...grupos.values()];
  }
  lotesPedido(p: any, linea?: any) {
    const lineas = linea ? [linea] : this.lineasPedido(p);
    const claves = new Set(lineas.map(l => `${l.almacenId}:${l.codigoArticulo}`));
    return this.lotes().filter(l => l.estado === 'DISPONIBLE' && Number(l.cantidadDisponible) > 0
      && claves.has(`${l.almacenId}:${l.codigoArticulo}`)).sort((a, b) => a.id - b.id);
  }
  cantidadSeleccionada(p: any, linea: any) {
    return this.lotesPedido(p, linea).reduce((suma, lote) => suma + Number(this.seleccion[`${p.id}:${lote.id}`] || 0), 0);
  }
  cantidadPendiente(p: any, linea: any) { return Math.max(0, Number(linea.cantidad) - this.cantidadSeleccionada(p, linea)); }
  esSeleccionExacta(p: any) {
    const lineas = this.lineasPedido(p);
    return lineas.length > 0 && lineas.every(linea => {
      const lotes = this.lotesPedido(p, linea);
      const valoresValidos = lotes.every(lote => {
        const cantidad = Number(this.seleccion[`${p.id}:${lote.id}`] || 0);
        return Number.isFinite(cantidad) && cantidad >= 0
          && cantidad <= Number(lote.cantidadDisponible)
          && Math.abs(cantidad * 10000 - Math.round(cantidad * 10000)) < 0.000001;
      });
      return valoresValidos
        && Math.round(this.cantidadSeleccionada(p, linea) * 10000) === Math.round(Number(linea.cantidad) * 10000);
    });
  }
  entregar(p: any) {
    if (!this.esSeleccionExacta(p)) { this.error.set('Las cantidades seleccionadas por artículo y almacén deben coincidir con el pedido.'); return; }
    const detalles = this.lotesPedido(p).filter(l => Number(this.seleccion[`${p.id}:${l.id}`]) > 0).map(l => ({
      codigoArticulo: l.codigoArticulo,
      loteId: l.id,
      cantidad: Number(this.seleccion[`${p.id}:${l.id}`]),
    }));
    this.enviar('/api/ventas/pedidos/' + p.id + '/entregar',
      { tipoEntrega: 'LOCAL', vehiculo: null, detalles },
      `Pedido #${p.id} entregado. Se descontaron los lotes seleccionados.`);
  }
  confirmar(id: number, aceptado: boolean) {
    this.enviar('/api/inventario/recepciones/' + id + '/confirmar', { aceptado },
      aceptado ? 'Control aceptado: se ingresó el stock y se generaron los lotes.' : 'Recepción rechazada; no se ingresó stock.');
  }
  nombreAlmacen(id: number) { return this.almacenes().find(a => a.id === id)?.nombre || `Almacén #${id}`; }
  almacen(id: number) { return this.nombreAlmacen(id); }
  almacenesPedido(p: any) {
    const ids = [...new Set((p.detalles || []).map((d: any) => d.almacenId || p.almacenId).filter(Boolean))] as number[];
    return ids.map(id => this.nombreAlmacen(id)).join(', ') || '—';
  }
}
