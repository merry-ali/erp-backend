package pe.edu.upeu.bomerp;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bomerp.administracion.model.Almacen;
import pe.edu.upeu.bomerp.administracion.model.Empresa;
import pe.edu.upeu.bomerp.administracion.model.Sucursal;
import pe.edu.upeu.bomerp.administracion.repository.AlmacenRepository;
import pe.edu.upeu.bomerp.administracion.repository.EmpresaRepository;
import pe.edu.upeu.bomerp.administracion.repository.SucursalRepository;
import pe.edu.upeu.bomerp.articulo.model.Articulo;
import pe.edu.upeu.bomerp.articulo.model.UnidadMedida;
import pe.edu.upeu.bomerp.articulo.repository.ArticuloRepository;
import pe.edu.upeu.bomerp.articulo.repository.UnidadMedidaRepository;
import pe.edu.upeu.bomerp.personas.persona.entity.Persona;
import pe.edu.upeu.bomerp.personas.persona.entity.RolPersona;
import pe.edu.upeu.bomerp.personas.persona.entity.TipoDocumento;
import pe.edu.upeu.bomerp.personas.persona.repository.PersonaRepository;
import pe.edu.upeu.bomerp.produccion.model.Formula;
import pe.edu.upeu.bomerp.produccion.model.FormulaDetalle;
import pe.edu.upeu.bomerp.produccion.repository.FormulaRepository;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Profile("demo")
@RequiredArgsConstructor
public class DemoData implements CommandLineRunner {

    private final EmpresaRepository empresas;
    private final SucursalRepository sucursales;
    private final AlmacenRepository almacenes;
    private final UnidadMedidaRepository unidades;
    private final ArticuloRepository articulos;
    private final PersonaRepository personas;
    private final FormulaRepository formulas;

    @Override
    @Transactional
    public void run(String... args) {
        if (empresas.count() == 0) {
            crearEmpresaDemostrativa();
        }

        Map<String, UnidadMedida> unidadesCatalogo = cargarUnidades();
        depurarCatalogoDemo();
        cargarArticulos(unidadesCatalogo);

        if (formulas.count() == 0) {
            crearFormulaInicial();
        }
    }

    private void crearEmpresaDemostrativa() {
        var empresa = new Empresa();
        empresa.setRuc("20123456789");
        empresa.setRazonSocial("Empresa demostrativa de producción y comercialización");
        empresas.save(empresa);

        var sucursal = new Sucursal();
        sucursal.setEmpresa(empresa);
        sucursal.setNombre("Planta principal — demostración");
        sucursales.save(sucursal);

        for (var nombre : List.of("Materias primas", "Productos terminados")) {
            var almacen = new Almacen();
            almacen.setSucursal(sucursal);
            almacen.setNombre(nombre);
            almacen.setTipo(nombre.startsWith("Materias")
                    ? Almacen.TipoAlmacen.MATERIA_PRIMA
                    : Almacen.TipoAlmacen.PRODUCTO_TERMINADO);
            almacenes.save(almacen);
        }

        for (var rol : List.of(RolPersona.PROVEEDOR, RolPersona.CLIENTE)) {
            var persona = new Persona();
            persona.setTipoDocumento(TipoDocumento.RUC);
            persona.setNumeroDocumento(rol == RolPersona.PROVEEDOR ? "20999999991" : "20999999992");
            persona.setNombreORazonSocial(rol + " de demostración");
            persona.setRoles(Set.of(rol));
            personas.save(persona);
        }
    }

    private Map<String, UnidadMedida> cargarUnidades() {
        var unidadesCatalogo = new LinkedHashMap<String, UnidadMedida>();
        registrarUnidad(unidadesCatalogo, "KG", "Kilogramo", "kg");
        return unidadesCatalogo;
    }

    private void registrarUnidad(Map<String, UnidadMedida> catalogo, String codigo, String nombre, String simbolo) {
        var unidad = unidades.findByCodigo(codigo).orElseGet(() -> {
            var nueva = new UnidadMedida();
            nueva.setCodigo(codigo);
            nueva.setNombre(nombre);
            nueva.setSimbolo(simbolo);
            return unidades.save(nueva);
        });
        catalogo.put(codigo, unidad);
    }

    private void cargarArticulos(Map<String, UnidadMedida> unidadesCatalogo) {
        var catalogo = List.of(
                new ArticuloDemo("MAIZ-01", "Maíz amarillo", "Grano de maíz usado como materia prima para la producción de alimento balanceado.", "MATERIA_PRIMA", "KG", "2.50", null),
                new ArticuloDemo("SOYA-01", "Harina de soya", "Ingrediente proteico para la fórmula demostrativa de alimento balanceado.", "MATERIA_PRIMA", "KG", "2.50", null),
                new ArticuloDemo("PUR-001", "Purina de engorde", "Producto terminado de alimento balanceado producido a partir de materias primas.", "PRODUCTO_TERMINADO", "KG", "2.50", "4.50")
        );

        for (var item : catalogo) {
            var articulo = articulos.findByCodigo(item.codigo()).orElseGet(Articulo::new);
            articulo.setCodigo(item.codigo());
            articulo.setNombre(item.nombre());
            articulo.setDescripcion(item.descripcion());
            articulo.setTipoArticulo(Articulo.TipoArticulo.valueOf(item.tipo()));
            articulo.setUnidadMedida(unidadesCatalogo.get(item.unidad()));
            articulo.setCostoReferencial(new BigDecimal(item.costo()));
            if (item.precioVenta() != null) {
                articulo.setPrecioVenta(new BigDecimal(item.precioVenta()));
            }
            articulos.save(articulo);
        }
    }

    private void depurarCatalogoDemo() {
        var codigosFueraDeCatalogo = Set.of(
                "AFRECHO-01", "MELAZA-01", "CARBONATO-01", "PREMIX-01",
                "SACO-40KG", "PUR-LECH-01", "PUR-INIC-01"
        );
        articulos.findAll().stream()
                .filter(articulo -> codigosFueraDeCatalogo.contains(articulo.getCodigo())
                        || esDatoDePrueba(articulo))
                .filter(Articulo::isActivo)
                .forEach(articulo -> {
                    articulo.setActivo(false);
                    articulos.save(articulo);
                });
    }

    private boolean esDatoDePrueba(Articulo articulo) {
        var codigo = articulo.getCodigo() == null ? "" : articulo.getCodigo().trim();
        var nombre = articulo.getNombre() == null ? "" : articulo.getNombre().trim();
        var descripcion = articulo.getDescripcion() == null ? "" : articulo.getDescripcion().trim();
        return nombre.equalsIgnoreCase("sdf")
                || nombre.equals("234")
                || (codigo.equals("234") && nombre.equals("234"))
                || (nombre.equals("234") && descripcion.equals("324"));
    }

    private void crearFormulaInicial() {
        var formula = new Formula();
        formula.setNombre("Purina de engorde 60/40");
        formula.setCodigoProductoTerminado("PUR-001");
        for (var codigo : List.of("MAIZ-01", "SOYA-01")) {
            var detalle = new FormulaDetalle();
            detalle.setCodigoArticulo(codigo);
            detalle.setUnidadMedida("KG");
            detalle.setCantidadPorUnidad(new BigDecimal(codigo.equals("MAIZ-01") ? "0.6" : "0.4"));
            formula.agregarDetalle(detalle);
        }
        formulas.save(formula);
    }

    private record ArticuloDemo(
            String codigo,
            String nombre,
            String descripcion,
            String tipo,
            String unidad,
            String costo,
            String precioVenta
    ) {}
}
