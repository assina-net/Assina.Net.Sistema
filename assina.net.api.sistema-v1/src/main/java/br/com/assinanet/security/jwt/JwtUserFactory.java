package br.com.assinanet.security.jwt;

import br.com.assinanet.entity.Usuario;
import br.com.assinanet.entity.enums.PerfilEnum;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Samuel Oliveira - samuk.exe@hotmail.com
 * Data: 20/05/2018 - 22:20
 */
public class JwtUserFactory {
    private JwtUserFactory() {
    }

    public static JwtUser create(Usuario usuario) {
        return new JwtUser(
                usuario.getId(),
                usuario.getLogin(),
                usuario.getSenha(),
                1,
                mapToGrantedAuthorities(usuario.getPerfil())
        );
    }

    private static List<GrantedAuthority> mapToGrantedAuthorities(PerfilEnum perfilEnum) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (perfilEnum != null)
            authorities.add(new SimpleGrantedAuthority(perfilEnum.toString()));
        return authorities;
    }
}

