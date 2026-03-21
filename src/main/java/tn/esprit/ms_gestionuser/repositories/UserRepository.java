package tn.esprit.ms_gestionuser.repositories;

import org.springframework.stereotype.Repository;
import tn.esprit.ms_gestionuser.entities.User;

import java.util.Optional;

@Repository
public interface UserRepository {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
