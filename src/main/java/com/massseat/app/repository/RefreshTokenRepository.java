package com.massseat.app.repository;

import com.massseat.app.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query(
            """
            UPDATE RefreshToken r set r.revoked = true where r.user.id = :userId and r.revoked = false
            """
    )
    void revokedAllForUser(long userId);
}
