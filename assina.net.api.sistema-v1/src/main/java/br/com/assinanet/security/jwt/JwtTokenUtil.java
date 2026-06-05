package br.com.assinanet.security.jwt;

import br.com.assinanet.entity.Cliente;
import br.com.assinanet.entity.enums.PerfilEnum;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Component
public class JwtTokenUtil implements Serializable {

    private static final String CLAIM_KEY_USERNAME = "sub";
    private static final String CLAIM_KEY_CREATED = "created";
    private static final long serialVersionUID = -3301605591108950415L;

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private Long expiration;

    public String getUsernameFromToken(String token) {
        String username;
        try {
            final Claims claims = getClaimsFromToken(token);
            username = claims.getSubject();
        } catch (Exception e) {
            username = null;
        }
        return username;
    }

    Integer getOrganizationFromToken(String token) {
        Integer organization;
        try {
            final Claims claims = getClaimsFromToken(token);
            organization = Integer.valueOf(claims.get("organization").toString());
        } catch (Exception e) {
            organization = null;
        }
        return organization;
    }

    String getPerfilFromToken(String token) {
        String perfil;
        try {
            final Claims claims = getClaimsFromToken(token);
            perfil = claims.get("perfil").toString();
        } catch (Exception e) {
            perfil = null;
        }
        return perfil;
    }

    UUID getIdFromToken(String token) {
        UUID id;
        try {
            final Claims claims = getClaimsFromToken(token);
            id = UUID.fromString(claims.get("id").toString());
        } catch (Exception e) {
            id = null;
        }
        return id;
    }

    private Date getExpirationDateFromToken(String token) {
        Date expiration;
        try {
            final Claims claims = getClaimsFromToken(token);
            expiration = claims.getExpiration();
        } catch (Exception e) {
            expiration = null;
        }
        return expiration;
    }

    private Claims getClaimsFromToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            claims = null;
        }
        return claims;
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        claims.put(CLAIM_KEY_USERNAME, userDetails.getUsername());
        claims.put("organization", ((JwtUser) userDetails).getOrganization());

        //claims.put("perfil",userDetails.getAuthorities().to);
        //claims.put("id",  userDetails).getId());

        final Date createdDate = new Date();
        claims.put(CLAIM_KEY_CREATED, createdDate);

        return doGenerateToken(claims);
    }

    public String generateTokenCliente(Cliente cliente) {
        Map<String, Object> claims = new HashMap<>();

        var userDetails = new JwtUser(
                cliente.getId(),
                cliente.getPessoa().getCpfCnpj(),
                cliente.getPessoa().getCpfCnpj(),
                1,
                new ArrayList<GrantedAuthority>(List.of(new SimpleGrantedAuthority(PerfilEnum.ROLE_USUARIO.toString()))));

        claims.put(CLAIM_KEY_USERNAME, cliente.getPessoa().getCpfCnpj());
        claims.put("organization", userDetails.getOrganization());
        claims.put("perfil", PerfilEnum.ROLE_INTEGRACAO.toString());
        claims.put("id", cliente.getId());

        final Date createdDate = new Date();
        claims.put(CLAIM_KEY_CREATED, createdDate);

        return doGenerateTokenCliente(claims);
    }

    private String doGenerateToken(Map<String, Object> claims) {
        final Date createdDate = (Date) claims.get(CLAIM_KEY_CREATED);
        final Date expirationDate = new Date(createdDate.getTime() + expiration * 1000);
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    private String doGenerateTokenCliente(Map<String, Object> claims) {
        final Date createdDate = (Date) claims.get(CLAIM_KEY_CREATED);
        final Date expirationDate = DateUtils.addYears(createdDate, 1);
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    private SecretKey getSigningKey() {
        try {
            byte[] keyBytes = MessageDigest.getInstance("SHA-512")
                    .digest(secret.getBytes(StandardCharsets.UTF_8));
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo SHA-512 indisponivel", e);
        }
    }

    public Boolean canTokenBeRefreshed(String token) {
        return (!isTokenExpired(token));
    }

    public String refreshToken(String token) {
        String refreshedToken;
        try {
            final Claims claims = getClaimsFromToken(token);
            claims.put(CLAIM_KEY_CREATED, new Date());
            refreshedToken = doGenerateToken(claims);
        } catch (Exception e) {
            refreshedToken = null;
        }
        return refreshedToken;
    }

    Boolean validateToken(String token, UserDetails userDetails) {
        JwtUser user = (JwtUser) userDetails;
        final String username = getUsernameFromToken(token);
        return (
                username.equals(user.getUsername())
                        && !isTokenExpired(token));
    }
}
