package com.imobiliaria.repository;

import com.imobiliaria.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    /**
     * CORRIGIDO: DISTINCT para não duplicar usuários quando Hibernate
     * faz LEFT JOIN com coleções (seria um produto cartesiano).
     */
    @Query("SELECT DISTINCT u FROM User u ORDER BY u.createdAt DESC")
    List<User> findAllDistinct();
}