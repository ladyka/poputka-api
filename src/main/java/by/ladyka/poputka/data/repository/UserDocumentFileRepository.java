package by.ladyka.poputka.data.repository;

import by.ladyka.poputka.data.entity.UserDocumentFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDocumentFileRepository extends JpaRepository<UserDocumentFile, String> {
}
