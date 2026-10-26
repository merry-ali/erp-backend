package pe.edu.upeu.bomerp.produccion.service;

import java.math.BigDecimal;

/**
 * Puerto que Produccion usa para preguntarle a Inventario cuanto hay
 * disponible antes de iniciar una orden -- "Produccion no reserva stock
 * de forma optimista" (ver documento del proyecto). Como el modulo
 * Inventario todavia no existe en este proyecto, hoy solo hay un
 * adaptador de prueba ({@link DisponibilidadInventarioStub}); el dia que
 * se construya Inventario, se reemplaza por un adaptador que llame a su
 * service real por llamada Java directa (misma JVM, mismo monolito) --
 * sin tener que tocar nada de este modulo, porque Produccion solo conoce
 * esta interfaz, nunca la clase concreta de Inventario.
 */
public interface DisponibilidadInventarioPort {

    /** Cantidad disponible de un articulo en un almacen especifico. */
    BigDecimal consultarDisponible(Long almacenId, String codigoArticulo);
}
