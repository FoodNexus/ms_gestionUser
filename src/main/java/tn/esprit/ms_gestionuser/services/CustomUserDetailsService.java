package tn.esprit.ms_gestionuser.services;


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tn.esprit.ms_gestionuser.entities.User;
import tn.esprit.ms_gestionuser.repositories.UserRepository;

import java.util.ArrayList;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. On cherche votre utilisateur dans la base de données
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        // 2. On traduit votre RoleType en "GrantedAuthority" que le vigile comprend
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().name());

        // 3. On crée l'objet Spring Security avec l'email, le mot de passe ET le rôle !
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(authority) // <-- C'est ça qui manquait !
        );
    }

}
