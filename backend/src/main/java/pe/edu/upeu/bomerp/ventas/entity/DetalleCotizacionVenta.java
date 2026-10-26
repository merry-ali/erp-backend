package pe.edu.upeu.bomerp.ventas.entity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "detalle_cotizacion_venta", schema = "bomerp")
@Getter
@Setter
public class DetalleCotizacionVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(optional = false)
    @JsonIgnore
    private CotizacionVenta cotizacion;

    @Column(nullable = false)
    private Integer numeroLinea;

    @Column(nullable = false, length = 40)
    private String codigoArticulo;

    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal cantidad;

    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal precioUnitario;

    @Column(nullable = false, precision = 7, scale = 4)
    private BigDecimal descuento;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotal;
}
