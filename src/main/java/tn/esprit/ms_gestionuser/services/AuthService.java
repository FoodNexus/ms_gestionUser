package tn.esprit.ms_gestionuser.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.esprit.ms_gestionuser.entities.User;
import tn.esprit.ms_gestionuser.repositories.UserRepository;
import tn.esprit.ms_gestionuser.security.JwtUtils;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

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

    public String login(String email, String password) {
        // 1. Chercher l'utilisateur
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // 2. Vérifier le mot de passe (on compare le clair avec le haché)
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Mot de passe incorrect");
        }
        // 3. Générer et renvoyer le Token
        return jwtUtils.generateToken(user.getEmail(), user.getRole().name());
    }

}
