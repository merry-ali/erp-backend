package pe.edu.upeu.bomerp.compras.ordencompra.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ORDENES_COMPRA", schema = "bomerp")
@Getter
@Setter
@NoArgsConstructor
public class OrdenCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "FECHA", nullable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 20)
    private EstadoOrdenCompra estado;

    @Column(name = "TOTAL", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "ID_PROVEEDOR", nullable = false)
    private Long proveedorId;

    @Column(name = "NOMBRE_PROVEEDOR", nullable = false, length = 150)
    private String nombreProveedor;

    @Column(name = "NUMERO_DOCUMENTO_PROVEEDOR", nullable = false, length = 20)
    private String numeroDocumentoProveedor;

    @Column(name = "CONDICIONES_PACTADAS", length = 255)
    private String condicionesPactadas;

    @Column(name="OPERADOR_ID",length=36)
    private String operadorId;

    @OneToMany(mappedBy = "ordenCompra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleOrden> detalles = new ArrayList<>();

    public void addDetalle(DetalleOrden detalle) {
        detalles.add(detalle);
        detalle.setOrdenCompra(this);
    }

    public void removeDetalle(DetalleOrden detalle) {
        detalles.remove(detalle);
        detalle.setOrdenCompra(null);
    }
}
