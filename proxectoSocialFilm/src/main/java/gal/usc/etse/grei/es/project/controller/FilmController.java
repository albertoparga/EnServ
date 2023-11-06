package gal.usc.etse.grei.es.project.controller;

import gal.usc.etse.grei.es.project.model.*;
import gal.usc.etse.grei.es.project.repository.FilmRepository;
import gal.usc.etse.grei.es.project.service.FilmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("films")
public class FilmController {
    private final FilmService films;

    private FilmRepository filmR;
    @Autowired
    public FilmController(FilmService films) {
        this.films = films;
    }

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Page<Film>> getBy(
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "genre", defaultValue = "") String genre,
            @RequestParam(name = "releaseDate", defaultValue = "") String releaseDate,
            @RequestParam(name = "producer", defaultValue = "") String producer,
            @RequestParam(name = "crew", defaultValue = "") String crewe,
            @RequestParam(name = "cast", defaultValue = "") String caste,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "") List<String> sort
    )
    {
        List<Sort.Order> criteria = sort.stream().map(string -> {

                    if (string.startsWith("+")) {
                        return Sort.Order.asc(string.substring(1));
                    } else if (string.startsWith("-")) {
                        return Sort.Order.desc(string.substring(1));
                    } else {
                        return null;
                    }})
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return ResponseEntity.of(films.getBy(page, size, Sort.by(criteria), keyword, genre, releaseDate, producer, crewe, caste));
    }

    @GetMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Film> get(@PathVariable("id") String id) {
        return ResponseEntity.of(films.get(id));
    }

    @PostMapping()
    Film createFilm(
            @RequestBody @Valid String title
    ) {
        Film film = new Film();
        film.setTitle(title);
        return films.create(film);
    }

    @DeleteMapping(path = "{id}")
    public void delete(@PathVariable("id") String id) {
        films.delete(id);
    }


}

