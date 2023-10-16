package gal.usc.etse.grei.es.project.service;

import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.model.Movie;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.repository.UserRepository;
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
public class UserService {
    private final UserRepository users;
    private final PatchUtils utils;

    @Autowired
    public UserService(UserRepository users, PatchUtils utils) {
        this.users = users;
        this.utils = utils;
    }

    public Optional<Page<User>> get(int page, int size, Sort sort) {
        Pageable request = PageRequest.of(page, size, sort);
        Page<User> result = users.findAll(request);

        if (result.isEmpty())
            return Optional.empty();

        else return Optional.of(result);
    }

    public Optional<User> get(String id) {
        return users.findById(id);
    }
    
    public void delete(String id) { users.deleteById(id); }
    
    public User create(User user) { return users.save(user); }

    public Optional<User> patch(String id, List<Map<String, Object>> updates) throws JsonPatchException {
        Optional<User> user = users.findById(id);
        User u = utils.patch(user.get(), updates);
        return Optional.of(users.save(u));
    }
}
