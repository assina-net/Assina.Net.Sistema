package br.com.assinanet.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
@AllArgsConstructor
public class PessoaCheckResponse {
    @NotNull
    private String nomeRazaoSocial;

    @NotNull
    private String cpfCnpj;
}
