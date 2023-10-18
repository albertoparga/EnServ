package gal.usc.etse.grei.es.project.controller;

import gal.usc.etse.grei.es.project.model.Film;
import gal.usc.etse.grei.es.project.service.FilmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("films")
public class FilmController {
    private final FilmService films;

    @Autowired
    public FilmController(FilmService films) {
        this.films = films;
    }

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Page<Film>> getBy(
            @RequestParam(name = "title", defaultValue="") String title,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "") List<String> sort
    ) {
        List<Sort.Order> criteria = sort.stream().map(string -> {
                    if (string.startsWith("+")) {
                        return Sort.Order.asc(string.substring(1));
                    } else if (string.startsWith("-")) {
                        return Sort.Order.desc(string.substring(1));
                    } else return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return ResponseEntity.of(films.getBy(page, size, Sort.by(criteria), title));
    }

    @GetMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Film> get(@PathVariable("id") String id) {
        return ResponseEntity.of(films.get(id));
    }

    @PostMapping("")
    Film createFilm(@RequestBody Film film) {
        return films.create(film);
    }

    @DeleteMapping(path = "{id}")
    public void delete(@PathVariable("id") String id) {
        films.delete(id);
    }



}

