package pe.edu.upeu.bomerp.ventas.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "pedido", schema = "bomerp")
@Getter 
@Setter
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Version
    private Long revision;

    @Column(nullable = false)
    private Long clienteId;

    @Column(nullable = false)
    private Long almacenId;

    private Long cotizacionVentaId;

    private String operadorSub;

    @Column(nullable = false, length = 20)
    private String estado = "BORRADOR";

    private String vehiculo;

    @Column(length = 300)
    private String motivoAnulacion;

    @Column(length = 300)
    private String motivoReversion;

    private LocalDateTime fecha = LocalDateTime.now();

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(nullable = false, length = 3)
    private String moneda = "PEN";

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();

}
