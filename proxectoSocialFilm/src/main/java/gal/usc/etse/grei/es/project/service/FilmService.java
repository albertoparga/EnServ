package gal.usc.etse.grei.es.project.service;

import com.sun.jdi.IntegerValue;
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

    @Autowired
    public FilmService(FilmRepository films, MongoTemplate mongo) {
        this.films = films;
        this.mongo = mongo;
    }

    public Optional<Page<Film>> get(int page, int size, Sort sort) {
        Pageable request = PageRequest.of(page, size, sort);
        Page<Film> result = films.findAll(request);

        if (result.isEmpty())
            return Optional.empty();

        else return Optional.of(result);
    }

    public Optional<Page<Film>> getBy(int page, int size, Sort sort, String keyword, String genre, String fecha, String producer, String crew, String cast) {
        Pageable request = PageRequest.of(page, size, sort);

        Criteria criteria;
        if(!fecha.equals("")){
            String[] fechadef = fecha.split("/");
             criteria = Criteria.where("keywords").regex(keyword).and("genres").regex(genre)
                    .and("releaseDate.day").is(Integer.valueOf(fechadef[0])).and("releaseDate.month").is(Integer.valueOf(fechadef[1])).and("releaseDate.year").is(Integer.valueOf(fechadef[2]))
                    .and("producers.name").regex(producer).and("crew.name").regex(crew).and("cast.name").regex(cast);
        }else{
            criteria = Criteria.where("keywords").regex(keyword).and("genres").regex(genre)
                    .and("producers.name").regex(producer).and("crew.name").regex(crew).and("cast.name").regex(cast);
        }
        Query query = Query.query(criteria).with(request);
        query.fields().exclude("tagline").exclude("keywords")
                .exclude("producers").exclude("cast").exclude("crew")
                .exclude("budget").exclude("status").exclude("_class");
        List <Film> film = mongo.find(query,Film.class);
        Page<Film> result = new PageImpl<>(film);

        if (result.isEmpty())
            return Optional.empty();

        else return Optional.of(result);
    }

    public Optional<Film> get(String id) {
        return films.findById(id);
    }


    public void delete(String id) { films.deleteById(id); }
    
    public Film create(Film film) { return films.save(film); }
}
