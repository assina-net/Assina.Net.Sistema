package br.com.assinanet.response;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Samuel Oliveira - samuk.exe@hotmail.com
 */
public class Response<T> {

    private T data;

    private List<String> errors;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public List<String> getErrors() {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

}
