import { Component, computed, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { FormArray, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { Auth } from '../auth';
import { mensaje } from '../api';

type Linea = { codigoArticulo: string; cantidad: number; precio: number; descuento: number };
type Persona = { id: number; tipoDocumento: string; numeroDocumento: string; nombreORazonSocial: string; direccion?: string; telefono?: string; email?: string; roles: string[]; activo: boolean };

@Component({
  imports: [ReactiveFormsModule, FormsModule, CurrencyPipe, DatePipe],
  templateUrl: './ventas.html',
  styles: [`.sales-line{flex-wrap:wrap;align-items:flex-end}.sales-line>label{min-width:120px}.sales-line-info{width:100%;display:flex;gap:16px;justify-content:space-between;flex-wrap:wrap;border-top:1px solid var(--border);padding:10px 0}.sales-actions{display:flex;gap:6px;flex-wrap:wrap}.status-approved{background:#fff1c7}.status-dispatched{background:#dceddf}.status-cancelled{background:#f7dfdd}.sales-tabs{display:flex;gap:8px;flex-wrap:wrap;margin:20px 0}.sales-tabs button.active{background:var(--accent);color:#fff}.pos-products{display:grid;grid-template-columns:repeat(auto-fill,minmax(165px,1fr));gap:12px}.pos-products button{display:grid;text-align:left;min-height:110px;gap:8px;background:var(--surface-soft);border:1px solid var(--border)}.pos-products button strong{font-size:14px}.receipt{max-width:460px;margin:auto;padding:24px;border:1px dashed #8c9988;background:#fff}.receipt h2,.receipt p{text-align:center}.receipt-row{display:flex;justify-content:space-between;gap:12px;border-bottom:1px solid #edf0e9;padding:8px 0}.report-filters{display:flex;gap:12px;align-items:end;flex-wrap:wrap}.report-filters label{min-width:150px}.quick-client{display:grid;grid-template-columns:1fr auto;gap:12px;align-items:end}.quick-client label{margin:0}@media(max-width:600px){.quick-client{grid-template-columns:1fr}.sales-tabs button{flex:1}}`],
})
export class Ventas {
  http = inject(HttpClient); fb = inject(FormBuilder); auth = inject(Auth);
  clientes = signal<Persona[]>([]); personasTodas = signal<Persona[]>([]); articulos = signal<any[]>([]); almacenes = signal<any[]>([]);
  pedidos = signal<any[]>([]); cotizaciones = signal<any[]>([]); contactos = signal<any[]>([]);
  error = signal(''); aviso = signal(''); ocupado = signal(false); seccion = signal<'pedidos'|'cotizaciones'|'rapida'|'historial'>('rapida');
  busquedaCliente = signal(''); busquedaArticulo = signal(''); busquedaHistorial = signal(''); filtroEstado = signal('');
  dni = ''; nombreDni = signal(''); dniConsultando = signal(false); clienteDniId = signal<number|null>(null); documentoConsultado = signal('');
  almacenMostrador: number|null = null; vehiculo = ''; desde = ''; hasta = '';
  reporte = signal<any>({}); ticketActual = signal<any|null>(null); detalleActual = signal<any|null>(null);
  stockDisponible = signal<Record<string, number>>({}); stockCargando = signal<Record<string, boolean>>({});

  form = this.crearFormulario(true);
  rapido = this.crearFormulario(true);
  cotizacionForm = this.crearFormulario(false);
  get filas(): FormArray { return this.form.controls.detalles as FormArray; }
  get filasRapidas(): FormArray { return this.rapido.controls.detalles as FormArray; }
  get filasCotizacion(): FormArray { return this.cotizacionForm.controls.detalles as FormArray; }

  clientesFiltrados = computed(() => { const q=this.busquedaCliente().trim().toLowerCase(); return this.clientes().filter(c=>!q||`${c.nombreORazonSocial} ${c.numeroDocumento}`.toLowerCase().includes(q)); });
  articulosFiltrados = computed(() => { const q=this.busquedaArticulo().trim().toLowerCase(); return this.articulos().filter(a=>!q||`${a.nombre} ${a.codigo}`.toLowerCase().includes(q)); });
  pedidosFiltrados = computed(() => { const q=this.busquedaHistorial().trim().toLowerCase(); return this.pedidos().filter(p=>(!this.filtroEstado()||p.estado===this.filtroEstado())&&(!q||`${p.id} ${this.nombreCliente(p.clienteId)} ${p.detalles?.map((d:any)=>d.codigoArticulo).join(' ')}`.toLowerCase().includes(q))); });

  private route = inject(ActivatedRoute);
  constructor(){this.route.data.subscribe(data=>this.seccion.set((data['vista']||'rapida') as 'pedidos'|'cotizaciones'|'rapida'|'historial'));this.agregar();this.agregarRapida();this.agregarCotizacion();this.cargar();}
  tituloSeccion(){return this.seccion()==='rapida'?'Nueva venta':this.seccion()==='cotizaciones'?'Cotizaciones':this.seccion()==='pedidos'?'Pedidos':'Consultar ventas';}
  descripcionSeccion(){return this.seccion()==='rapida'?'Registra una compra rápida de mostrador.':this.seccion()==='cotizaciones'?'Prepara y da seguimiento a cotizaciones de clientes.':this.seccion()==='pedidos'?'Registra pedidos para clientes y almacenes.':'Revisa el historial, imprime comprobantes y consulta reportes.';}
  private crearFormulario(conAlmacen:boolean){return this.fb.group({clienteId:[null as number|null,Validators.required],almacenId:[null as number|null,conAlmacen?Validators.required:[]],detalles:this.fb.array([] as FormGroup[],Validators.required)});}
  private crearLinea(){return this.fb.group({codigoArticulo:['',Validators.required],cantidad:[1,[Validators.required,Validators.min(0.0001)]],precio:[null as number|null,[Validators.required,Validators.min(0.01)]],descuento:[0,[Validators.required,Validators.min(0),Validators.max(100)]]});}
  agregar(){this.filas.push(this.crearLinea());} agregarRapida(){this.filasRapidas.push(this.crearLinea());} agregarCotizacion(){this.filasCotizacion.push(this.crearLinea());}
  cargar(){forkJoin({p:this.http.get<Persona[]>('/api/v1/personas'),a:this.http.get<any[]>('/api/articulos'),s:this.http.get<any[]>('/api/gestion/almacenes'),v:this.http.get<any[]>('/api/ventas/pedidos'),c:this.http.get<any[]>('/api/ventas/contactos'),q:this.http.get<any[]>('/api/ventas/cotizaciones')}).subscribe({next:r=>{this.personasTodas.set(r.p);this.clientes.set(r.p.filter(p=>p.activo&&p.roles?.includes('CLIENTE')));this.articulos.set(r.a.filter(a=>a.activo&&a.tipoArticulo==='PRODUCTO_TERMINADO'));this.almacenes.set(r.s);this.pedidos.set(r.v);this.contactos.set(r.c);this.cotizaciones.set(r.q);this.cargarReporte();},error:e=>this.error.set(mensaje(e))});}
  cargarReporte(){const params:any={};if(this.desde)params.desde=this.desde;if(this.hasta)params.hasta=this.hasta;this.http.get<any>('/api/ventas/reportes',{params}).subscribe({next:r=>this.reporte.set(r),error:e=>this.error.set(mensaje(e))});}

  seleccionarArticulo(i:number, form:'pedido'|'rapida'|'cotizacion'='pedido'){const array=this.arrayPara(form);const fila=array.at(i);const a=this.articulos().find(x=>x.codigo===fila.get('codigoArticulo')?.value);fila.get('precio')?.setValue(a?.precioVenta??null);if(form==='rapida'&&a&&this.almacenMostrador)this.cargarStock(a.codigo);this.error.set('');}
  private arrayPara(form:string):FormArray{return form==='rapida'?this.filasRapidas:form==='cotizacion'?this.filasCotizacion:this.filas;}
  quitar(i:number,form:'pedido'|'rapida'|'cotizacion'='pedido'){const a=this.arrayPara(form);if(a.length>1)a.removeAt(i);else a.at(0).reset({codigoArticulo:'',cantidad:1,precio:null,descuento:0});}
  precioSinConfigurar(codigo:string|null|undefined){return !!codigo&&this.articulos().find(a=>a.codigo===codigo)?.precioVenta==null;}
  subtotal(d:any){return Number(d.cantidad||0)*Number(d.precio||0)*(1-Number(d.descuento||0)/100);}
  total(form:'pedido'|'rapida'|'cotizacion'='pedido'){return this.arrayPara(form).controls.reduce((s:number,c:any)=>s+this.subtotal(c.getRawValue()),0);}
  private lineasValidas(f:FormGroup, a:FormArray){f.markAllAsTouched();return f.valid&&a.length>0&&!a.controls.some((c:any)=>this.precioSinConfigurar(c.value.codigoArticulo));}

  guardarPedido(){this.error.set('');if(!this.lineasValidas(this.form,this.filas)){this.error.set('Completa cliente, almacén y líneas válidas. Revisa cantidades, precios y descuentos.');return;}this.post('/api/ventas/pedidos',this.form.getRawValue(),'Pedido guardado como borrador.',()=>{this.form=this.crearFormulario(true);this.agregar();});}
  guardarCotizacion(){this.error.set('');if(!this.lineasValidas(this.cotizacionForm,this.filasCotizacion)){this.error.set('Completa el cliente y las líneas con precios válidos.');return;}this.post('/api/ventas/cotizaciones',{clienteId:this.cotizacionForm.value.clienteId,detalles:this.filasCotizacion.getRawValue().map((d:any)=>({codigoArticulo:d.codigoArticulo,cantidad:d.cantidad,precioUnitario:d.precio,descuento:d.descuento}))},'Cotización creada como borrador.',()=>{this.cotizacionForm=this.crearFormulario(false);this.agregarCotizacion();});}
  enviarCotizacion(id:number,accion:'enviar'|'aceptar'|'rechazar'){this.post(`/api/ventas/cotizaciones/${id}/${accion}`,{},`Cotización ${accion==='enviar'?'enviada':accion==='aceptar'?'aceptada':'rechazada'}.`);}
  convertirCotizacion(c:any){if(!this.almacenMostrador){this.error.set('Selecciona el almacén para preparar el pedido.');return;}this.post(`/api/ventas/cotizaciones/${c.id}/pedido`,{almacenId:this.almacenMostrador},'Cotización convertida en pedido.');}

  consultarDni(){const numero=this.dni.trim();if(!/^\d{8}$/.test(numero)){this.error.set('Escribe un DNI de 8 dígitos.');return;}this.error.set('');this.dniConsultando.set(true);this.documentoConsultado.set('');this.clienteDniId.set(null);this.http.get<any>('/api/v1/personas/consulta-documento',{params:{tipo:'DNI',numero}}).subscribe({next:r=>{const existente=this.clientes().find(p=>p.numeroDocumento===numero);this.nombreDni.set(r.nombreORazonSocial||'');this.documentoConsultado.set(numero);if(existente){this.clienteDniId.set(existente.id);this.nombreDni.set(existente.nombreORazonSocial);}this.dniConsultando.set(false);},error:e=>{this.dniConsultando.set(false);this.error.set(e.status===0?'No se pudo consultar el servicio. Puedes completar el nombre manualmente y continuar.':mensaje(e));}});}
  completarClienteYVender(){this.error.set('');if(!/^\d{8}$/.test(this.dni.trim())){this.error.set('La venta rápida requiere un DNI válido de 8 dígitos.');return;}if(!this.almacenMostrador){this.error.set('Selecciona el almacén de productos terminados.');return;}if(!this.detallesValidos(this.filasRapidas)){this.error.set('Agrega productos con cantidades y precios configurados.');return;}
    const existente=this.personasTodas().find(p=>p.numeroDocumento===this.dni.trim());
    if(this.documentoConsultado()!==this.dni.trim()&&!this.nombreDni().trim()&&!existente){this.error.set('Consulta el DNI o escribe el nombre del cliente.');return;}
    const continuar=(clienteId:number)=>{const payload={clienteId,almacenId:this.almacenMostrador,detalles:this.filasRapidas.getRawValue()};this.post('/api/ventas/pedidos/venta-rapida',payload,'Venta registrada. Se actualizó el stock.',(p)=>{this.ticketActual.set({...p,clienteNombre:this.nombreCliente(clienteId),clienteDocumento:this.dni});this.rapido=this.crearFormulario(true);this.agregarRapida();this.dni='';this.nombreDni.set('');this.clienteDniId.set(null);this.documentoConsultado.set('');});};
    if(existente){const roles=[...new Set([...(existente.roles||[]),'CLIENTE'])];if(!existente.roles?.includes('CLIENTE')||!existente.activo){this.http.put<Persona>(`/api/v1/personas/${existente.id}`,{tipoDocumento:existente.tipoDocumento,numeroDocumento:existente.numeroDocumento,nombreORazonSocial:existente.nombreORazonSocial,direccion:existente.direccion||'',telefono:existente.telefono||'',email:existente.email||'',roles,activo:true}).subscribe({next:p=>{this.actualizarPersona(p);continuar(p.id);},error:e=>this.error.set(mensaje(e))});}else continuar(existente.id);return;}
    const documento=this.dni.trim();this.http.post<Persona>('/api/v1/personas',{tipoDocumento:'DNI',numeroDocumento:documento,nombreORazonSocial:this.nombreDni().trim(),direccion:'',telefono:'',email:'',roles:['CLIENTE'],activo:true}).subscribe({next:p=>{this.personasTodas.update(v=>[...v,p]);this.clientes.update(v=>[...v,p]);continuar(p.id);},error:e=>this.error.set(this.mensajeError(e))});
  }
  cargarStock(codigo:string){if(!this.almacenMostrador)return;this.http.get<any>('/api/inventario/stock',{params:{almacenId:this.almacenMostrador,codigoArticulo:codigo}}).subscribe({next:r=>this.stockDisponible.update(v=>({...v,[codigo]:Number(r.cantidadDisponible)})),error:()=>{}});}
  cambiarAlmacenMostrador(id:number|null){this.almacenMostrador=id;this.rapido.controls.almacenId.setValue(id);this.filasRapidas.getRawValue().forEach((d:any)=>d.codigoArticulo&&this.cargarStock(d.codigoArticulo));}
  agregarProductoRapido(a:any){const fila=this.filasRapidas.controls.find((c:any)=>c.value.codigoArticulo===a.codigo);if(fila){fila.get('cantidad')?.setValue(Number(fila.value.cantidad||0)+1);return;}const nueva=this.crearLinea();nueva.patchValue({codigoArticulo:a.codigo,cantidad:1,precio:a.precioVenta??null,descuento:0});this.filasRapidas.push(nueva);if(this.almacenMostrador)this.cargarStock(a.codigo);}
  stockRapido(codigo:string){const n=this.stockDisponible()[codigo];return n===undefined?'Stock pendiente de validar':`Disponible: ${n}`;}
  private detallesValidos(a:FormArray){a.markAllAsTouched();return a.length>0&&a.controls.every((c:any)=>c.valid&&!this.precioSinConfigurar(c.value.codigoArticulo));}

  post(url:string,body:any,exito:string,alExito?:(r:any)=>void){if(this.ocupado())return;this.ocupado.set(true);this.error.set('');this.aviso.set('');this.http.post<any>(url,body).subscribe({next:r=>{this.ocupado.set(false);this.aviso.set(exito);alExito?.(r);this.cargar();},error:e=>{this.ocupado.set(false);this.error.set(this.mensajeError(e));}});}
  cancelarPedido(p:any){const motivo=prompt(`Motivo para anular el pedido #${p.id}:`);if(!motivo?.trim())return;this.post(`/api/ventas/pedidos/${p.id}/anular`,{motivo},'Pedido anulado. Se conservó el registro.');}
  revertirVenta(p:any){const motivo=prompt(`Motivo para devolver y revertir la venta #${p.id}:`);if(!motivo?.trim())return;if(!confirm('Se reingresarán todos los productos al almacén y la venta quedará como DEVUELTA. ¿Continuar?'))return;this.post(`/api/ventas/pedidos/${p.id}/revertir`,{motivo},'Venta revertida y stock reingresado.');}
  estadoPedido(e:string){return ({BORRADOR:'Borrador',APROBADO:'Aprobado',DESPACHADO:'Despachado',ENTREGADO:'Despachado',DEVUELTO:'Devuelto',ANULADO:'Anulado'} as Record<string,string>)[e]||e;}
  nombreCliente(id:number){return this.clientes().find(c=>c.id===id)?.nombreORazonSocial||`Cliente #${id}`;}
  documentoCliente(id:number){return this.clientes().find(c=>c.id===id)?.numeroDocumento||'—';}
  nombreAlmacen(id:number){return this.almacenes().find(a=>a.id===id)?.nombre||`Almacén #${id}`;}
  verDetalle(p:any){this.detalleActual.set(p);}
  actualizarPersona(p:Persona){this.personasTodas.update(v=>[...v.filter(x=>x.id!==p.id),p]);this.clientes.update(v=>[...v.filter(x=>x.id!==p.id),p]);}
  imprimir(p:any){const cliente=p.clienteNombre||this.nombreCliente(p.clienteId);this.ticketActual.set({...p,clienteNombre:cliente});document.body.classList.add('printing-ticket');window.onafterprint=()=>document.body.classList.remove('printing-ticket');setTimeout(()=>window.print(),100);}
  cerrarTicket(){this.ticketActual.set(null);}
  private mensajeError(e:any){const d=e?.error?.detail||e?.error?.message||'';if(e.status===0)return 'No se pudo conectar con el servidor. Los datos de la venta siguen en pantalla.';if(e.status===401)return 'La sesión venció. Vuelve a iniciar sesión.';if(e.status===403)return 'Tu rol no tiene permiso para realizar esta acción.';if(/stock insuficiente/i.test(d))return `${d}. Revisa el almacén y las cantidades.`;return d||mensaje(e);}
}
