package pe.edu.upeu.bomerp.ventas.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.edu.upeu.bomerp.ventas.repository.PedidoRepository;

@RestController
@RequestMapping("/api/ventas/reportes")
@RequiredArgsConstructor
@PreAuthorize("@acceso.permite(authentication,'VENTAS')")
public class VentasReportesController {
    private final PedidoRepository pedidos;

    @GetMapping
    @Transactional(readOnly = true)
    public Map<String, Object> resumen(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new IllegalArgumentException("El periodo de inicio no puede ser posterior al final");
        }
        var lista = pedidos.findAll().stream()
                .filter(p -> desde == null || !p.getFecha().toLocalDate().isBefore(desde))
                .filter(p -> hasta == null || !p.getFecha().toLocalDate().isAfter(hasta))
                .toList();
        BigDecimal despachado = lista.stream().filter(p -> "DESPACHADO".equals(p.getEstado()) || "DEVUELTO".equals(p.getEstado()))
                .map(p -> p.getTotal()).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal devuelto = lista.stream().filter(p -> "DEVUELTO".equals(p.getEstado()))
                .map(p -> p.getTotal()).reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Map<String, Object>> porProducto = new LinkedHashMap<>();
        for (var pedido : lista) {
            if (!"DESPACHADO".equals(pedido.getEstado()) && !"DEVUELTO".equals(pedido.getEstado())) continue;
            for (var linea : pedido.getDetalles()) {
                var producto = porProducto.computeIfAbsent(linea.getCodigoArticulo(), codigo -> {
                    Map<String, Object> datos = new LinkedHashMap<>();
                    datos.put("codigoArticulo", codigo);
                    datos.put("unidades", BigDecimal.ZERO);
                    datos.put("totalBruto", BigDecimal.ZERO);
                    datos.put("totalDevuelto", BigDecimal.ZERO);
                    return datos;
                });
                producto.put("unidades", ((BigDecimal) producto.get("unidades")).add(linea.getCantidad()));
                producto.put("totalBruto", ((BigDecimal) producto.get("totalBruto")).add(linea.getSubtotal()));
                if ("DEVUELTO".equals(pedido.getEstado())) {
                    producto.put("totalDevuelto", ((BigDecimal) producto.get("totalDevuelto")).add(linea.getSubtotal()));
                }
            }
        }
        List<Map<String, Object>> resumenProducto = new ArrayList<>(porProducto.values());
        resumenProducto.forEach(producto -> producto.put("totalNeto",
                ((BigDecimal) producto.get("totalBruto")).subtract((BigDecimal) producto.get("totalDevuelto"))));
        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("pedidos", lista.size());
        resumen.put("despachados", lista.stream().filter(p -> "DESPACHADO".equals(p.getEstado())).count());
        resumen.put("devueltos", lista.stream().filter(p -> "DEVUELTO".equals(p.getEstado())).count());
        resumen.put("anulados", lista.stream().filter(p -> "ANULADO".equals(p.getEstado())).count());
        resumen.put("totalDespachado", despachado);
        resumen.put("totalDevuelto", devuelto);
        resumen.put("totalNeto", despachado.subtract(devuelto));
        resumen.put("porProducto", resumenProducto);
        return resumen;
    }
}
