package pe.edu.upeu.bomerp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pe.edu.upeu.bomerp.administracion.model.*;
import pe.edu.upeu.bomerp.administracion.repository.*;
import pe.edu.upeu.bomerp.articulo.model.*;
import pe.edu.upeu.bomerp.articulo.repository.*;
import pe.edu.upeu.bomerp.personas.persona.entity.*;
import pe.edu.upeu.bomerp.personas.persona.repository.PersonaRepository;
import pe.edu.upeu.bomerp.produccion.model.*;
import pe.edu.upeu.bomerp.produccion.service.*;
import pe.edu.upeu.bomerp.inventario.model.*;
import pe.edu.upeu.bomerp.inventario.service.*;
import pe.edu.upeu.bomerp.compras.ordencompra.dto.*;
import pe.edu.upeu.bomerp.compras.ordencompra.service.OrdenCompraService;
import pe.edu.upeu.bomerp.ventas.web.dto.PedidoRequest;
import pe.edu.upeu.bomerp.ventas.service.PedidoService;
import pe.edu.upeu.bomerp.ventas.repository.PedidoRepository;
import pe.edu.upeu.bomerp.ventas.service.CotizacionVentaService;
import pe.edu.upeu.bomerp.ventas.web.dto.CrearCotizacionRequest;
import pe.edu.upeu.bomerp.ventas.web.dto.ConvertirCotizacionRequest;



