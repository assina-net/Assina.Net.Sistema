package br.com.assinanet.util;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.Get;

import javax.servlet.http.HttpServletRequest;

public class RequestUtil {


    public static String retornaIpCliente(HttpServletRequest request) {
        String remoteAddress = "";
        if (request != null) {
            remoteAddress = request.getHeader("X-Forwarded-For");
            if (remoteAddress == null || "".equals(remoteAddress)) {
                remoteAddress = request.getRemoteAddr();
            }
        }
        return remoteAddress;
    }

    public static String retornaProtocolo(HttpServletRequest request) {

        if (request != null) {
            return request.getHeader("X-Forwarded-Proto");
        }
        return "vazio";
    }

    public static Boolean retornaIsSecure(HttpServletRequest request) {
        String proto = "";
        if (request != null) {
            proto = request.getHeader("X-Forwarded-Proto");
            if (proto == null || "".equals(proto)) {
                return request.isSecure();
            } else {
                return CommonsUtil.mesmoValor("https", proto);
            }
        }
        return false;
    }

}
