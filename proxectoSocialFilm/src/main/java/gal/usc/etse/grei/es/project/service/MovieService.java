package gal.usc.etse.grei.es.project.service;

import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.model.Movie;
import gal.usc.etse.grei.es.project.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MovieService {
    private final MovieRepository movies;
    private final PatchUtils utils;

    @Autowired
    public MovieService(MovieRepository movies, PatchUtils utils) {
        this.movies = movies;
        this.utils = utils;
    }

    public Optional<Page<Movie>> get(int page, int size, Sort sort) {
        Pageable request = PageRequest.of(page, size, sort);
        Page<Movie> result = movies.findAll(request);

        if (result.isEmpty())
            return Optional.empty();

        else return Optional.of(result);
    }

    public Optional<Movie> get(String id) {
        return movies.findById(id);
    }
    
    public void delete(String id) { movies.deleteById(id); }
    
    public Movie create(Movie movie) { return movies.save(movie); }

    public Optional<Movie> patch(String id, List<Map<String, Object>> updates) throws JsonPatchException {
        Optional<Movie> movie = movies.findById(id);
        Movie m = utils.patch(movie.get(), updates);
        return Optional.of(movies.save(m));
    }
}
