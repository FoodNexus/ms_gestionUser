package tn.esprit.ms_gestionuser.security;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;
import tn.esprit.ms_gestionuser.repositories.UserRepository;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Convertisseur personnalisé pour extraire les rôles Keycloak du token JWT.
 * Keycloak stocke les rôles dans "realm_access.roles" et "resource_access.[client].roles".
 * Ce convertisseur les transforme en GrantedAuthority pour Spring Security.
 */
@Component
@RequiredArgsConstructor
public class KeycloakJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserRepository userRepository;
    private final JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // --- VÉRIFICATION DU STATUT DU COMPTE (isActif) ---
        // On récupère le sub (ID Keycloak) du token
        String keycloakId = jwt.getSubject();
        
        // On vérifie dans la base si l'utilisateur est bloqué
        userRepository.findByKeycloakId(keycloakId).ifPresent(user -> {
            if (!user.isActif()) {
                throw new DisabledException("Votre compte est bloqué par l'administrateur.");
            }
        });

        Collection<GrantedAuthority> authorities = Stream.concat(
                defaultConverter.convert(jwt).stream(),
                extractKeycloakRoles(jwt).stream()
        ).collect(Collectors.toSet());

        return new JwtAuthenticationToken(jwt, authorities, jwt.getClaimAsString("preferred_username"));
    }

    /**
     * Extrait les rôles depuis realm_access.roles du token Keycloak.
     */
    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractKeycloakRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null || realmAccess.isEmpty()) {
            return Collections.emptySet();
        }

        List<String> roles = (List<String>) realmAccess.get("roles");
        if (roles == null) {
            return Collections.emptySet();
        }

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toSet());
    }
}
