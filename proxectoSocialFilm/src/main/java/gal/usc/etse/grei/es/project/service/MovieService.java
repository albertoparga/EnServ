package gal.usc.etse.grei.es.project.service;

import gal.usc.etse.grei.es.project.model.Movie;
import gal.usc.etse.grei.es.project.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MovieService {
    private final MovieRepository movies;

    @Autowired
    public MovieService(MovieRepository movies) {
        this.movies = movies;
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
}
