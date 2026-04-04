package tn.esprit.ms_gestionuser.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import tn.esprit.ms_gestionuser.entities.User;
import tn.esprit.ms_gestionuser.services.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Récupère le profil de l'utilisateur connecté.
     * Si c'est la première connexion, l'utilisateur est automatiquement créé en base.
     */
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        User user = userService.getOrCreateUser(jwt);
        return ResponseEntity.ok(user);
    }

    /**
     * Met à jour le profil de l'utilisateur connecté (champs métier uniquement).
     */
    @PutMapping("/me/update")
    public ResponseEntity<User> updateMyProfile(@AuthenticationPrincipal Jwt jwt, @RequestBody User updatedUser) {
        User user = userService.updateUserProfile(jwt.getSubject(), updatedUser);
        return ResponseEntity.ok(user);
    }

    /**
     * Récupère tous les utilisateurs (réservé à l'ADMIN).
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Supprime un utilisateur par son ID (réservé à l'ADMIN).
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok("Utilisateur avec l'ID " + id + " a été supprimé avec succès !");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Endpoint de test pour vérifier que l'authentification fonctionne.
     */
    @GetMapping("/test-auth")
    public ResponseEntity<String> testAuth(@AuthenticationPrincipal Jwt jwt) {
        String message = "Bravo ! Vous êtes authentifié via Keycloak ! "
                + "Email: " + jwt.getClaimAsString("email")
                + ", Subject: " + jwt.getSubject();
        return ResponseEntity.ok(message);
    }
}
