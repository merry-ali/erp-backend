package pe.edu.upeu.bomerp.produccion.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Orden de produccion: planifica una cantidad contra una {@link Formula},
 * y al ejecutarse registra consumo real, rendimiento, mermas y los
 * productos resultantes. Antes de pasar a EN_PROCESO, este modulo
 * confirma disponibilidad con Inventario (via el puerto
 * {@code DisponibilidadInventarioPort}) -- nunca reserva de forma
 * optimista.
 */
@Entity
@Table(name = "orden_produccion", schema = "bomerp")
@Getter
@Setter
@NoArgsConstructor
public class OrdenProduccion {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orden_produccion_seq")
    @SequenceGenerator(name = "orden_produccion_seq", sequenceName = "orden_produccion_seq", allocationSize = 1)
    private Long id;
    @Version
    private Long revision;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "formula_id", nullable = false)
    private Formula formula;

    /** Almacen de origen de insumos y destino del producto terminado (id de Administracion). */
    @NotNull
    @Column(name = "almacen_origen_id", nullable = false)
    private Long almacenOrigenId;

    @NotNull
    @Column(name = "almacen_destino_id", nullable = false)
    private Long almacenDestinoId;

    @NotNull
    @Positive
    @Column(name = "cantidad_planificada", precision = 12, scale = 4, nullable = false)
    private BigDecimal cantidadPlanificada;

    @Column(name = "cantidad_real", precision = 12, scale = 4)
    private BigDecimal cantidadReal;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    private EstadoOrden estado = EstadoOrden.PLANIFICADA;

    @Column(name = "fecha_planificada", nullable = false)
    private LocalDateTime fechaPlanificada = LocalDateTime.now();

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @OneToMany(mappedBy = "ordenProduccion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConsumoInsumo> consumos = new ArrayList<>();

    @OneToMany(mappedBy = "ordenProduccion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductoResultante> resultados = new ArrayList<>();

    public void agregarConsumo(ConsumoInsumo consumo) {
        consumo.setOrdenProduccion(this);
        this.consumos.add(consumo);
    }

    public void agregarResultado(ProductoResultante resultado) {
        resultado.setOrdenProduccion(this);
        this.resultados.add(resultado);
    }

    public enum EstadoOrden {
        PLANIFICADA, EN_ESPERA, EN_PROCESO, CERRADA, CANCELADA
    }
}
