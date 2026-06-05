package br.com.assinanet.security.model;

import br.com.assinanet.models.UsuarioClienteModel;
import br.com.assinanet.response.UsuarioResponse;

import java.util.List;
import java.util.UUID;

/**
 * @author Samuel Oliveira - samuk.exe@hotmail.com
 * Data: 20/08/2018 - 22:46
 */
public class CurrentUser {

    private String token;
    private UsuarioResponse usuario;
    List<UsuarioClienteModel> clientes;
    private UUID idContrato;
    private UUID idCliente;


    public CurrentUser(String token, UsuarioResponse usuario, List<UsuarioClienteModel> clientes) {
        this.token = token;
        this.usuario = usuario;
        this.clientes = clientes;
    }

    public CurrentUser(String token, UsuarioResponse usuario, List<UsuarioClienteModel> clientes, UUID idContrato, UUID idCliente) {
        this.token = token;
        this.usuario = usuario;
        this.clientes = clientes;
        this.idContrato = idContrato;
        this.idCliente = idCliente;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public br.com.assinanet.response.UsuarioResponse getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioResponse usuario) {
        this.usuario = usuario;
    }

    public List<UsuarioClienteModel> getClientes() {
        return clientes;
    }

    public void setClientes(List<UsuarioClienteModel> clientes) {
        this.clientes = clientes;
    }

    public UUID getIdContrato() {
        return idContrato;
    }

    public void setIdContrato(UUID idContrato) {
        this.idContrato = idContrato;
    }

    public UUID getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(UUID idCliente) {
        this.idCliente = idCliente;
    }
}
