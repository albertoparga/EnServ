package gal.usc.etse.grei.es.project.service;

import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder encoder;

    @Autowired
    public UserService(UserRepository users, PatchUtils utils, MongoTemplate mongo, PasswordEncoder encoder) {
        this.users = users;
        this.utils = utils;
        this.mongo = mongo;
        this.encoder = encoder;
    }

    public Optional<Page<User>> get(int page, int size, Sort sort) {
        Pageable request = PageRequest.of(page, size, sort);
        Page<User> result = users.findAll(request);

        if (result.isEmpty())
            return Optional.empty();

        else return Optional.of(result);
    }

    public Optional<Page<User>> getBy(int page, int size, Sort sort, String email, String name) {
        Pageable pageable = PageRequest.of(page, size, sort);

        Criteria criteria = Criteria.where("email").regex(email).and("name").regex(name);
        Query query = Query.query(criteria);
        query.fields().exclude("email").exclude("friends").exclude("password").exclude("roles");

        long count = mongo.count(query, User.class); // Obtén el número total de elementos que coinciden con el criterio

        query.with(pageable);

        List<User> users = mongo.find(query, User.class);
        Page<User> result = new PageImpl<>(users, pageable, count);

        if (result.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(result);
        }
    }

    public Optional<User> get(String id) {
        return users.findById(id);
    }
    
    public void delete(String id) { users.deleteById(id); }

    public Optional<User> create(User user){
        if(!users.existsById(user.getEmail())) {
            // Modificamos o contrasinal para gardalo codificado na base de datos
            user.setPassword(encoder.encode(user.getPassword()));
            List<String> roles = new ArrayList<>();
            roles.add("ROLE_USER");
            user.setRoles(roles);
            return Optional.of(users.insert(user));
        } else {
            return Optional.empty();
        }
    }

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
                    User f2 = new User();
                    f2.setEmail(f.get().getEmail());
                    f2.setName(f.get().getName());
                    user.getFriends().add(f2);
                }
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
                User f2 = new User();
                f2.setEmail(f.get().getEmail());
                f2.setName(f.get().getName());
                user.getFriends().remove(f2);
            }
           users.save(user);
        }
    }

    public Boolean areFriends(String email, String amigo) {
            Optional<User> usuario = users.findById(email);
            if(usuario.isPresent()) {
                List<User> list = usuario.get().getFriends();
                if (list != null) {
                    for (User u : list) {
                        if (u.getEmail().equals(amigo)) {
                            return true;
                        }
                    }
                }
                return false;
            }
            return false;
     }

}
