package br.com.assinanet.entity;

import br.com.assinanet.entity.enums.StatusEnum;
import br.com.assinanet.util.CommonsUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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
public class Papel {



    @Id
    @GeneratedValue
    @Column(columnDefinition = "uniqueidentifier")
    @Type(type = "uuid-char")
    private UUID id;


    @ManyToOne
    @JoinColumn(name = "idCliente")
    @NotNull
    private Cliente cliente;

    private String identificacao;

    @NotNull
    private String nome;

    private Boolean assina;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private StatusEnum status;


    // pessoas fisicas, somente para cadastro de pessoas juridicas
    @OneToMany(mappedBy = "papel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<PapelTipoCliente> papelTipoClientes;

    public List<PapelTipoCliente> getPapelTipoClientes() {

        if (papelTipoClientes == null)
            return Collections.emptyList();

        papelTipoClientes.sort((o1, o2) -> {

            int compare = 0;

            if (!CommonsUtil.semValor(o1.getSegmento()) && !CommonsUtil.semValor(o2.getSegmento())) {
                compare = CommonsUtil.stringValue(o1.getSegmento().getNome().toUpperCase()).compareTo(

                        CommonsUtil.stringValue(o2.getSegmento().getNome().toUpperCase())

                );
            }
            return compare;
        });

        return papelTipoClientes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Papel papel = (Papel) o;
        return id != null && Objects.equals(id, papel.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
