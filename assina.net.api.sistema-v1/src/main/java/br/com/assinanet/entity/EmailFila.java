package br.com.assinanet.entity;

import br.com.assinanet.entity.enums.TipoEnvioMsgEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Alexandre Murta - amurta@gmail.com
 */
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
public class EmailFila {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uniqueidentifier")
    @Type(type = "uuid-char")
    private UUID id;

    @NotNull
    private Date dataEnvio;

    @Lob
    @NotNull
    private String email; //objeto seralizado do e-mail

    @Lob
    private String logSucesso; //objeto de log seralizado para sucesso

    @Lob
    private String logFalha; //objeto de log seralizado para falha

    private Boolean enviado;

    private TipoEnvioMsgEnum tipoEnvio = TipoEnvioMsgEnum.EMAIL;

    @Lob
    private String erroEnvio;

    private Date dataUltimoEnvio;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        EmailFila emailFila = (EmailFila) o;
        return id != null && Objects.equals(id, emailFila.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
