package br.com.assinanet.models;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

public class CarimboTempoModel {

    @Getter
    @Setter
    Date dataCarimboTempo;

    @Getter
    @Setter
    private String gmtCarimboTempo;

    @Getter
    @Setter
    private String carimboTempo;

}
