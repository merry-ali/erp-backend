package pe.edu.upeu.bomerp.administracion.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Empresa duena del sistema. Hoy el sistema opera mono-tenant (una sola
 * fila), pero se modela como entidad explicita desde el inicio: si el
 * negocio se ofrece como SaaS mas adelante, esta es la raiz de aislamiento
 * (tenant) de la que cuelgan Sucursal, Almacen y Tienda, y en cascada,
 * cada movimiento de los demas modulos.
 */
@Entity
@Table(name = "empresa", schema = "bomerp")
@Getter
@Setter
@NoArgsConstructor
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "empresa_seq")
    @SequenceGenerator(name = "empresa_seq", sequenceName = "empresa_seq", allocationSize = 1)
    private Long id;

    @NotBlank
    @Column(name = "ruc", length = 20, nullable = false, unique = true)
    private String ruc;

    @NotBlank
    @Column(name = "razon_social", length = 200, nullable = false)
    private String razonSocial;

    @Column(name = "nombre_comercial", length = 200)
    private String nombreComercial;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;
}
