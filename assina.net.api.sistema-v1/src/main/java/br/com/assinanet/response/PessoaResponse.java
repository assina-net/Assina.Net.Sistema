package br.com.assinanet.response;

import br.com.assinanet.entity.*;
import br.com.assinanet.entity.enums.StatusEnum;
import br.com.assinanet.entity.enums.TipoPessoaEnum;
import br.com.assinanet.util.CommonsUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class PessoaResponse {

    private UUID id;

    private String nomeRazaoSocial;

    private String cpfCnpj;

    private TipoPessoaEnum tipoPessoa;

    private String email;

    private String celular;

    private Cliente cliente;

    private List<PessoaEndereco> pessoaEndereco = new ArrayList<>(0);

    private List<PessoaTelefone> pessoaTelefone = new ArrayList<>(0);

    private List<PessoaFisicaPapel> papel = new ArrayList<>(0);

    private List<PessoaJuridicaPessoaFisica> pessoasFisica = new ArrayList<>(0);

    public PessoaResponse ( Pessoa pessoa){
        BeanUtils.copyProperties(pessoa, this );
    }

}
