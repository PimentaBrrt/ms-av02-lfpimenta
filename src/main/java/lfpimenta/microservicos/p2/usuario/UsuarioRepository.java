package lfpimenta.microservicos.p2.usuario;

import org.springframework.data.repository.CrudRepository;

public interface UsuarioRepository extends CrudRepository<UsuarioEntity, String> {

    Iterable<UsuarioEntity> findAllByOrderByUsernameAsc();

}
