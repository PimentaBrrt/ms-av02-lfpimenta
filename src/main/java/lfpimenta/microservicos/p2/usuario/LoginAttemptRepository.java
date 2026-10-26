package lfpimenta.microservicos.p2.usuario;

import org.springframework.data.repository.CrudRepository;

public interface LoginAttemptRepository extends CrudRepository<LoginAttemptEntity, String> {

    long countByLocked(boolean locked);

}
