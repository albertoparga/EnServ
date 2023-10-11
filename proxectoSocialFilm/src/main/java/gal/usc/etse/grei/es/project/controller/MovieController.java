package gal.usc.etse.grei.es.project.controller;

import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.model.Movie;
import gal.usc.etse.grei.es.project.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("movies")
public class MovieController {
    private final MovieService movies;

    @Autowired
    public MovieController(MovieService movies) {
        this.movies = movies;
    }

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Page<Movie>> get(
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

        return ResponseEntity.of(movies.get(page, size, Sort.by(criteria)));
    }

    @GetMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Movie> get(@PathVariable("id") String id) {
        return ResponseEntity.of(movies.get(id));
    }

    @PostMapping("")
    Movie createMovie(@RequestBody Movie movie) {
        return movies.create(movie);
    }

    @DeleteMapping(path = "{id}")
    void delete(@PathVariable("id") String id) {
        movies.delete(id);
    }

    @PatchMapping(path = "{id}")
    Optional<Movie> patch(@PathVariable("id") String id, @RequestBody List<Map<String, Object>> movie) throws JsonPatchException {
        return movies.patch(id, movie);
    }

}

