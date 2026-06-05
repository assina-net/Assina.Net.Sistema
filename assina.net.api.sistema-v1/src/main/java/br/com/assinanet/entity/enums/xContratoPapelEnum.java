package br.com.assinanet.entity.enums;

/**
 * @author Samuel Oliveira - samuk.exe@hotmail.com
 */
public enum xContratoPapelEnum {

    PARTE("Parte", "Assinar como parte", new xTipoClienteEnum[]{xTipoClienteEnum.CONTRATOSIMPLES}),
    TESTEMUNHA("Testemunha", "Assinar como testemunha", new xTipoClienteEnum[]{xTipoClienteEnum.CONTRATOSIMPLES}),
    ADMINSITRADOR("Administrador", "Assinar como administrador", new xTipoClienteEnum[]{}),
    AVALISTA("Avalista", "Assinar como avalista", new xTipoClienteEnum[]{xTipoClienteEnum.FACTORING, xTipoClienteEnum.FIDC}),
    CEDENTE("Cedente", "Assinar como cedente", new xTipoClienteEnum[]{xTipoClienteEnum.FACTORING, xTipoClienteEnum.FIDC}),
    CESSIONARIO("Cessionário", "Assinar como cessionário", new xTipoClienteEnum[]{xTipoClienteEnum.FACTORING, xTipoClienteEnum.FIDC}),
    CONTRATADA("Contratada", "Assinar como contratada", new xTipoClienteEnum[]{}),
    CONTRATANTE("Contratante", "Assinar como contratante", new xTipoClienteEnum[]{}),
    DEVEDORSOLIDARIO("Devedor Solidário", "Assinar como devedor solidário", new xTipoClienteEnum[]{xTipoClienteEnum.FACTORING, xTipoClienteEnum.FIDC}),
    EMITENTE("Emitente", "Assinar como emitente", new xTipoClienteEnum[]{xTipoClienteEnum.FACTORING, xTipoClienteEnum.FIDC}),
    ENDOSSANTE("Endossante", "Assinar como endossante", new xTipoClienteEnum[]{xTipoClienteEnum.FACTORING, xTipoClienteEnum.FIDC}),
    ENDOSSATARIO("Endossatário", "Assinar como endossatário", new xTipoClienteEnum[]{xTipoClienteEnum.FACTORING, xTipoClienteEnum.FIDC}),
    FOMENTADA("Fomentada", "Assinar como Fomentada", new xTipoClienteEnum[]{xTipoClienteEnum.FACTORING, xTipoClienteEnum.FIDC}),
    FOMENTADORA("Fomentadora", "Assinar como Fomentadora", new xTipoClienteEnum[]{xTipoClienteEnum.FACTORING, xTipoClienteEnum.FIDC}),
    GESTOR("Gestor", "Assinar como gestor", new xTipoClienteEnum[]{xTipoClienteEnum.FIDC}),
    INTERVENIENTE("Interveniente", "Assinar como interveniente", new xTipoClienteEnum[]{xTipoClienteEnum.FIDC}),
    COMPRADORA("Compradora", "Assinar como parte compradora", new xTipoClienteEnum[]{}),
    VENDEDORA("Vendedora", "Assinar como parte vendedora", new xTipoClienteEnum[]{}),
    PROCURADOR("Procurador", "Assinar como procurador", new xTipoClienteEnum[]{}),
    REPRESENTANTELEGAL("Representante Legal", "Assinar como representante legal", new xTipoClienteEnum[]{xTipoClienteEnum.FIDC}),
    RESPONSAVELSOLIDARIO("Responsável Solidário", "Assinar como responsável solidário", new xTipoClienteEnum[]{xTipoClienteEnum.FIDC}),
    VALIDADOR("Validador", "Assinar como validador", new xTipoClienteEnum[]{}),
    RECEBEDOR("Acusador de recebimento", "Assinar para acusar recebimento", new xTipoClienteEnum[]{}),
    APROVADOR("Aprovador", "Assinar para aprovar", new xTipoClienteEnum[]{}),
    FIADOR("Fiador", "Assinar como fiador", new xTipoClienteEnum[]{}),
    LOCADOR("Locador", "Assinar como locador", new xTipoClienteEnum[]{}),
    LOCATARIO("Locatário", "Assinar como locatário", new xTipoClienteEnum[]{}),
    SEGURADO("Segurado", "Assinar como segurado", new xTipoClienteEnum[]{xTipoClienteEnum.CORRETORASEGUROS}),
    CORRETORASEGUROS("Corretora de Seguros", "Assinar como corretora de seguros", new xTipoClienteEnum[]{xTipoClienteEnum.CORRETORASEGUROS});


    private final String descricaoCombo;
    private final String descricao;
    private final xTipoClienteEnum[] tipoCliente;

    xContratoPapelEnum(String descricao, String descricaoCombo, xTipoClienteEnum[] tipoCliente) {
        this.descricao = descricao;
        this.descricaoCombo = descricaoCombo;
        this.tipoCliente = tipoCliente;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getDescricaoCombo() {
        return descricaoCombo;
    }

    public xTipoClienteEnum[] getTipoCliente() {
        return this.tipoCliente;
    }

//    public static List<ComboListResponse> getListCombo() {
//        List<ComboListResponse> lstEnum = new ArrayList<>(0);
//        xContratoPapelEnum[] values = xContratoPapelEnum.values();
//        for (xContratoPapelEnum contratoPapelEnum : xContratoPapelEnum.values()) {
//            lstEnum.add(new ComboListResponse(contratoPapelEnum.toString(), contratoPapelEnum.getDescricaoCombo(), contratoPapelEnum.getDescricao(), contratoPapelEnum.getTipoCliente()));
//        }
//
//        lstEnum.sort(Comparator.comparing(ComboListResponse::getLabel));
//
//        return lstEnum;
//    }
}
