package gal.usc.etse.grei.es.project.service;

import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.Film;
import gal.usc.etse.grei.es.project.repository.CommentRepository;
import gal.usc.etse.grei.es.project.repository.FilmRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CommentService {
    private final CommentRepository comments;
    private final FilmRepository films;

    @Autowired
    public CommentService(CommentRepository comments, FilmRepository films) {
        this.comments = comments;
        this.films = films;
    }

    public Optional<Page<Assessment>> get(int page, int size, Sort sort) {
        Pageable request = PageRequest.of(page, size, sort);
        Page<Assessment> result = comments.findAll(request);

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
        //Optional<User> u = users.findById(userId);

        if (f.isPresent() /*&& u.isPresent()*/) {
            Film film = f.get();
            //User user = u.get();
            com.setFilm(film);
            //com.setuser(user);

            return comments.save(com);
        }

        return null;
    }
}
