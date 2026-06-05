package br.com.assinanet.security.jwt;

import java.io.Serializable;

/**
 * @author Samuel Oliveira - samuk.exe@hotmail.com
 * Data: 20/08/2018 - 22:40
 */
public class JwtAuthenticationRequest implements Serializable {

    private static final long serialVersionUID = 1L;
    private String login;
    private String senha;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return this.senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}