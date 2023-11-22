package gal.usc.etse.grei.es.project.controller;

import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.model.Film;
import gal.usc.etse.grei.es.project.service.CommentService;
import gal.usc.etse.grei.es.project.service.FilmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("films")
@Tag(name = "Films API", description = "Films related operations")
@SecurityRequirement(name = "JWT")
public class FilmController {
    private final FilmService films;
    private final CommentService comments;
    private final LinkRelationProvider relationProvider;
    @Autowired
    public FilmController(FilmService films, CommentService comments, LinkRelationProvider relationProvider ) {
        this.films = films;
        this.comments = comments;
        this.relationProvider = relationProvider;
    }

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("isAuthenticated()")
    @Operation(
            operationId = "getFilmsBy",
            summary = "Get all films details or one film by some filter",
            description = "Get the details for all the films or for one or more films filtering by keyword, genre, releaseDate, producer, crew or cast. To see film details " +
                    "you must be logged."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The film or films details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Film.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Films not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad token",
                    content = @Content
            ),
    })
    ResponseEntity<Page<Film>> getBy(
            @RequestParam(name = "keywords", defaultValue = "") String keyword,
            @RequestParam(name = "genres", defaultValue = "") String genre,
            @RequestParam(name = "releaseDate", defaultValue = "") String releaseDate,
            @RequestParam(name = "producers", defaultValue = "") String producer,
            @RequestParam(name = "crew", defaultValue = "") String crew,
            @RequestParam(name = "cast", defaultValue = "") String caste,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "") List<String> sort
    ) {
        List<Sort.Order> criteria = sort.stream().map(string -> {
                    if (string.startsWith("+")) {
                        return Sort.Order.asc(string.substring(1));
                    } else if (string.startsWith("-")) {
                        return Sort.Order.desc(string.substring(1));
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Optional<Page<Film>> response = films.getBy(page, size, Sort.by(criteria), keyword, genre, releaseDate, producer, crew, caste);

        if(response.isPresent()) {
            Page<Film> data = response.get();
            Pageable metadata = data.getPageable();
            Link self = linkTo(methodOn(FilmController.class).getBy(keyword, genre, releaseDate, producer, crew, caste, page, size, sort)).withSelfRel();
            Link first = linkTo(
                    methodOn(FilmController.class).getBy(keyword, genre, releaseDate, producer, crew, caste,metadata.first().getPageNumber(), size, sort)
            ).withRel(IanaLinkRelations.FIRST);
            Link last = linkTo(
                    methodOn(FilmController.class).getBy(keyword, genre, releaseDate, producer, crew, caste, data.getTotalPages() - 1, size, sort)
            ).withRel(IanaLinkRelations.LAST);
            Link next = linkTo(
                    methodOn(FilmController.class).getBy(keyword, genre, releaseDate, producer, crew, caste, metadata.next().getPageNumber(), size, sort)
            ).withRel(IanaLinkRelations.NEXT);
            Link previous = linkTo(
                    methodOn(FilmController.class).getBy(keyword, genre, releaseDate, producer, crew, caste, metadata.previousOrFirst().getPageNumber(), size, sort)
            ).withRel(IanaLinkRelations.PREVIOUS);

            Link one = linkTo(
                    methodOn(FilmController.class).get(null)
            ).withRel(relationProvider.getItemResourceRelFor(Film.class));
            return ResponseEntity.ok()
                    .header(HttpHeaders.LINK, self.toString())
                    .header(HttpHeaders.LINK, first.toString())
                    .header(HttpHeaders.LINK, last.toString())
                    .header(HttpHeaders.LINK, next.toString())
                    .header(HttpHeaders.LINK, previous.toString())
                    .header(HttpHeaders.LINK, one.toString())
                    .body(response.get());
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("isAuthenticated()")
    @Operation(
            operationId = "getOneFilmById",
            summary = "Get film by id",
            description = "Get the details for one film filtering by his id." +
                    "You must be logged."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The film",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Film.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Film not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad token",
                    content = @Content
            ),
    })
    ResponseEntity<Film> get(@PathVariable("id") String id) {
        Optional<Film> film = films.get(id);
        if(film.isPresent()) {
            Link self = linkTo(methodOn(FilmController.class).get(id)).withSelfRel();
            Link all = linkTo(FilmController.class).withRel(relationProvider.getCollectionResourceRelFor(Film.class));

            return ResponseEntity.ok()
                    .header(HttpHeaders.LINK, self.toString())
                    .header(HttpHeaders.LINK, all.toString())
                    .body(film.get());
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping()
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
            operationId = "createFilm",
            summary = "Create a film",
            description = "Create a film specifying his title." +
                    "You must be an ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Film was created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Film.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Film Id already exits",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad token",
                    content = @Content
            ),
    })
    ResponseEntity<Film> createFilm(@RequestBody @Valid String title) {
        Film film = new Film();
        film.setTitle(title);
        Film created = films.create(film);
        Optional<Film> createdFilm = films.get(created.getId());

        if(createdFilm.isPresent()) {
            Link self = linkTo(methodOn(FilmController.class).createFilm(title)).withSelfRel();
            Link all = linkTo(FilmController.class).withRel(relationProvider.getCollectionResourceRelFor(Film.class));

            return ResponseEntity.ok()
                    .header(HttpHeaders.LINK, self.toString())
                    .header(HttpHeaders.LINK, all.toString())
                    .body(createdFilm.get());
        }

        return ResponseEntity.notFound().build();

    }

    @PatchMapping(path = "{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
            operationId = "ModifyFilmById",
            summary = "Modify a film",
            description = "Modify a film by his id." +
                    "You must be an ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The film was modified successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Film.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Film not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad token",
                    content = @Content
            ),
    })
    ResponseEntity<Film> patchFilm(@PathVariable("id") String id, @RequestBody List<Map<String, Object>> film) throws JsonPatchException {
        Optional<Film> patchedFilm = films.patch(id, film);

        // Añadir enlaces HATEOAS al recurso modificado (si existe)
        if(patchedFilm.isPresent()) {
            Link self = linkTo(methodOn(FilmController.class).get(id)).withSelfRel();
            Link all = linkTo(FilmController.class).withRel(relationProvider.getCollectionResourceRelFor(Film.class));

            return ResponseEntity.ok()
                    .header(HttpHeaders.LINK, self.toString())
                    .header(HttpHeaders.LINK, all.toString())
                    .body(patchedFilm.get());
        }

        return ResponseEntity.notFound().build();
    }

    @DeleteMapping(path = "{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
            operationId = "DeleteFilmById",
            summary = "Delete a film",
            description = "Delete film by his id." +
                    "You must be an ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The film was deleted",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Film not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad token",
                    content = @Content
            ),
    })
    public ResponseEntity<Film> delete(@PathVariable("id") String id) {
        Optional<Film> film = films.get(id);

        if(film.isPresent()) {
            films.delete(id);
            comments.deleteCommentsF(id);

            Link all = linkTo(FilmController.class).withRel(relationProvider.getCollectionResourceRelFor(Film.class));
            return ResponseEntity.ok()
                    .header(HttpHeaders.LINK, all.toString())
                            .body(null);

        }

        return ResponseEntity.notFound().build();
    }
}
