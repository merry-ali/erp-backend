package pe.edu.upeu.bomerp.administracion.service;
import org.springframework.stereotype.Component;import org.springframework.boot.CommandLineRunner;import org.springframework.transaction.annotation.Transactional;import lombok.RequiredArgsConstructor;import pe.edu.upeu.bomerp.administracion.repository.*;import pe.edu.upeu.bomerp.administracion.model.*;import java.util.*;
@Component @RequiredArgsConstructor public class RolesIniciales implements CommandLineRunner {
private final RolRepository roles;private final PermisoRepository permisos;
@Transactional public void run(String...args){if(roles.count()>0)return;var mapa=new LinkedHashMap<String,Permiso>();for(var m:List.of(Permiso.Modulo.ADMINISTRACION,Permiso.Modulo.COMPRAS,Permiso.Modulo.INVENTARIO,Permiso.Modulo.PRODUCCION,Permiso.Modulo.VENTAS)){var p=new Permiso();p.setNombre("OPERAR_"+m);p.setModulo(m);p.setDescripcion("Acceso al módulo "+m);mapa.put(m.name(),permisos.save(p));}
for(var nombre:List.of("ADMIN","COMPRADOR","ALMACENERO","OPERARIO_PLANTA","VENDEDOR")){var r=new Rol();r.setNombre(nombre);r.setDescripcion("Rol inicial del ERP");String m=switch(nombre){case "COMPRADOR"->"COMPRAS";case "ALMACENERO"->"INVENTARIO";case "OPERARIO_PLANTA"->"PRODUCCION";case "VENDEDOR"->"VENTAS";default->"ADMINISTRACION";};r.getPermisos().add(mapa.get(m));roles.save(r);}}
}
