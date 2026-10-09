package com.massseat.app.repository;

import com.massseat.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    @Modifying
    @Query("""
            UPDATE User u
            SET u.fcmToken = null
            WHERE u.fcmToken = :token
            """
    )
    void clearFcmToken(@Param("token") String token);

}
