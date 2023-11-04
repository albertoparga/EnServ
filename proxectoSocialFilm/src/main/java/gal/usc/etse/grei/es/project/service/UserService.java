package gal.usc.etse.grei.es.project.service;

import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository users;
    private final PatchUtils utils;
    private final MongoTemplate mongo;

    @Autowired
    public UserService(UserRepository users, PatchUtils utils, MongoTemplate mongo) {
        this.users = users;
        this.utils = utils;
        this.mongo = mongo;
    }

    public Optional<Page<User>> get(int page, int size, Sort sort) {
        Pageable request = PageRequest.of(page, size, sort);
        Page<User> result = users.findAll(request);

        if (result.isEmpty())
            return Optional.empty();

        else return Optional.of(result);
    }

    public Optional<Page<User>> getBy(int page, int size, Sort sort, String email, String name) {
        Pageable request = PageRequest.of(page, size, sort);

        Criteria criteria = Criteria.where("email").regex(email).and("name").regex(name);
        Query query = Query.query(criteria).with(request);
        query.fields().exclude("email").exclude("friends");

        List <User> usrs = mongo.find(query,User.class);
        Page<User> result = new PageImpl<>(usrs);

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

        u.setEmail(user.get().getEmail());
        u.setBirthday((user.get().getBirthday()));

        return Optional.of(users.save(u));
    }

    public Optional<User> addFriend(String id, User friend) {
        Optional<User> u = users.findById(id);
        if (u.isPresent()) {
            User user = u.get();
            if(user.getFriends() == null) {
                List<User> friends = new ArrayList<>();
                user.setFriends(friends);
            }
            Optional<User> f = users.findById(friend.getEmail());
            if(f.isPresent()) {
                if (f.get().getName().equals(friend.getName())) {
                    User f2 = f.get();
                    user.getFriends().add(f2);
                }
            } else {
                   //LANZAR EXCEPCIÓN
            }

            return Optional.of(users.save(user));
        }
        return Optional.ofNullable(friend);
    }

    public void deleteFriend(String id, String friend) {
        Optional<User> u = users.findById(id);
        if(u.isPresent()) {
            User user = u.get();
            Optional<User> f = users.findById(friend);
            if (f.isPresent()) {
                User f2 = f.get();
                user.getFriends().remove(f2);
            }
           users.save(user);
        }
    }
}
