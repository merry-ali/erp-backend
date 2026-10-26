package pe.edu.upeu.bomerp.administracion.service;
import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;import lombok.RequiredArgsConstructor;import org.springframework.security.core.Authentication;import java.util.*;
import pe.edu.upeu.bomerp.administracion.repository.RolRepository;
@Service("acceso") @RequiredArgsConstructor public class Acceso {
 private final RolRepository roles;
 @Transactional(readOnly=true) public boolean permite(Authentication a,String modulo){
  if(a==null)return false;
  var nombres=a.getAuthorities().stream().map(r->r.getAuthority().replaceFirst("^ROLE_", "")).toList();
  if(nombres.contains("ADMIN"))return true;
  try {
   if(roles.findAll().stream().filter(r->r.isActivo()&&nombres.contains(r.getNombre()))
       .anyMatch(r->r.getPermisos().stream().anyMatch(p->p.getModulo().name().equals(modulo)))) return true;
  } catch (RuntimeException ignored) {
   // During the first login the identity provider can be available before
   // the optional institutional permission seed has finished.
  }
  return nombres.stream().anyMatch(rol -> switch(rol){
   case "COMPRADOR" -> modulo.equals("COMPRAS");
   case "ALMACENERO" -> modulo.equals("INVENTARIO");
   case "OPERARIO_PLANTA" -> modulo.equals("PRODUCCION");
   case "VENDEDOR" -> modulo.equals("VENTAS");
   default -> false;
  });}
 public boolean admin(Authentication a){return a!=null&&a.getAuthorities().stream().anyMatch(r->r.getAuthority().equals("ROLE_ADMIN"));}
}
