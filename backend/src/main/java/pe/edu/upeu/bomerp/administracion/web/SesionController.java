package pe.edu.upeu.bomerp.administracion.web;
import org.springframework.web.bind.annotation.*;import org.springframework.security.core.Authentication;import lombok.RequiredArgsConstructor;import java.util.*;import pe.edu.upeu.bomerp.administracion.service.Acceso;
@RestController @RequiredArgsConstructor public class SesionController {
 private final Acceso acceso;
 @GetMapping("/api/sesion") public Map<String,Object> sesion(Authentication a){
  var roles=a==null?List.<String>of():a.getAuthorities().stream().map(r->r.getAuthority().replaceFirst("^ROLE_","")).toList();
  var modulos=new java.util.ArrayList<String>();
  if(roles.contains("ADMIN")) modulos.addAll(List.of("ADMINISTRACION","PRODUCCION","INVENTARIO","COMPRAS","VENTAS"));
  if(roles.contains("COMPRADOR")) modulos.add("COMPRAS");
  if(roles.contains("ALMACENERO")) modulos.add("INVENTARIO");
  if(roles.contains("OPERARIO_PLANTA")) modulos.add("PRODUCCION");
  if(roles.contains("VENDEDOR")) modulos.add("VENTAS");
  String sub = (a == null || a.getName() == null) ? "" : a.getName();

        return Map.of(
            "sub", sub,
            "roles", roles,
            "modulos", modulos.stream().distinct().toList()
        );
 }
}
