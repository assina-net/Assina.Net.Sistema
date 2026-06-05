package br.com.assinanet.entity.enums;

/**
 * @author Samuel Oliveira - samuk.exe@hotmail.com
 */
public enum PerfilEnum {

    ROLE_ADMIN("ADMINISTRADOR"),
    ROLE_USUARIO("USUÁRIO"),
    ROLE_ADMIN_CLIENTE("ADMINISTRADOR CLIENTE"),
    ROLE_ASSINADOR("ASSINADOR"),
    ROLE_SUPORTE("SUPORTE"),
    ROLE_DIRETORIA("DIRETORIA"),
    ROLE_FINANCEIRO("FINANCEIRO"),
    ROLE_INTEGRACAO("ROLE_INTEGRACAO");

    private final String descricao;

    PerfilEnum(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
