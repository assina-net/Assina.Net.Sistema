package br.com.assinanet.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.ArrayList;
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
public class Pais {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "uniqueidentifier")
    @Type(type = "uuid-char")
    private UUID id;

    @Getter
    @Setter
    private String nome;

    @Getter
    @Setter
    private String codigoPais;

    @Getter
    @Setter
    private Integer codigoTelefonePais;

    @Getter
    @Setter
    private String continenteCodigo;

    @Getter
    @Setter
    private String continente;

    @Getter
    @Setter
    private BigInteger geonameId;

    @Getter
    @Setter
    private String capital;

    @Getter
    @Setter
    private String bandeira;

    @Getter
    @Setter
    @Type(type="org.hibernate.type.StringNVarcharType")
    private String bandriaEmoji;

    @Getter
    @Setter
    private String bandeiraEmojiUnicode;

    // pessoas fisicas, somente para cadastro de pessoas juridicas
    @OneToMany(mappedBy = "pais", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Getter
    @Setter
    @ToString.Exclude
    private List<PaisIdiomas> idimoas = new ArrayList<>(0);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Pais pais = (Pais) o;
        return id != null && Objects.equals(id, pais.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
