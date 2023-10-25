package gal.usc.etse.grei.es.project.controller;

import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.Film;
import gal.usc.etse.grei.es.project.service.CommentService;
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
@RequestMapping("comments")
public class CommentController {
    private final CommentService comments;

    @Autowired
    public CommentController(CommentService comments) {
        this.comments = comments;
    }

    @GetMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Assessment> get(@PathVariable("id") String id) {
        return ResponseEntity.of(comments.get(id));
    }
    


    @PostMapping("{filmId}/{userId}")
    Assessment createComment(@PathVariable("filmId") String filmId, @PathVariable("userId") String userId, @RequestBody Assessment com) {
        return comments.create(filmId, userId, com);
    }

    @DeleteMapping(path = "{id}")
    public void deleteComment(@PathVariable("id") String id) {
        comments.delete(id);
    }

}

