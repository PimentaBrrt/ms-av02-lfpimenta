package lfpimenta.microservicos.p2.usuario;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @Operation(summary = "Efetua login", description = "Mais de uma falha bloqueia o usuário", tags = { "usuarios" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário e senha válidos"),
            @ApiResponse(responseCode = "401", description = "Usuário e senha inválidos ou usuário bloqueado") })
    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest request) {
        boolean valido = service.login(request.username, request.senha);
        return new ResponseEntity<Void>(valido ? HttpStatus.OK : HttpStatus.UNAUTHORIZED);
    }

    @Operation(summary = "Troca a senha", description = "Informa a senha atual e a nova senha", tags = { "usuarios" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Senha alterada"),
            @ApiResponse(responseCode = "401", description = "Senha atual inválida"),
            @ApiResponse(responseCode = "404", description = "Usuário não localizado"),
            @ApiResponse(responseCode = "423", description = "Usuário bloqueado") })
    @PutMapping("/trocarsenha/{userid}")
    public ResponseEntity<Void> trocarSenha(@PathVariable String userid, @Valid @RequestBody TrocarSenhaRequest request) {
        service.trocarSenha(userid, request.atual, request.nova);
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    @Operation(summary = "Cadastra um usuário", description = "Requer ROLE_MANUTENCAO", tags = { "usuarios" },
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem a role ROLE_MANUTENCAO"),
            @ApiResponse(responseCode = "409", description = "Usuário já cadastrado") })
    @PostMapping("/")
    public ResponseEntity<Void> cadastrar(@Valid @RequestBody LoginRequest request) {
        service.cadastrar(request.username, request.senha);
        return new ResponseEntity<Void>(HttpStatus.CREATED);
    }

    @Operation(summary = "Lista os usuários", description = "Requer ROLE_MANUTENCAO", tags = { "usuarios" },
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuários"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem a role ROLE_MANUTENCAO") })
    @GetMapping("/")
    public ResponseEntity<Iterable<UsuarioResponse>> listar() {
        return new ResponseEntity<Iterable<UsuarioResponse>>(service.listar(), HttpStatus.OK);
    }

    @Operation(summary = "Desbloqueia um usuário", description = "Requer ROLE_ADMIN", tags = { "usuarios" },
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário desbloqueado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem a role ROLE_ADMIN"),
            @ApiResponse(responseCode = "404", description = "Usuário não localizado") })
    @PutMapping("/unlock/{userid}")
    public ResponseEntity<Map<String, String>> desbloquear(@PathVariable String userid) {
        service.desbloquear(userid);
        return new ResponseEntity<Map<String, String>>(Map.of("mensagem", "Usuário " + userid + " desbloqueado"),
                HttpStatus.OK);
    }

}
