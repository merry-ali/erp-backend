package pe.edu.upeu.bomerp.articulo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Ficha unica de un articulo (materia prima, insumo, empaque, producto
 * terminado o subproducto) que fluye por todo el sistema. Ningun otro
 * modulo mantiene su propia tabla de articulos: Inventario, Produccion,
 * Compras y Ventas lo referencian por {@code codigo} (String), igual que
 * Compras/Ventas referencian a {@code Persona} por id en vez de duplicar
 * el contacto.
 * <p>
 * Este modulo NO guarda stock ni costos de movimiento: la cantidad
 * disponible real la calcula Inventario a partir de sus lotes, y el
 * costo de produccion lo calcula Produccion/Costos al cerrar una orden.
 * El campo {@code costoReferencial} de aqui es solo un valor de
 * catalogo (uso interno) y se excluye explicitamente de la vista que se
 * entrega al Portal Web (ver {@code ArticuloPublicoResponse}).
 */
@Entity
@Table(name = "articulo", schema = "bomerp",
        uniqueConstraints = @UniqueConstraint(columnNames = "codigo"))
@Getter
@Setter
@NoArgsConstructor
public class Articulo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "articulo_seq")
    @SequenceGenerator(name = "articulo_seq", sequenceName = "articulo_seq", allocationSize = 1)
    private Long id;

    /** Codigo unico usado por el resto de modulos para referenciar este articulo. */
    @NotBlank
    @Column(name = "codigo", length = 30, nullable = false)
    private String codigo;

    @NotBlank
    @Column(name = "nombre", length = 150, nullable = false)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    /** URL o referencia de la imagen mostrada en el catalogo publico del Portal Web. */
    @Column(name = "imagen_url", length = 300)
    private String imagenUrl;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_articulo", length = 20, nullable = false)
    private TipoArticulo tipoArticulo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unidad_medida_id", nullable = false)
    private UnidadMedida unidadMedida;
    @ManyToOne(fetch = FetchType.LAZY)
    private Categoria categoria;

    /** Valor de catalogo, uso interno. NUNCA se expone en la vista publica del Portal. */
    @Column(name = "costo_referencial", precision = 12, scale = 4)
    private BigDecimal costoReferencial;

    /** Precio unitario de venta; es distinto del costo interno referencial. */
    @Column(name = "precio_venta", precision = 14, scale = 4)
    private BigDecimal precioVenta;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    public enum TipoArticulo {
        MATERIA_PRIMA, INSUMO, EMPAQUE, PRODUCTO_TERMINADO, SUBPRODUCTO
    }
}
