package by.ladyka.poputka.data.repository;

import by.ladyka.poputka.data.entity.PoputkaUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PoputkaUserRepository extends JpaRepository<PoputkaUser, Byte> {
    Optional<PoputkaUser> findByUsername(String username);

    Optional<PoputkaUser> findByTelegramId(Long tgId);
}
