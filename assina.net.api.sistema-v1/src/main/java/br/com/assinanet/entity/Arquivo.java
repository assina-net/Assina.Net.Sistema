package br.com.assinanet.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Arquivo {


    @Id
    @GeneratedValue
    @Column(columnDefinition = "uniqueidentifier")
    private UUID id;

    @NotNull
    @ManyToOne( fetch = FetchType.LAZY)
    @JoinColumn(name = "idCliente", updatable = false)
    private Cliente cliente; //id da tabela de usuario

    @ManyToOne( fetch = FetchType.LAZY)
    @JoinColumn(name = "idContrato", nullable = false, updatable = false)
    private Contrato contrato;

}
