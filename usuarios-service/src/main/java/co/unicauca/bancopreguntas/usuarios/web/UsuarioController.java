package co.unicauca.bancopreguntas.usuarios.web;

import co.unicauca.bancopreguntas.usuarios.domain.Rol;
import co.unicauca.bancopreguntas.usuarios.service.UsuarioService;
import co.unicauca.bancopreguntas.usuarios.web.dto.LoginRequest;
import co.unicauca.bancopreguntas.usuarios.web.dto.RegistroRequest;
import co.unicauca.bancopreguntas.usuarios.web.dto.UsuarioResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrar(@RequestBody RegistroRequest request) {
        return UsuarioResponse.from(service.registrar(request.login(), request.nombreCompleto(),
                request.email(), request.rol(), request.password()));
    }

    @PostMapping("/login")
    public UsuarioResponse login(@RequestBody LoginRequest request) {
        return UsuarioResponse.from(service.autenticar(request.login(), request.password()));
    }

    @GetMapping("/{id}")
    public UsuarioResponse buscar(@PathVariable Long id) {
        return UsuarioResponse.from(service.buscar(id));
    }

    @GetMapping
    public List<UsuarioResponse> listar(@RequestParam(required = false) Rol rol) {
        return service.listar(rol).stream().map(UsuarioResponse::from).toList();
    }
}
