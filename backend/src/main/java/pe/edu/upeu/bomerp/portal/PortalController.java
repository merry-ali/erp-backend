package pe.edu.upeu.bomerp.portal;
@org.springframework.web.bind.annotation.RestController @lombok.RequiredArgsConstructor public class PortalController {
private final ContactoRepository repo;
@org.springframework.web.bind.annotation.PostMapping("/api/portal/contacto") public java.util.Map<String,String> contacto(@jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody Contacto r){r.setId(null);r.setFecha(java.time.LocalDateTime.now());repo.save(r);return java.util.Map.of("message","Solicitud enviada al equipo de ventas");}
@org.springframework.web.bind.annotation.GetMapping("/api/ventas/contactos") @org.springframework.security.access.prepost.PreAuthorize("@acceso.permite(authentication,'VENTAS')") public java.util.List<Contacto> listar(){return repo.findAll();}
}