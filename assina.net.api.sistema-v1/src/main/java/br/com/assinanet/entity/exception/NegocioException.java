package br.com.assinanet.entity.exception;

import br.com.assinanet.util.Util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Exceção a ser lançada na ocorrência de falhas provenientes da manipulacao de arquivos.
 *
 * @author Samuel Oliveira
 */

public class NegocioException extends Exception {

    private static final long serialVersionUID = -2093312986951988270L;

    private String message;

    /**
     * Construtor da classe.
     */
    public NegocioException(Throwable e) {
        String msg = e.getMessage();
        if (Util.verifica(msg).isPresent()) {
            StringWriter errorMsg = new StringWriter();
            e.printStackTrace(new PrintWriter(errorMsg));
            msg = errorMsg.toString();
        }
        this.setMessage(msg);
    }

    public NegocioException(String msg) {
        this.setMessage(msg);
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message vo set
     */
    public void setMessage(String message) {
        this.message = message;
    }

}