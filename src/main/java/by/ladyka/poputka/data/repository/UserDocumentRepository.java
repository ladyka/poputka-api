package by.ladyka.poputka.data.repository;

import by.ladyka.poputka.data.entity.PoputkaUser;
import by.ladyka.poputka.data.entity.UserDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDocumentRepository extends JpaRepository<UserDocument, String> {
    List<UserDocument> findAllByCreatedUser(PoputkaUser createdUser);
}
