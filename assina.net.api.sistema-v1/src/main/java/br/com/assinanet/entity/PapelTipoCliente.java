package br.com.assinanet.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
public class PapelTipoCliente {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uniqueidentifier")
    private UUID id;


    @ManyToOne
    @JoinColumn(name = "idPapel")
    @NotNull
    private Papel papel;

    @OneToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "idSegmento")
    private Segmento segmento; //id da tabela de segmento


    @JsonBackReference(value = "papel")
    public Papel getPapel() {
        return papel;
    }


}
