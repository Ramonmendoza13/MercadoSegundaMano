package com.mercado.mercadosegundamano.repository;

import com.mercado.mercadosegundamano.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring genera: SELECT * FROM users WHERE username = ?
    // Optional porque el usuario puede no existir
    Optional<User> findByUsername(String username);

    // Spring genera: SELECT COUNT(*) > 0 FROM users WHERE username = ?
    boolean existsByUsername(String username);

    // Spring genera: SELECT COUNT(*) > 0 FROM users WHERE email = ?
    boolean existsByEmail(String email);
}