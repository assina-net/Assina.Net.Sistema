package br.com.assinanet.entity;

import br.com.assinanet.entity.enums.StatusEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

/**
 * @author Alexandre Murta - amurta@gmail.com
 */
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SistemaAtributo {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uniqueidentifier")
    @Type(type = "uuid-char")
    private UUID id;

    /**
     * Nome do atributo
     */
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idTipoAtributo")
    private SistemaTipoAtributo tipoAtributo;

    /**
     * Valor do atributo
     */
    @Lob
    @Column(nullable = false)
    @Nationalized
    private String valorAtributo;

    private Date dataAlteracao;

    @ManyToOne
    @JoinColumn(name = "idCliente")
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    private StatusEnum status;

}


