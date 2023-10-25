package gal.usc.etse.grei.es.project.service;

import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.Film;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.repository.CommentRepository;
import gal.usc.etse.grei.es.project.repository.FilmRepository;
import gal.usc.etse.grei.es.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CommentService {
    private final CommentRepository comments;
    private final FilmRepository films;
    private final UserRepository users;

    private final PatchUtils utils;

    @Autowired
    public CommentService(CommentRepository comments, FilmRepository films, UserRepository users, PatchUtils utils) {
        this.comments = comments;
        this.films = films;
        this.users = users;
        this.utils = utils;
    }

    public Optional<Page<Assessment>> getBy(int page, int size, Sort sort, String filmId, String userId) {
        Pageable request = PageRequest.of(page, size, sort);
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        Optional<Film> f = films.findById(filmId);
        Film film = f.get();
        Optional<User> u = users.findById(userId);
        User user = u.get();

        Example<Assessment> filter = Example.of(
                new Assessment().setFilm(film).setUser(user), //NO FUNCIONA
                matcher );
        Page <Assessment> result = comments.findAll(filter, request);

        if (result.isEmpty())
            return Optional.empty();

        else return Optional.of(result);
    }

    public Optional<Assessment> get(String id) {
        return comments.findById(id);
    }

    public void delete(String id) { comments.deleteById(id); }
    
    public Assessment create(String filmId, String userId, Assessment com) {
        Optional<Film> f = films.findById(filmId);
        Optional<User> u = users.findById(userId);

        if (f.isPresent() && u.isPresent()) {
            Film film = f.get();
            User user = u.get();
            com.setFilm(film);
            com.setUser(user);

            return comments.save(com);
        }

        return null;
    }

    public Optional<Assessment> patch(String id, List<Map<String, Object>> updates) throws JsonPatchException {
        Optional<Assessment> comment = comments.findById(id);
        Assessment c = utils.patch(comment.get(), updates);

        return Optional.of(comments.save(c));
    }

    public Assessment getByFilmId(String filmId) {
        return comments.findByFilm(filmId);
    }

    public Assessment getByUserId(String userId) {
        return comments.findByUser(userId);
    }
}

