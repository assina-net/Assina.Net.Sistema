package br.com.assinanet.entity;

import br.com.assinanet.entity.enums.StatusEnum;
import br.com.assinanet.util.CommonsUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.hibernate.Hibernate;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.*;

/**
 * @author Alexandre Murta - amurta@gmail.com
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TipoDocumento {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uniqueidentifier")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "idCliente")
    @NotNull
    private Cliente cliente;

    @NotNull
    private String nome;

    private String identificacao;

    private Boolean assina;

    private Boolean qrcode;

    private Boolean validacaoOnLine;

    @Enumerated(EnumType.STRING)
    private StatusEnum status;

    private Integer ordem;


    // pessoas fisicas, somente para cadastro de pessoas juridicas
    @OneToMany(mappedBy = "tipoDocumento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<TipoDocumentoPapel> papeis = new ArrayList<>();

    public List<TipoDocumentoPapel> getPapeis() {
        if (papeis != null) {

            //verificando se algum tipo de documento tem papel inativo
            papeis.removeIf( p->  StatusEnum.INATIVO.equals( p.getPapel().getStatus() ) );

            //ordenando
            papeis.sort(Comparator.comparing(o -> CommonsUtil.stringValue(o.getPapel().getNome().toUpperCase())));
        }
        return papeis;
    }


    // pessoas fisicas, somente para cadastro de pessoas juridicas
    @OneToMany(mappedBy = "tipoDocumento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<TipoDocumentoTipoCliente> tipoDocumentoTipoClientes = new ArrayList<>();

    public List<TipoDocumentoTipoCliente> getTipoDocumentoTipoClientes() {
        if (tipoDocumentoTipoClientes != null) {
            //ordenando
            tipoDocumentoTipoClientes.sort((o1, o2) -> {

                int compare = 0;

                if (!CommonsUtil.semValor(o1.getSegmento()) && !CommonsUtil.semValor(o2.getSegmento())) {
                    compare = CommonsUtil.stringValue(o1.getSegmento().getNome().toUpperCase()).compareTo(

                            CommonsUtil.stringValue(o2.getSegmento().getNome().toUpperCase())

                    );
                }

                return compare;
            });
        }
        return tipoDocumentoTipoClientes;
    }

    @OneToMany(mappedBy = "tipoDocumento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<TipoDocumentoPosicao> posicoesAssinatura = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        TipoDocumento that = (TipoDocumento) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
