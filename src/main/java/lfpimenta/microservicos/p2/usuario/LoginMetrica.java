package lfpimenta.microservicos.p2.usuario;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class LoginMetrica {

    private final Counter loginsValidos;
    private final Counter loginsInvalidos;

    public LoginMetrica(MeterRegistry registry, LoginAttemptRepository attemptRepo) {
        loginsValidos = Counter.builder("login.validos")
                .description("Quantidade de logins válidos")
                .register(registry);
        loginsInvalidos = Counter.builder("login.invalidos")
                .description("Quantidade de logins inválidos")
                .register(registry);
        Gauge.builder("usuarios.bloqueados", attemptRepo, repo -> repo.countByLocked(true))
                .description("Total de usuários bloqueados")
                .register(registry);
    }

    public void loginValido() {
        loginsValidos.increment();
    }

    public void loginInvalido() {
        loginsInvalidos.increment();
    }

}
