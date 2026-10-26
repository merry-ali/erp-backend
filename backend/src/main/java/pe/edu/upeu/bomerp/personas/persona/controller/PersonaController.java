package pe.edu.upeu.bomerp.personas.persona.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.bomerp.personas.persona.dto.PersonaRequest;
import pe.edu.upeu.bomerp.personas.persona.dto.PersonaResponse;
import pe.edu.upeu.bomerp.personas.persona.entity.RolPersona;
import pe.edu.upeu.bomerp.personas.persona.service.PersonaService;
import pe.edu.upeu.bomerp.personas.persona.service.ConsultaDocumentoService;
import pe.edu.upeu.bomerp.personas.persona.dto.ConsultaDocumentoResponse;
import pe.edu.upeu.bomerp.personas.persona.entity.TipoDocumento;

import java.util.List;
import java.util.Map;

@org.springframework.security.access.prepost.PreAuthorize("@acceso.permite(authentication, 'COMPRAS') or @acceso.permite(authentication, 'VENTAS') or @acceso.admin(authentication)")
@RestController
@RequestMapping("/api/v1/personas")
@RequiredArgsConstructor
@Tag(name = "Personas", description = "API para la gestión de Personas (Clientes y Proveedores)")
public class PersonaController {

    private final PersonaService personaService;
    private final ConsultaDocumentoService consultaDocumentoService;

    @GetMapping("/consulta-documento")
    @Operation(summary = "Consultar datos básicos por DNI o RUC")
    public ResponseEntity<ConsultaDocumentoResponse> consultarDocumento(
            @RequestParam TipoDocumento tipo, @RequestParam String numero) {
        return ResponseEntity.ok(consultaDocumentoService.consultar(tipo, numero));
    }

    @GetMapping("/verificar-documento")
    @Operation(summary = "Verificar si un número de documento ya está registrado")
    public ResponseEntity<Map<String, Boolean>> verificarDocumento(
            @RequestParam String numero, @RequestParam(required = false) Long excluirId) {
        return ResponseEntity.ok(Map.of("existe", personaService.existeDocumento(numero, excluirId)));
    }

    @GetMapping
    @Operation(summary = "Listar todas las personas o filtrar por rol")
    public ResponseEntity<List<PersonaResponse>> listarPersonas(
            @RequestParam(required = false) RolPersona rol) {
        return ResponseEntity.ok(personaService.listarPersonas(rol));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una persona por su ID")
    public ResponseEntity<PersonaResponse> obtenerPersona(@PathVariable Long id) {
        return ResponseEntity.ok(personaService.obtenerPersona(id));
    }

    @PostMapping
    @Operation(summary = "Crear una nueva persona")
    public ResponseEntity<PersonaResponse> crearPersona(@Valid @RequestBody PersonaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(personaService.crearPersona(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una persona existente")
    public ResponseEntity<PersonaResponse> actualizarPersona(
            @PathVariable Long id, @Valid @RequestBody PersonaRequest request) {
        return ResponseEntity.ok(personaService.actualizarPersona(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una persona de forma lógica (baja)")
    public ResponseEntity<Void> eliminarPersona(@PathVariable Long id) {
        personaService.eliminarPersona(id);
        return ResponseEntity.noContent().build();
    }
}
