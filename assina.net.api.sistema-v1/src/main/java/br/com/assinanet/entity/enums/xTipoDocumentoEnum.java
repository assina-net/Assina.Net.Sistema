package br.com.assinanet.entity.enums;

/**
 * @author Samuel Oliveira - samuel@swconsultoria.com.br
 */
public enum xTipoDocumentoEnum {

    ADITIVO("ADITIVO", 1, new xTipoClienteEnum[]{xTipoClienteEnum.FACTORING, xTipoClienteEnum.FIDC}, new xContratoPapelEnum[]{xContratoPapelEnum.FOMENTADA, xContratoPapelEnum.FOMENTADORA}),
    DUPLICATA("DUPLICATA", 2, new xTipoClienteEnum[]{xTipoClienteEnum.FACTORING, xTipoClienteEnum.FIDC}, new xContratoPapelEnum[]{xContratoPapelEnum.EMITENTE, xContratoPapelEnum.AVALISTA, xContratoPapelEnum.ENDOSSANTE}),
    CONTRATO("CONTRATO", 3, new xTipoClienteEnum[]{xTipoClienteEnum.CONTRATOSIMPLES}, new xContratoPapelEnum[]{xContratoPapelEnum.PARTE, xContratoPapelEnum.TESTEMUNHA}),
    CSVDUPLICATAS("CSV DUPLICATAS", 4, new xTipoClienteEnum[]{}, new xContratoPapelEnum[]{}),
    CARTA_CESSAO("CARTA CESSÃO", 5, new xTipoClienteEnum[]{xTipoClienteEnum.FACTORING, xTipoClienteEnum.FIDC}, new xContratoPapelEnum[]{xContratoPapelEnum.EMITENTE}),
    CONTRATO_SEGURO("CONTRATO SEGURO", 6, new xTipoClienteEnum[]{xTipoClienteEnum.CORRETORASEGUROS}, new xContratoPapelEnum[]{xContratoPapelEnum.CORRETORASEGUROS, xContratoPapelEnum.SEGURADO}),
    ADENDO("ADENDO", 7, new xTipoClienteEnum[]{xTipoClienteEnum.CORRETORASEGUROS}, new xContratoPapelEnum[]{xContratoPapelEnum.CORRETORASEGUROS, xContratoPapelEnum.SEGURADO});


    private final String descricao;

    private final Integer opcao;

    private final xTipoClienteEnum[] tipoCliente;

    private final xContratoPapelEnum[] papel;

    xTipoDocumentoEnum(String descricao, Integer opcao, xTipoClienteEnum[] tipoCliente, xContratoPapelEnum[] papel) {
        this.descricao = descricao;
        this.opcao = opcao;
        this.tipoCliente = tipoCliente;
        this.papel = papel;
    }

    public String getDescricao() {
        return descricao;
    }

    public Integer getOpcao() {
        return opcao;
    }

    public xTipoClienteEnum[] getTipoCliente() {
        return tipoCliente;
    }

    public xContratoPapelEnum[] getPapel() {
        return papel;
    }



    public xTipoDocumentoEnum find(Boolean opcao) {
        for (xTipoDocumentoEnum status : xTipoDocumentoEnum.values()) {
            if (opcao.equals(status.getOpcao())) {
                return status;
            }
        }
        return null;
    }

//
//    public static List<ComboListResponse> getListCombo() {
//        List<ComboListResponse> lstEnum = new ArrayList<>(0);
//        xTipoDocumentoEnum[] values = xTipoDocumentoEnum.values();
//        for (xTipoDocumentoEnum tipoDocumentoEnum : xTipoDocumentoEnum.values()) {
//
//            lstEnum.add(new ComboListResponse(tipoDocumentoEnum.toString(), tipoDocumentoEnum.getDescricao(), tipoDocumentoEnum.getDescricao(),
//                    tipoDocumentoEnum.getTipoCliente(), tipoDocumentoEnum.getPapel()));
//        }
//        lstEnum.sort(Comparator.comparing(ComboListResponse::getLabel));
//
//        return lstEnum;
//    }

}

