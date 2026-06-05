package br.com.assinanet.response;


import br.com.assinanet.entity.Cliente;
import br.com.assinanet.entity.ContratoDocumento;
import br.com.assinanet.entity.enums.StatusDocumentoEnum;
import br.com.assinanet.util.CommonsUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import rufus.lzstring4java.LZString;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Getter
@Setter
public class ContratoDocumentoVisualizaResponse {


    private UUID id;
    private String documentoPDF;

    private byte[] documentoBytes;
    private StatusDocumentoEnum statusDocumento;
    @JsonIgnoreProperties
    private Boolean validacaoOnLine;

    @JsonIgnoreProperties
    private Cliente cliente;

    private List<ContratoDocumentoVisualizaAssinaturasResponse> assinaturas;

    //public ContratoDocumentoVisualizaResponse(Optional<ContratoDocumento> id, byte[] documentoPDF, boolean originalBytes) {
    //   this(id, documentoPDF, (Boolean) null, null, originalBytes);
    //}
    public ContratoDocumentoVisualizaResponse(Optional<ContratoDocumento> id, byte[] documentoPDF, boolean originalBytes) {
        if  ( !CommonsUtil.semValor(id) && id.isPresent() ) {
            this.id = id.get().getId();
            this.validacaoOnLine = id.get().getTipoDocumento().getValidacaoOnLine();
        }
        if (!CommonsUtil.booleanValue(originalBytes) && !CommonsUtil.semValor(documentoPDF) )
            this.documentoPDF = LZString.compressToUTF16(Base64.getEncoder().encodeToString(documentoPDF));
        else
            this.documentoBytes = documentoPDF;
        this.validacaoOnLine = validacaoOnLine;
        this.cliente = cliente;
    }

    public ContratoDocumentoVisualizaResponse(Optional<ContratoDocumento> id, byte[] documentoPDF, Boolean validacaoOnLine, Cliente cliente, boolean originalBytes ) {
        if (id.isPresent() ) {
            this.id = id.get().getId();
            this.validacaoOnLine = id.get().getTipoDocumento().getValidacaoOnLine();
        }
        if (!CommonsUtil.booleanValue(originalBytes))
            this.documentoPDF = LZString.compressToUTF16(Base64.getEncoder().encodeToString(documentoPDF));
        else
            this.documentoBytes = documentoPDF;

        this.validacaoOnLine = validacaoOnLine;
        this.cliente = cliente;
    }


    public ContratoDocumentoVisualizaResponse(UUID id, byte[] documentoPDF, Boolean validacaoOnLine, Cliente cliente ) {
        this.id =  id;
        if (documentoPDF != null) {
            this.documentoPDF = LZString.compressToUTF16(Base64.getEncoder().encodeToString(documentoPDF));
        }

        this.validacaoOnLine = validacaoOnLine;
        this.cliente = cliente;
    }

    public ContratoDocumentoVisualizaResponse( Optional<ContratoDocumento> id, byte[] documentoPDF, StatusDocumentoEnum statusDocumento) {
        if (id.isPresent() ) {
            this.id = id.get().getId();
            this.validacaoOnLine = id.get().getTipoDocumento().getValidacaoOnLine();
        }
        if (documentoPDF != null)
            this.documentoPDF = LZString.compressToUTF16(Base64.getEncoder().encodeToString(documentoPDF));

        this.statusDocumento = statusDocumento;
    }
    public ContratoDocumentoVisualizaResponse( UUID id, byte[] documentoPDF) {
        if (documentoPDF != null) {
            this.id = id;
            this.documentoPDF = LZString.compressToUTF16(Base64.getEncoder().encodeToString(documentoPDF));
        }
    }
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    public byte[] getDocumento(){
        return Base64.getDecoder().decode(LZString.decompressFromUTF16(this.documentoPDF));
    }
}
