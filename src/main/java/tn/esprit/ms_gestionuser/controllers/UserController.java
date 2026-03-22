package tn.esprit.ms_gestionuser.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tn.esprit.ms_gestionuser.entities.User;
import tn.esprit.ms_gestionuser.repositories.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        // 1. Récupérer le contexte de sécurité (rempli automatiquement par votre JwtFilter)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 2. Récupérer l'email de l'utilisateur connecté (le "subject" du token)
        String userEmail = authentication.getName();

        // 3. Chercher l'utilisateur dans la base de données
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé dans la base de données"));

        // 4. Petite astuce de sécurité : on efface le mot de passe de l'objet avant de l'envoyer au client !
        currentUser.setPassword(null);

        // 5. Renvoyer les informations de l'utilisateur (Code 200 OK)
        return ResponseEntity.ok(currentUser);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')") // Seuls les ADMIN peuvent entrer ici !
    public ResponseEntity<?> getAllUsers() {

        // On récupère tous les utilisateurs
        List<User> users = userRepository.findAll();

        // On masque les mots de passe avant de les envoyer
        users.forEach(user -> user.setPassword(null));

        return ResponseEntity.ok(users);
    }

    // ----------------------------------------------------
    // 1. SUPPRIMER UN UTILISATEUR (RÉSERVÉ À L'ADMIN)
    // ----------------------------------------------------
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.badRequest().body("Erreur : Utilisateur introuvable !");
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok("Utilisateur avec l'ID " + id + " a été supprimé avec succès !");
    }

    // ----------------------------------------------------
    // 2. METTRE À JOUR SON PROPRE PROFIL (POUR TOUT LE MONDE)
    // ----------------------------------------------------
    @PutMapping("/me/update")
    public ResponseEntity<?> updateMyProfile(@RequestBody User updatedUser) {
        // On récupère l'utilisateur connecté via son Token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // On met à jour uniquement les champs envoyés (on ne touche pas au mot de passe ou au rôle ici pour des raisons de sécurité)
        if (updatedUser.getNom() != null) currentUser.setNom(updatedUser.getNom());
        if (updatedUser.getPrenom() != null) currentUser.setPrenom(updatedUser.getPrenom());
        if (updatedUser.getTelephone() != null) currentUser.setTelephone(updatedUser.getTelephone());

        // On sauvegarde dans la base de données
        userRepository.save(currentUser);

        // On cache le mot de passe avant d'afficher la réponse
        currentUser.setPassword(null);

        return ResponseEntity.ok(currentUser);
    }
}
