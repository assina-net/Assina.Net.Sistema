package br.com.assinanet.entity.enums;

import lombok.Getter;

/**
 * @author Alexandre Murta - amurta@gmail.com
 */
public enum CodigoErrosEnvioEnum {

    //region  Envio
    Erro_1(1, "O código do custodiante deve ser válido."),
    Erro_2(2, "O código do custodiante deve ser um código cadastrado."),
    Erro_3(3, "O tipo de informação indicada para o contrato deve ser numérico."),
    Erro_4(4, "O tipo de informação indicada para o contrato deve ser \"1\" (procuradores) ou \"2\"(empresas)."),
    Erro_5(5, "O CNPJ da empresa do procurador (indicado para o contrato) deve ser numérico."),
    Erro_6(6, "O CNPJ da empresa do procurador (indicado para o contrato) deve ter exatamente 14 dígitos."),
    Erro_7(7, "O digito verificador do CNPJ da empresa do procurador (indicado para o contrato) não confere."),
    Erro_8(8, "O CPF do procurador (indicado para o contrato) deve ser numérico."),
    Erro_9(9, "O CPF do procurador (indicado para o contrato) deve ter exatamente 11 dígitos."),
    Erro_10(10, "O digito verificador do CPF do procurador (indicado para o contrato) não confere."),
    Erro_11(11, "Formato do e-mail do procurador (indicado para o contrato) está inválido."),
    Erro_47(47, "Deve ser indicado se o procurador é do contrato (0) ou da duplicata (1)."),
    //Erro_12(12, "O categoria da empresa (indicada para o contrato) deve estar cadastrada no MLA." ),
    //Erro_13(13, "O CNPJ da empresa (indicada para o contrato) deve ser numérico." ),
    //Erro_14(14, "O CNPJ da empresa (indicada para o contrato) deve ter exatamente 14 dígitos." ),
    //Erro_15(15, "O digito verificador do CNPJ da empresa (indicada para o contrato) não confere." ),
    //Erro_16(16, "O CNPJ da empresa (indicada para o contrato) deve estar cadastrada no MLA." ),
    //Erro_17(17, "O CNPJ da empresa (indicada para o contrato) deve estar associada a categoria informada (no MLA)." ),
    Erro_18(18, "O tipo do contrato não está cadastrado."),
    Erro_19(19, "O tipo do contrato não está ativado para alguma empresa indicada no contrato."),
    Erro_20(20, "A moeda não está cadastrada."),
    Erro_21(21, "O valor do contrato deve ser numérico e decimal."),
    Erro_22(22, "O formato do valor do contrato deve ser igual a 999999999.99."),
    Erro_23(23, "O CNPJ da empresa do validador (indicado para o contrato) deve ser numérico."),
    Erro_24(24, "O CNPJ da empresa do validador (indicado para o contrato) deve ter exatamente 14 dígitos."),
    Erro_25(25, "O digito verificador do CNPJ da empresa do validador (indicado para o contrato) não confere."),
    Erro_26(26, "O CPF do validador (indicado para o contrato) deve ser numérico."),
    Erro_27(27, "O CPF do validador (indicado para o contrato) deve ter exatamente 11 dígitos."),
    Erro_28(28, "O digito verificador do CPF do validador (indicado para o contrato) não confere."),
    Erro_29(29, "Formato do e-mail do validador (indicado para o contrato) está inválido."),
    Erro_30(30, "O documento do contrato deve estar no formato Base64."),
    Erro_31(31, "O documento do contrato deve ser um arquivo TXT ou PDF."),
    Erro_32(32, "Tag é obrigatória e portanto deve ter conteúdo ou subtags."),
    Erro_33(33, "Tag é obrigatória e portanto deve ser informada."),
    Erro_35(35, ""),
    Erro_36(36, "É obrigatório que seja informado ao menos um procurador ou validador para cada uma das empresas do contrato."),
    Erro_37(37, "É obrigatório que seja informado ao menos um procurador para cada uma das empresas das duplicatas."),
    Erro_38(38, "É obrigatório que seja informado ao menos um procurador para o contrato."),
    Erro_39(39, "É obrigatório que seja informado ao menos um procurador para as duplicatas."),
    Erro_40(40, "É obrigatório informar se a assinatura das duplicatas é ou não obrigatória."),
    Erro_41(41, "Alguma empresa selecionada não possui cadastrado procurador com poderes para assinar o contrato."),
    Erro_42(42, "O arquivo de duplicatas deve estar no formato Base64."),
    Erro_43(43, "O arquivo de duplicatas deve ser um arquivo CSV."),
    Erro_44(44, "Um ou mais procuradores do contrato possuem mais de um papel por tag."),
    Erro_45(45, "Uma ou mais linhas (duplicatas), contidas no arquivo CSV, não possui 25 campos."),
    //Erro_126(126, "Tag [nome da tag] é obrigatória, entretanto não foi informada." ),
    //Erro_127(127, "Tag [nome da tag] não pode ser informada mais de uma vez." ),
    Erro_128(128, "O remetente informado [e-mail do remetente] não existe na base de usuário"),
    //endregion
    //region Exclusao Procurador
    Erro_100(100, "O procurador é inexistente pra este contrato."),
    Erro_101(101, "O procurador não foi indicado para o contrato."),
    Erro_102(102, "O procurador informado já realizou a assinatura do contrato e não pode ser excluído."),
    Erro_103(103, "Papel do procurador inexistente."),
    Erro_104(104, "E-mail do procurador inexistente."),
    Erro_106(106, "O CNPJ da empresa do procurador (indicado para o contrato) deve ser numérico."),
    Erro_107(107, "O CNPJ da empresa do procurador (indicado para o contrato) deve ter exatamente 14 dígitos."),
    Erro_108(108, "O digito verificador do CNPJ da empresa do procurador (indicado para o contrato) não confere."),
    Erro_109(109, "O CPF do procurador (indicado para o contrato) deve ser numérico."),
    Erro_110(110, "O CPF do procurador (indicado para o contrato) deve ter exatamente 11 dígitos."),
    Erro_111(111, "O digito verificador do CPF do procurador (indicado para o contrato) não confere."),
    Erro_112(112, "Formato do e-mail do procurador (indicado para o contrato) está inválido."),
    Erro_113(113, "Tag é obrigatória e portanto deve ter conteúdo ou subtags."),
    Erro_114(114, "Tag sem conteúdo ou subtags não deve ser informada."),
    Erro_115(115, "Tag é obrigatória e portanto deve ser informada."),
    Erro_117(117, "É obrigatório que seja informado ao menos um procurador para cada uma das empresas do contrato.");
    //endregion

    @Getter
    private final int codigo;
    @Getter
    private final String mensagemErro;

    CodigoErrosEnvioEnum(int codigo, String mensagemErro) {
        this.mensagemErro = mensagemErro;
        this.codigo = codigo;
    }

    public static CodigoErrosEnvioEnum find(int codigo) {
        for (CodigoErrosEnvioEnum e : CodigoErrosEnvioEnum.values()) {
            if (e.getCodigo() == codigo) {
                return e;
            }
        }
        return null;
    }
}


