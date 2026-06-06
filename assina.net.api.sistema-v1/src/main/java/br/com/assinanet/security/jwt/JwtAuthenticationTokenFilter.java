package br.com.assinanet.security.jwt;

import br.com.assinanet.entity.Cliente;
import br.com.assinanet.entity.Usuario;
import br.com.assinanet.entity.enums.PerfilEnum;
import br.com.assinanet.security.service.JwtUserDetailsService;
import br.com.assinanet.service.ClienteService;
import br.com.assinanet.service.UsuarioService;
import br.com.assinanet.util.CommonsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author Samuel Oliveira - samuk.exe@hotmail.com
 * Data: 20/08/2018 - 22:42
 */
@Slf4j
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUserDetailsService userDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ClienteService clienteService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || "/swagger-ui.html".equals(path);
    }

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authToken = request.getHeader("Authorization");
        String username = jwtTokenUtil.getUsernameFromToken(authToken);
        Integer empresaId = jwtTokenUtil.getOrganizationFromToken(authToken);
        String perfil = jwtTokenUtil.getPerfilFromToken(authToken);
        UUID usuarioId  = jwtTokenUtil.getIdFromToken(authToken);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails;
            if (!CommonsUtil.mesmoValor(perfil, PerfilEnum.ROLE_INTEGRACAO.toString())) {
                userDetails = this.userDetailsService.loadUserByUsername(username);
                Usuario usuario = usuarioService.findByLogin(username);
                usuarioId = usuario.getId();
            }else{
                Cliente cliente = clienteService .findById(usuarioId);
                 userDetails = new JwtUser(
                        cliente.getId(),
                        cliente.getPessoa().getCpfCnpj(),
                        cliente.getPessoa().getCpfCnpj(),
                        1,
                        new ArrayList<GrantedAuthority>(List.of(new SimpleGrantedAuthority(PerfilEnum.ROLE_USUARIO.toString()))));
            }

            if (jwtTokenUtil.validateToken(authToken, userDetails)) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                request.setAttribute("empresaAtual", empresaId);
                request.setAttribute("usuario", usuarioId);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                log.info("Usúario Logado " + username + ", configurado no contexto de segurança");
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                SecurityContextHolder.clearContext();
            }
        } else {
            SecurityContextHolder.clearContext();
        }
        chain.doFilter(request, response);
    }
}

