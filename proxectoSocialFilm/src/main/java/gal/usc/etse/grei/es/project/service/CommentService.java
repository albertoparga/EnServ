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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CommentService {
    private final CommentRepository comments;
    private final FilmRepository films;
    private final UserRepository users;
    private final MongoTemplate mongo;

    private final PatchUtils utils;

    @Autowired
    public CommentService(CommentRepository comments, FilmRepository films, UserRepository users, PatchUtils utils, MongoTemplate mongo) {
        this.comments = comments;
        this.films = films;
        this.users = users;
        this.utils = utils;
        this.mongo = mongo;
    }

    public Optional<Page<Assessment>> getBy(int page, int size, Sort sort, String filmId, String userId) {
        Pageable pageable = PageRequest.of(page, size, sort);

        Criteria criteria = Criteria.where("film.id").regex(filmId).and("user.email").regex(userId);
        Query query = Query.query(criteria);

        long count = mongo.count(query, Assessment.class); // Obtén el número total de elementos que coinciden con el criterio

        query.with(pageable);

        List<Assessment> comments = mongo.find(query, Assessment.class);
        Page<Assessment> result = new PageImpl<>(comments, pageable, count);

        if (result.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(result);
        }
    }

    public Optional<Assessment> get(String id) {
        return comments.findById(id);
    }

    public void delete(String id) { comments.deleteById(id); }
    
    public Assessment create(String filmId, String userId, Assessment com) {
        Optional<Film> f = films.findById(filmId);
        Optional<User> u = users.findById(userId);

        if (f.isPresent() && u.isPresent()) {
            Film film = new Film();
            film.setId(f.get().getId());
            film.setTitle(f.get().getTitle());
            User user = new User();
            user.setEmail(u.get().getEmail());
            user.setName(u.get().getName());
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

    public boolean commentUser(String id, String userId) {
        Optional<Assessment> comment = comments.findById(id);
        if(comment.isPresent()) {
            Assessment c = comment.get();
            if(c.getUser().getEmail().equals(userId)) {
                return true;
            }
        }
        return false;
    }

    public void deleteCommentsU(String userId) {
        Criteria criteria = Criteria.where("user.email").regex(userId);
        Query query = Query.query(criteria);
        mongo.remove(query, Assessment.class);
    }

    public void deleteCommentsF(String filmId) {
        Criteria criteria = Criteria.where("film.id").regex(filmId);
        Query query = Query.query(criteria);
        mongo.remove(query, Assessment.class);
    }
}

