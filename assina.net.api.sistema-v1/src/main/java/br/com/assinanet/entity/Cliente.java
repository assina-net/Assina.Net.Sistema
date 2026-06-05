package br.com.assinanet.entity;

import br.com.assinanet.entity.enums.StatusEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
public class Cliente {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(columnDefinition = "uniqueidentifier")
    //@Column(columnDefinition = "BINARY(16)")
    @Type(type = "uuid-char")
    private UUID id;

    @NotNull
    @OneToOne(cascade = CascadeType.ALL , fetch = FetchType.LAZY )
    @JoinColumn(name = "idPessoa")
    private Pessoa pessoa; //id da tabela de usuario


    @OneToOne(cascade = CascadeType.ALL , fetch = FetchType.LAZY )
    @JoinColumn(name = "idSegmento")
    private Segmento segmento; //id da tabela de segmento


    //@OneToOne(cascade = CascadeType.ALL , fetch = FetchType.LAZY )
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPlano")
    private Plano plano; //id da tabela de plano

    @Enumerated(EnumType.STRING)
    @NotNull
    private StatusEnum status;

    @NotNull
    private Date dataInicioContrato;

    private Date dataFimContrato;

    private Boolean naoMostrar;

    private String indicacao;

    public String getDescStatus() {
        if (this.status == null)
            return null;
        return this.status.getDescricao();
    }

}
