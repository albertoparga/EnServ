package gal.usc.etse.grei.es.project.repository;

import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.Film;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface CommentRepository extends MongoRepository<Assessment, String> {
    @Query("{'film.id' :  ?0}")
    Assessment findByFilm(String filmId);

    @Query("{'user.id' :  ?0}")
    Assessment findByUser(String userId);
}
