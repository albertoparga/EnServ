package gal.usc.etse.grei.es.project.service;

import gal.usc.etse.grei.es.project.model.Film;
import gal.usc.etse.grei.es.project.repository.FilmRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FilmService {
    private final FilmRepository films;

    @Autowired
    public FilmService(FilmRepository films) {
        this.films = films;
    }

    public Optional<Page<Film>> get(int page, int size, Sort sort) {
        Pageable request = PageRequest.of(page, size, sort);
        Page<Film> result = films.findAll(request);

        if (result.isEmpty())
            return Optional.empty();

        else return Optional.of(result);
    }

    public Optional<Page<Film>> getBy(int page, int size, Sort sort, String title) {
        Pageable request = PageRequest.of(page, size, sort);
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<Film> filter = Example.of(
                new Film().setTitle(title),
                matcher );
        Page <Film> result = films.findAll(filter, request);

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
