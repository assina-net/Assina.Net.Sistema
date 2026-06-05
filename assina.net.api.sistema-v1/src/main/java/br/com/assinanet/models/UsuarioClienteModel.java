package br.com.assinanet.models;

import br.com.assinanet.entity.Cliente;
import br.com.assinanet.entity.enums.PerfilEnum;
import br.com.assinanet.entity.enums.SistemaTipoAtributoEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class UsuarioClienteModel {

    @Getter
    @Setter
    Cliente cliente;

    @Getter
    @Setter
    private PerfilEnum perfil;

    @Getter
    @Setter
    private Map<SistemaTipoAtributoEnum, String> sistemaAtributo;

    public UsuarioClienteModel(Cliente cliente, PerfilEnum perfil) {
        this.cliente = cliente;
        this.perfil = perfil;
        sistemaAtributo = new HashMap<>(0);
    }


}
