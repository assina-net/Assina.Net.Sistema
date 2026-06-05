package br.com.assinanet.entity;

import br.com.assinanet.entity.enums.SistemaTipoAtributoEnum;
import br.com.assinanet.entity.enums.SistemaTipoValorEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * @author Alexandre Murta - amurta@gmail.com
 */
@AllArgsConstructor
@NoArgsConstructor
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SistemaTipoAtributo {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uniqueidentifier")
    @Getter
    @Setter
    private UUID id;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private SistemaTipoAtributoEnum tipoAtributo;

    @Getter
    @Setter
    private String  descricao;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private SistemaTipoValorEnum tipoValor;

}
