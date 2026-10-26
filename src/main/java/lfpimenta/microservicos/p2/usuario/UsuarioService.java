package lfpimenta.microservicos.p2.usuario;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lfpimenta.microservicos.p2.common.SenhaAtualInvalidaException;
import lfpimenta.microservicos.p2.common.UsuarioBloqueadoException;
import lfpimenta.microservicos.p2.common.UsuarioJaExisteException;
import lfpimenta.microservicos.p2.common.UsuarioNaoEncontradoException;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepo;
    private final LoginAttemptRepository attemptRepo;
    private final JdbcUserDetailsManager userManager;
    private final LoginMetrica metrica;
    private final int maxFalhas;

    public UsuarioService(UsuarioRepository usuarioRepo, LoginAttemptRepository attemptRepo,
            JdbcUserDetailsManager userManager, LoginMetrica metrica, @Value("${app.login.max-falhas}") int maxFalhas) {
        this.usuarioRepo = usuarioRepo;
        this.attemptRepo = attemptRepo;
        this.userManager = userManager;
        this.metrica = metrica;
        this.maxFalhas = maxFalhas;
    }

    @Transactional
    public synchronized boolean login(String username, String senha) {
        Optional<UsuarioEntity> usuario = usuarioRepo.findById(username);
        if (usuario.isEmpty() || estaBloqueado(username)) {
            metrica.loginInvalido();
            return false;
        }
        if (usuario.get().senha.equals(senha)) {
            zerarTentativas(username);
            metrica.loginValido();
            return true;
        }
        registrarFalha(username);
        metrica.loginInvalido();
        return false;
    }

    @Transactional(noRollbackFor = SenhaAtualInvalidaException.class)
    public synchronized void trocarSenha(String username, String atual, String nova) {
        UsuarioEntity usuario = usuarioRepo.findById(username).orElseThrow(UsuarioNaoEncontradoException::new);
        if (estaBloqueado(username)) {
            throw new UsuarioBloqueadoException();
        }
        if (!usuario.senha.equals(atual)) {
            registrarFalha(username);
            throw new SenhaAtualInvalidaException();
        }
        usuario.senha = nova;
        usuarioRepo.save(usuario);
        zerarTentativas(username);
        UserDetails detalhes = userManager.loadUserByUsername(username);
        userManager.updateUser(User.withUserDetails(detalhes).password("{noop}" + nova).build());
    }

    @Transactional
    public void cadastrar(String username, String senha) {
        if (usuarioRepo.existsById(username) || userManager.userExists(username)) {
            throw new UsuarioJaExisteException();
        }
        usuarioRepo.save(new UsuarioEntity(username, senha));
        userManager.createUser(User.withUsername(username).password("{noop}" + senha).roles("USUARIO").build());
    }

    @Transactional
    public void desbloquear(String username) {
        usuarioRepo.findById(username).orElseThrow(UsuarioNaoEncontradoException::new);
        LoginAttemptEntity tentativa = attemptRepo.findById(username).orElse(new LoginAttemptEntity(username));
        tentativa.locked = false;
        tentativa.failedAttempts = 0;
        tentativa.lockedAt = null;
        tentativa.updatedAt = LocalDateTime.now();
        attemptRepo.save(tentativa);
        habilitar(username, true);
    }

    public List<UsuarioResponse> listar() {
        List<UsuarioResponse> usuarios = new ArrayList<UsuarioResponse>();
        for (UsuarioEntity usuario : usuarioRepo.findAllByOrderByUsernameAsc()) {
            LoginAttemptEntity tentativa = attemptRepo.findById(usuario.username).orElse(null);
            usuarios.add(new UsuarioResponse(usuario.username, tentativa != null && tentativa.locked,
                    tentativa == null ? 0 : tentativa.failedAttempts));
        }
        return usuarios;
    }

    private boolean estaBloqueado(String username) {
        return attemptRepo.findById(username).map(tentativa -> tentativa.locked).orElse(false);
    }

    private void zerarTentativas(String username) {
        attemptRepo.findById(username).ifPresent(tentativa -> {
            tentativa.failedAttempts = 0;
            tentativa.updatedAt = LocalDateTime.now();
            attemptRepo.save(tentativa);
        });
    }

    private void registrarFalha(String username) {
        LocalDateTime agora = LocalDateTime.now();
        LoginAttemptEntity tentativa = attemptRepo.findById(username).orElse(new LoginAttemptEntity(username));
        tentativa.failedAttempts++;
        tentativa.lastFailedAt = agora;
        tentativa.updatedAt = agora;
        if (tentativa.failedAttempts > maxFalhas) {
            tentativa.locked = true;
            tentativa.lockedAt = agora;
            habilitar(username, false);
        }
        attemptRepo.save(tentativa);
    }

    private void habilitar(String username, boolean habilitado) {
        UserDetails detalhes = userManager.loadUserByUsername(username);
        userManager.updateUser(User.withUserDetails(detalhes).disabled(!habilitado).build());
    }

}
