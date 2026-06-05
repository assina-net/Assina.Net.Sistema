package br.com.assinanet.response;

import br.com.assinanet.entity.*;
import br.com.assinanet.entity.enums.StatusAssinaturaEnum;
import br.com.assinanet.entity.enums.StatusEnum;
import br.com.assinanet.entity.enums.TipoPessoaEnum;
import br.com.assinanet.util.CommonsUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class ContratoParteResponse {

    private UUID id;

    private Contrato contrato;

    private ContratoParte contratoPartePJ;

    private StatusEnum status;

    private UUID idPessoa;

    private TipoPessoaEnum tipoPessoa;

    private String cpfCnpj;

    private String nomeRazaoSocial;

    private String email;

    private String celular;

    private String chaveAcesso;

    private Date validadeChaveAcesso;

    private String tokenAssinatura;

    private StatusAssinaturaEnum statusAssinatura;

    private String requisitoAssinatura;

    private Boolean liberadoAssinatura;

    public String getStatusAssinaturaDesc() {

        if (this.statusAssinatura != null) {
            return this.statusAssinatura.getDescricao();
        } else {
            return null;
        }
    }

    private Boolean duplicatas;

    // Contatos
    private List<ContratoParteResponse> contatos = new ArrayList<>(0);

    public List<ContratoParteResponse> getContatos() {

        contatos.removeIf(c -> StatusEnum.INATIVO.equals(c.getStatus()));
        Collections.sort(contatos, (o1, o2) -> {
            int iRetorno = o1.getNomeRazaoSocial().compareTo(o2.getNomeRazaoSocial());
            return iRetorno;
        });

        return contatos;
    }

    // papeis
    private List<ContratoPartePapel> papel = new ArrayList<>(0);

    public List<ContratoPartePapel> getPapel() {
        papel = papel.stream().filter(p -> p.getPapel().getStatus() == StatusEnum.ATIVO || p.getId() == null).collect(Collectors.toList());
        Collections.sort(papel, (o1, o2) -> {
            int iRetorno = o1.getPapel().getNome().compareTo(o2.getPapel().getNome());
            return iRetorno;
        });
        return papel;
    }


    public ContratoParteResponse() {
        super();
    }

    public ContratoParteResponse(ContratoParte contratoParte) {
        BeanUtils.copyProperties(contratoParte, this, "contatos");


        if (contratoParte.getContatos() != null) {
            this.contatos = new ArrayList<>(0);
            contratoParte.getContatos().forEach(contato -> {
                if (StatusEnum.ATIVO.equals(contato.getStatus()))
                    this.contatos.add(new ContratoParteResponse(contato));
            });
        } else {
            this.contatos = null;
        }

    }

    public ContratoParteResponse(Pessoa pessoa) {
        if (pessoa != null) {
            this.nomeRazaoSocial = pessoa.getNomeRazaoSocial();
            this.cpfCnpj = pessoa.getCpfCnpj();
            this.email = pessoa.getEmail();
            this.tipoPessoa = pessoa.getTipoPessoa();
        }
    }


    public ContratoParteResponse(TipoDocumentoParte tipoDocumentoParte, Pessoa pessoa) {
        if (pessoa != null) {
            this.nomeRazaoSocial = pessoa.getNomeRazaoSocial();
            this.cpfCnpj = pessoa.getCpfCnpj();
            this.email = pessoa.getEmail();
            this.tipoPessoa = pessoa.getTipoPessoa();


            if ( pessoa.getPessoaTelefone().size() > 0){
                PessoaTelefone celular = pessoa.getPessoaTelefone().stream().filter(t -> CommonsUtil.mesmoValor( t.getTipoTelefone().getIdentificacao() ,"CELULAR")).findFirst().orElse(null);
                if( celular != null)
                this.celular = celular.getNumero();
            }

        }

        if (tipoDocumentoParte != null) {
            this.id = tipoDocumentoParte.getId();
            this.papel = new ArrayList<>(0);
            tipoDocumentoParte.getPapel().forEach(papel -> {
                this.papel.add(new ContratoPartePapel(papel));
            });
        }
    }


}
