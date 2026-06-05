package br.com.assinanet.security.service;

import br.com.assinanet.entity.Usuario;
import br.com.assinanet.security.jwt.JwtUserFactory;
import br.com.assinanet.service.UsuarioService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @author Samuel Oliveira - samuk.exe@hotmail.com
 * Data: 20/08/2018 - 22:47
 */
@Service
public class JwtUserDetailsService implements UserDetailsService {

    private final UsuarioService userService;

    public JwtUserDetailsService(UsuarioService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {

        Usuario user = userService.findByLogin(login);
        if (user == null) {
            throw new UsernameNotFoundException(String.format("Nenhum usuário encontrado com o login '%s'.", login));
        } else {
            return JwtUserFactory.create(user);
        }
    }
}