@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional class IntegracionTest {
@Autowired MockMvc mvc;@Autowired EmpresaRepository empresas;@Autowired SucursalRepository sucursales;@Autowired AlmacenRepository almacenes;@Autowired UnidadMedidaRepository unidades;@Autowired ArticuloRepository articulos;@Autowired PersonaRepository personas;@Autowired FormulaService formulas;@Autowired OrdenProduccionService produccion;@Autowired InventarioService inventario;@Autowired OrdenCompraService compras;@Autowired PedidoService ventas;@Autowired CotizacionVentaService cotizaciones;@Autowired RolRepository roles;
Long almacen;Articulo maiz,terminado;pe.edu.upeu.bomerp.personas.persona.entity.Persona proveedor,cliente;Formula formula;
@BeforeEach void datos(){var e=new Empresa();e.setRuc("20111111111");e.setRazonSocial("Prueba");
empresas.save(e);var s=new Sucursal();s.setEmpresa(e);s.setNombre("Planta");
sucursales.save(s);var a=new Almacen();a.setSucursal(s);a.setNombre("General");
a.setTipo(Almacen.TipoAlmacen.GENERAL);almacen=almacenes.save(a).getId();var u=new UnidadMedida();
u.setCodigo("KG");u.setNombre("Kilogramo");unidades.save(u);
maiz=articulo("MAIZ",Articulo.TipoArticulo.MATERIA_PRIMA,u);
terminado=articulo("PURINA",Articulo.TipoArticulo.PRODUCTO_TERMINADO,u);
proveedor=persona("111",RolPersona.PROVEEDOR);
cliente=persona("222",RolPersona.CLIENTE);var 
f=new Formula();f.setNombre("Receta");f.setCodigoProductoTerminado("PURINA");
var d=new FormulaDetalle();d.setCodigoArticulo("MAIZ");d.setUnidadMedida("KG");
d.setCantidadPorUnidad(BigDecimal.ONE);f.agregarDetalle(d);formula=formulas.crear(f);}
Articulo articulo(String codigo,Articulo.TipoArticulo tipo,UnidadMedida u){var a=new Articulo();a.setCodigo(codigo);a.setNombre(codigo);a.setUnidadMedida(u);a.setTipoArticulo(tipo);a.setCostoReferencial(new BigDecimal("2.50"));return articulos.save(a);}
pe.edu.upeu.bomerp.personas.persona.entity.Persona persona(String doc,RolPersona rol){var p=new pe.edu.upeu.bomerp.personas.persona.entity.Persona();p.setTipoDocumento(TipoDocumento.RUC);p.setNumeroDocumento(doc);p.setNombreORazonSocial(rol.name());p.setRoles(Set.of(rol));return personas.save(p);}
MovimientoInventario recepcion(){var c=compras.crearOrden(new OrdenCompraRequest(proveedor.getId(),"Contado",List.of(new DetalleOrdenRequest(maiz.getId(),100))),"subject-real");var d=new DetalleMovimiento();d.setCodigoArticulo("MAIZ");d.setCantidad(new BigDecimal("100"));d.setUnidadMedida("KG");return inventario.registrarRecepcion(almacen,c.getId(),new BigDecimal("100"),BigDecimal.ZERO,BigDecimal.ZERO,"Prueba",List.of(d));}
@Test void compraProduccionVentaIntegradas(){var r=recepcion();assertThat(inventario.consultarDisponible(almacen,"MAIZ")).isEqualByComparingTo("0");inventario.confirmarRecepcion(r.getId(),true);assertThat(inventario.consultarDisponible(almacen,"MAIZ")).isEqualByComparingTo("100");var o=produccion.planificar(formula.getId(),almacen,almacen,new BigDecimal("20"));produccion.iniciar(o.getId());produccion.registrarConsumo(o.getId(),"MAIZ",null,new BigDecimal("20"),"KG");produccion.registrarResultado(o.getId(),ProductoResultante.TipoResultado.TERMINADO,"PURINA",new BigDecimal("19"),"KG",null);produccion.registrarResultado(o.getId(),ProductoResultante.TipoResultado.MERMA,"PURINA",BigDecimal.ONE,"KG",null);produccion.cerrar(o.getId());assertThat(inventario.consultarDisponible(almacen,"MAIZ")).isEqualByComparingTo("80");assertThat(inventario.consultarDisponible(almacen,"PURINA")).isEqualByComparingTo("19");var p=ventas.crear(new PedidoRequest(cliente.getId(),almacen,List.of(new PedidoRequest.Linea("PURINA",new BigDecimal("5"),new BigDecimal("10"),new BigDecimal("10")))),"usuario-sub");assertThat(p.getTotal()).isEqualByComparingTo("45");ventas.aprobar(p.getId());ventas.entregar(p.getId(),"ABC-123");assertThat(inventario.consultarDisponible(almacen,"PURINA")).isEqualByComparingTo("14");}
@Test void noIniciaSinStock(){var o=produccion.planificar(formula.getId(),almacen,almacen,BigDecimal.TEN);assertThat(produccion.iniciar(o.getId()).getEstado()).isEqualTo(OrdenProduccion.EstadoOrden.EN_ESPERA);}
@Test void rechazarRecepcionNoGeneraStock(){var r=recepcion();inventario.confirmarRecepcion(r.getId(),false);assertThat(inventario.consultarDisponible(almacen,"MAIZ")).isEqualByComparingTo("0");}
@Test void noDuplicaRecepcion(){var r=recepcion();inventario.confirmarRecepcion(r.getId(),true);assertThatThrownBy(()->inventario.confirmarRecepcion(r.getId(),true)).isInstanceOf(IllegalStateException.class);}
@Test void loteInsuficienteNoPuedeConsumirse(){var r=recepcion();inventario.confirmarRecepcion(r.getId(),true);var o=produccion.planificar(formula.getId(),almacen,almacen,BigDecimal.TEN);produccion.iniciar(o.getId());assertThatThrownBy(()->produccion.registrarConsumo(o.getId(),"MAIZ",null,new BigDecimal("101"),"KG")).isInstanceOf(IllegalStateException.class);}
@Test void apiSegura401y403()throws Exception{mvc.perform(get("/api/produccion/ordenes")).andExpect(status().isUnauthorized());mvc.perform(get("/api/produccion/ordenes").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_VENDEDOR")))).andExpect(status().isForbidden());mvc.perform(get("/api/produccion/ordenes").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OPERARIO_PLANTA")))).andExpect(status().isOk());mvc.perform(get("/api/articulos/catalogo-publico")).andExpect(status().isOk()).andExpect(jsonPath("$[0].costoReferencial").doesNotExist());}
@Test void relacionesInvalidasDevuelven400()throws Exception{mvc.perform(post("/api/gestion/sucursales").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))).contentType("application/json").content("{\"nombre\":\"Sede\",\"empresa\":{\"id\":999999}}" )).andExpect(status().isBadRequest());}
@Test void serializaCabeceraDetalleSinCiclos()throws Exception{var o=produccion.planificar(formula.getId(),almacen,almacen,BigDecimal.TEN);mvc.perform(get("/api/produccion/ordenes/"+o.getId()).with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))).andExpect(status().isOk()).andExpect(jsonPath("$.formula.detalles[0].codigoArticulo").value("MAIZ"));}
@Test void cotizacionAceptadaSeConvierteUnaSolaVezEnPedido(){var request=new CrearCotizacionRequest(cliente.getId(),List.of(new CrearCotizacionRequest.Linea("PURINA",new BigDecimal("3"),new BigDecimal("10"),new BigDecimal("10"))));var cotizacion=cotizaciones.crear(request);assertThat(cotizacion.getTotal()).isEqualByComparingTo("27");cotizaciones.enviar(cotizacion.getId());cotizaciones.aceptar(cotizacion.getId());var pedido=cotizaciones.convertirEnPedido(cotizacion.getId(),new ConvertirCotizacionRequest(almacen),"usuario-sub");assertThat(pedido.getCotizacionVentaId()).isEqualTo(cotizacion.getId());assertThat(pedido.getDetalles()).hasSize(1);assertThat(pedido.getTotal()).isEqualByComparingTo("27");assertThatThrownBy(()->cotizaciones.convertirEnPedido(cotizacion.getId(),new ConvertirCotizacionRequest(almacen),"usuario-sub")).isInstanceOf(IllegalStateException.class);}
}

