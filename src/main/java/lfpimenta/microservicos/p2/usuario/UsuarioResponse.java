package lfpimenta.microservicos.p2.usuario;

public class UsuarioResponse {

    public String username;
    public boolean bloqueado;
    public int tentativasFalhas;

    public UsuarioResponse(String username, boolean bloqueado, int tentativasFalhas) {
        this.username = username;
        this.bloqueado = bloqueado;
        this.tentativasFalhas = tentativasFalhas;
    }

}
