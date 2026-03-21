package tn.esprit.ms_gestionuser.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.esprit.ms_gestionuser.entities.User;
import tn.esprit.ms_gestionuser.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(User user) {
        // 1. Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Erreur : Cet email est déjà utilisé !");
        }

        // 2. Crypter le mot de passe
        String motDePasseCrypte = passwordEncoder.encode(user.getPassword());
        user.setPassword(motDePasseCrypte);

        // 3. Sauvegarder dans la base de données
        return userRepository.save(user);
    }


}
