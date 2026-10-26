package pe.edu.upeu.bomerp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del ERP interno (monolito modular).
 * Los 6 modulos de negocio (Compras, Inventario, Produccion, Ventas,
 * Finanzas, Administracion) viven como paquetes de este mismo proceso
 * y se comunican por llamada Java directa, nunca por HTTP entre si.
 * <p>
 * Este proyecto implementa, por ahora, los modulos:
 * - pe.edu.upeu.bomerp.administracion
 * - pe.edu.upeu.bomerp.produccion
 */
@SpringBootApplication
public class BomerpBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BomerpBackendApplication.class, args);
    }
}
