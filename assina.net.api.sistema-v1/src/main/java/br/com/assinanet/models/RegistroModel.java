package br.com.assinanet.models;

import br.com.assinanet.entity.enums.TipoPessoaEnum;
import lombok.Data;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.UUID;

@Data
public class RegistroModel {

    private String nomeRazaoSocial;

    private String cpfCnpj;

    private String rgie;

    private String nomeRazaoSocialResponsavel;

    private String cpfCnpjResponsavel;

    @Enumerated(EnumType.STRING)
    private TipoPessoaEnum tipoPessoa;

    private String email;

    private String celular;

    private String indicacao;

    private UUID idPlano;
}
