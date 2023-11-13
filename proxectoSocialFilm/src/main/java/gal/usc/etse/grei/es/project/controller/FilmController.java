package gal.usc.etse.grei.es.project.controller;

import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.model.*;
import gal.usc.etse.grei.es.project.repository.FilmRepository;
import gal.usc.etse.grei.es.project.service.FilmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
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
    @PreAuthorize("#email == principal")
    ResponseEntity<Page<Film>> getBy(
            @RequestParam(name = "keywords", defaultValue = "") String keyword,
            @RequestParam(name = "genres", defaultValue = "") String genre,
            @RequestParam(name = "releaseDate", defaultValue = "") String releaseDate,
            @RequestParam(name = "producers", defaultValue = "") String producer,
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
    @PreAuthorize("#email == principal")
    ResponseEntity<Film> get(@PathVariable("id") String id) {
        return ResponseEntity.of(films.get(id));
    }

    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    Film createFilm(
            @RequestBody @Valid String title
    ) {
        Film film = new Film();
        film.setTitle(title);
        return films.create(film);
    }

    @PatchMapping(path = "{id}")
    @PreAuthorize("hasRole('ADMIN')")
    Optional<Film> patchFilm(@PathVariable("id") String id, @RequestBody List<Map<String, Object>> film) throws JsonPatchException {
        return films.patch(id, film);
    }

    @DeleteMapping(path = "{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable("id") String id) {
        films.delete(id);
    }


}

