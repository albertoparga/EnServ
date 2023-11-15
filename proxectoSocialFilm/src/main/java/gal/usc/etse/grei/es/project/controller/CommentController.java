package gal.usc.etse.grei.es.project.controller;

import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.Film;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.service.CommentService;
import gal.usc.etse.grei.es.project.service.FilmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
    @PostAuthorize("hasRole('ROLE_ADMIN') or #user==principal or " +
            "@userService.areFriends(#user, principal) or isAuthenticated()")
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

        return ResponseEntity.of(comments.getBy(page, size, Sort.by(criteria), film, user));
    }

    //cambiar pathtodo en el body
    @PostMapping("{filmId}/{userId}")
    @PreAuthorize("#userId == principal")
    Assessment createComment(@PathVariable("filmId") String filmId, @PathVariable("userId") String userId, @RequestBody Assessment com) {
        return comments.create(filmId, userId, com);
    }

    @DeleteMapping(path = "{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @commentService.commentUser(#id, principal)")
    public void deleteComment(@PathVariable("id") String id) {
        comments.delete(id);
    }

    @PatchMapping(path = "{id}")
    @PreAuthorize("@commentService.commentUser(#id, principal)")
    Optional<Assessment> patchUser(@PathVariable("id") String id, @RequestBody List<Map<String, Object>> user) throws JsonPatchException {
        return comments.patch(id, user);
    }

}

