package gal.usc.etse.grei.es.project.repository;

import gal.usc.etse.grei.es.project.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;


public interface FilmRepository extends MongoRepository<Film, String> {

}
