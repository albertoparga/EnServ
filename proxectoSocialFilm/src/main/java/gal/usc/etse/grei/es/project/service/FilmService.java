package gal.usc.etse.grei.es.project.service;

import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.model.*;
import gal.usc.etse.grei.es.project.repository.FilmRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FilmService {
    private final FilmRepository films;
    private final MongoTemplate mongo;
    private final PatchUtils utils;


    @Autowired
    public FilmService(FilmRepository films, MongoTemplate mongo, PatchUtils utils) {
        this.films = films;
        this.mongo = mongo;
        this.utils = utils;
    }

    public Optional<Page<Film>> get(int page, int size, Sort sort) {
        Pageable request = PageRequest.of(page, size, sort);
        Page<Film> result = films.findAll(request);

        if (result.isEmpty())
            return Optional.empty();

        else return Optional.of(result);
    }

    public Optional<Film> patch(String id, List<Map<String, Object>> updates) throws JsonPatchException {
        Optional<Film> film = films.findById(id);
        Film f = utils.patch(film.get(), updates);

        return Optional.of(films.save(f));
    }

    public Optional<Page<Film>> getBy(int page, int size, Sort sort, String keyword, String genre, String fecha, String producer, String crew, String cast) {
        Pageable pageable = PageRequest.of(page, size, sort);

        Criteria criteria;
        if (!fecha.equals("")) {
            String[] fechadef = fecha.split("/");
            criteria = Criteria.where("keywords").regex(keyword).and("genres").regex(genre)
                    .and("releaseDate.day").is(Integer.valueOf(fechadef[0]))
                    .and("releaseDate.month").is(Integer.valueOf(fechadef[1]))
                    .and("releaseDate.year").is(Integer.valueOf(fechadef[2]))
                    .and("producers.name").regex(producer)
                    .and("crew.name").regex(crew)
                    .and("cast.name").regex(cast);
        } else {
            criteria = Criteria.where("keywords").regex(keyword)
                    .and("genres").regex(genre)
                    .and("producers.name").regex(producer)
                    .and("crew.name").regex(crew)
                    .and("cast.name").regex(cast);
        }
        Query query = Query.query(criteria);

        long count = mongo.count(query, Film.class); // Obtén el número total de elementos que coinciden con el criterio

        query.with(pageable);

        query.fields().exclude("tagline").exclude("keywords")
                .exclude("producers").exclude("cast").exclude("crew")
                .exclude("budget").exclude("status").exclude("_class");

        List<Film> films = mongo.find(query, Film.class);
        Page<Film> result = new PageImpl<>(films, pageable, count);

        if (result.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(result);
        }
    }

    public Optional<Film> get(String id) {
        return films.findById(id);
    }


    public void delete(String id) { films.deleteById(id); }
    
    public Film create(Film film) { return films.save(film); }
}
