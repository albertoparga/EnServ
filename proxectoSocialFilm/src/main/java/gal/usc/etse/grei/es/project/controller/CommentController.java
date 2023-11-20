package gal.usc.etse.grei.es.project.controller;

import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.Film;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.service.CommentService;
import gal.usc.etse.grei.es.project.service.FilmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("comments")
public class CommentController {
    private final CommentService comments;

    @Autowired
    public CommentController(CommentService comments) {
        this.comments = comments;
    }

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("(!#film.equals('') and hasRole('ROLE_USER')) or " +
            "(!#user.equals('') and (hasRole('ROLE_ADMIN') or #user == principal or @userService.areFriends(#user,principal)))")
    ResponseEntity<Page<Assessment>> getBy(
            @RequestParam(name = "film", defaultValue="") String film,
            @RequestParam(name = "user", defaultValue="") String user,
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

        Optional<Page<Assessment>> response = comments.getBy(page, size, Sort.by(criteria), film, user);

        if(response.isPresent()) {
            Page<Assessment> data = response.get();
            Pageable metadata = data.getPageable();
            Link busqueda;
            if(user.equals("")){
                busqueda = linkTo(methodOn(UserController.class).get(user)).withSelfRel();
            }else{
                busqueda = linkTo(methodOn(FilmController.class).get(film)).withSelfRel();
            }

            Link first = linkTo(
                    methodOn(CommentController.class).getBy(film, user,metadata.first().getPageNumber(), size, sort)
            ).withRel(IanaLinkRelations.FIRST);
            Link last = linkTo(
                    methodOn(CommentController.class).getBy(film, user, data.getTotalPages() - 1, size, sort)
            ).withRel(IanaLinkRelations.LAST);
            Link next = linkTo(
                    methodOn(CommentController.class).getBy(film, user, metadata.next().getPageNumber(), size, sort)
            ).withRel(IanaLinkRelations.NEXT);
            Link previous = linkTo(
                    methodOn(CommentController.class).getBy(film, user, metadata.previousOrFirst().getPageNumber(), size, sort)
            ).withRel(IanaLinkRelations.PREVIOUS);


            return ResponseEntity.ok()
                    .header(HttpHeaders.LINK, busqueda.toString())
                    .header(HttpHeaders.LINK, first.toString())
                    .header(HttpHeaders.LINK, last.toString())
                    .header(HttpHeaders.LINK, next.toString())
                    .header(HttpHeaders.LINK, previous.toString())
                    .body(response.get());
        }
        // Añadir enlaces HATEOAS a la respuesta

        return ResponseEntity.notFound().build();
    }

    //cambiar pathtodo en el body
    @PostMapping("{filmId}/{userId}")
    @PreAuthorize("#userId == principal")
    ResponseEntity<Assessment> createComment(@PathVariable("filmId") String filmId, @PathVariable("userId") String userId, @RequestBody Assessment com) {
        Optional<Assessment> comment = Optional.of(comments.create(filmId, userId, com));
        String user = "";
        List<String> sort = new ArrayList<>();
        if(comment.isPresent()){
            Link film = linkTo(methodOn(FilmController.class).get(filmId)).withRel("films");
            Link filmComments = linkTo(
                    methodOn(CommentController.class).getBy(filmId, user, 0, 20, sort)
            ).withRel("comments");

        return ResponseEntity.ok()
                .header(HttpHeaders.LINK, film.toString())
                .header(HttpHeaders.LINK, filmComments.toString())
                .body(comment.get());
    }
    // Añadir enlaces HATEOAS a la respuesta

        return ResponseEntity.notFound().build();
    }

    @DeleteMapping(path = "{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @commentService.commentUser(#id, principal)")
    public ResponseEntity<Assessment> deleteComment(@PathVariable("id") String id) {
        Optional<Assessment> comment = comments.get(id);
        String u = "";
        String film = "";
        List<String> sort = new ArrayList<>();
        comments.delete(id);
        if(comment.isPresent()){
            String filmId = comment.get().getFilm().getId();
            String userId = comment.get().getUser().getEmail();
            Link filmComments = linkTo(
                    methodOn(CommentController.class).getBy(filmId, u, 0, 20, sort)
            ).withRel("FilmComments");
            Link userComments = linkTo(
                    methodOn(CommentController.class).getBy(film, userId, 0, 20, sort)
            ).withRel("UserComments");

            return ResponseEntity.ok()
                    .header(HttpHeaders.LINK, filmComments.toString())
                    .header(HttpHeaders.LINK, userComments.toString())
                    .body(null);
        }
        // Añadir enlaces HATEOAS a la respuesta

        return ResponseEntity.notFound().build();
    }

    @PatchMapping(path = "{id}")
    @PreAuthorize("@commentService.commentUser(#id, principal)")
    ResponseEntity<Assessment> patchUser(@PathVariable("id") String id, @RequestBody List<Map<String, Object>> user) throws JsonPatchException {
        Optional<Assessment> comment = comments.patch(id, user);
        String u = "";
        String film = "";
        List<String> sort = new ArrayList<>();
        if(comment.isPresent()){
            Link self = linkTo(methodOn(CommentController.class).get(comment.get().getId())).withSelfRel();
            Link filmComments = linkTo(
                    methodOn(CommentController.class).getBy(comment.get().getFilm().getId(), u, 0, 20, sort)
            ).withRel("FilmComments");
            Link userComments = linkTo(
                    methodOn(CommentController.class).getBy(film, comment.get().getUser().getEmail(), 0, 20, sort)
            ).withRel("UserComments");

            return ResponseEntity.ok()
                    .header(HttpHeaders.LINK, self.toString())
                    .header(HttpHeaders.LINK, filmComments.toString())
                    .header(HttpHeaders.LINK, userComments.toString())
                    .body(comment.get());
        }
        // Añadir enlaces HATEOAS a la respuesta

        return ResponseEntity.notFound().build();
    }

    @GetMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("isAuthenticated()")
    Optional<Assessment> get(@PathVariable("id") String id) {
        Optional<Assessment> comment = comments.get(id);
        if(comment.isPresent()) {
           return comment;
        }
        // Añadir enlaces HATEOAS a la respuesta

        return null;
    }

}

