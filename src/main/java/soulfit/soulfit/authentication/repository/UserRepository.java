package soulfit.soulfit.authentication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import soulfit.soulfit.authentication.entity.UserAuth;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserAuth, Long> {
    Optional<UserAuth> findByUsername(String username);
    Optional<UserAuth> findByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
}