package pe.edu.upeu.bomerp.personas.persona.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upeu.bomerp.personas.persona.dto.ConsultaDocumentoResponse;
import pe.edu.upeu.bomerp.personas.persona.entity.TipoDocumento;

import java.util.Map;

@Service
public class ConsultaDocumentoService {
    private final RestClient client;
    private final String token;

    public ConsultaDocumentoService(
            @Value("${consulta-documento.base-url:https://api.apiperu.pe}") String baseUrl,
            @Value("${consulta-documento.token:}") String token) {
        this.client = RestClient.create(baseUrl);
        this.token = token;
    }

    public ConsultaDocumentoResponse consultar(TipoDocumento tipo, String numero) {
        if (tipo != TipoDocumento.DNI && tipo != TipoDocumento.RUC) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La consulta solo está disponible para DNI y RUC");
        }
        String limpio = numero == null ? "" : numero.trim();
        int largo = tipo == TipoDocumento.DNI ? 8 : 11;
        if (!limpio.matches("\\d{" + largo + "}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El " + tipo + " debe tener " + largo + " dígitos");
        }
        if (token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Configura la credencial de consulta DNI/RUC en API_PERU_TOKEN");
        }
        try {
            String ruta = tipo == TipoDocumento.DNI ? "/dni" : "/ruc";
            String campo = tipo == TipoDocumento.DNI ? "dni" : "ruc";
            Map<?, ?> body = client.post()
                    .uri(ruta)
                    .headers(h -> h.setBearerAuth(token))
                    .body(Map.of(campo, limpio))
                    .retrieve()
                    .body(Map.class);
            if (body == null || !Boolean.TRUE.equals(body.get("success"))) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontraron datos para ese documento");
            }
            Object datos = body.get("data");
            Map<?, ?> data = datos instanceof Map<?, ?> mapa ? mapa : body;
            String nombre = tipo == TipoDocumento.DNI
                    ? first(data, "nombre_completo", "nombre")
                    : first(data, "nombre_o_razon_social", "razon_social", "nombre_comercial", "nombre");
            String direccion = first(data, "direccion_completa", "direccion", "domicilio_fiscal", "address");
            return new ConsultaDocumentoResponse(limpio, nombre, direccion);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El documento no figura en el padrón de consulta");
        } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests e) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "La API alcanzó su límite de consultas; vuelve a intentar más tarde");
        } catch (org.springframework.web.client.HttpClientErrorException.Unauthorized e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "La credencial de API Perú no es válida o venció");
        } catch (org.springframework.web.client.HttpClientErrorException.Forbidden e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "La cuenta de API Perú no tiene un plan activo para consultar documentos");
        } catch (org.springframework.web.client.ResourceAccessException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No hay conexión con API Perú; puedes completar los datos manualmente");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo consultar el servicio externo de DNI/RUC");
        }
    }

    private String text(Object value) { return value == null ? "" : value.toString().trim(); }
    private String first(Map<?, ?> values, String... keys) {
        for (String key : keys) {
            String value = text(values.get(key));
            if (!value.isBlank()) return value;
        }
        return "";
    }
}
