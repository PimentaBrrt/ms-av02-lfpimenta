package lfpimenta.microservicos.p2.usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class UsuarioEntity {

    @Id
    @Column(name = "username")
    public String username;

    @Column(name = "senha")
    public String senha;

    public UsuarioEntity() {
    }

    public UsuarioEntity(String username, String senha) {
        this.username = username;
        this.senha = senha;
    }

}
