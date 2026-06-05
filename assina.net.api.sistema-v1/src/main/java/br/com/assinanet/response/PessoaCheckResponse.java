package br.com.assinanet.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
public class PessoaCheckResponse {
    @NotNull
    private String nomeRazaoSocial;

    @NotNull
    private String cpfCnpj;
}
