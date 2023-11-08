package gal.usc.etse.grei.es.project.controller;

import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("users")
public class UserController {
    private final UserService users;

    @Autowired
    public UserController(UserService users) {
        this.users = users;
    }

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Page<User>> getUsers(
            @RequestParam(name = "name", defaultValue = "") String name,
            @RequestParam(name = "email", defaultValue = "") String email,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "") List<String> sort
    ) {
        List<Sort.Order> criteria = sort.stream().map(string -> {
                    if (string.startsWith("-")) {
                        return Sort.Order.desc(string.substring(1));
                    } else if (string.startsWith("+")) {
                        return Sort.Order.asc(string.substring(1));
                    } else return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return ResponseEntity.of(users.getBy(page, size, Sort.by(criteria), email, name));
    }

    @GetMapping(
            path = "{email}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('ADMIN') or #email == principal or @userService.areFriends(#email, principal.username)")
    public ResponseEntity<User> get(@PathVariable("email") String email) {
        return ResponseEntity.of(users.get(email));
    }

    @PostMapping("")
    Optional<User> createUser(@RequestBody @Valid User user) {
        return users.create(user);
    }

    @DeleteMapping(path = "{id}")
    void deleteUser(@PathVariable("id") String id) {
        users.delete(id);
    }

    @PatchMapping(path = "{id}")
    Optional<User> patchUser(@PathVariable("id") String id, @RequestBody List<Map<String, Object>> user) throws JsonPatchException {
        return users.patch(id, user);
    }

    @PostMapping(path = "{id}/friend")
    Optional<User> addFriend(@PathVariable("id") String id, @RequestBody User friend) {
        return users.addFriend(id, friend);
    }

    @DeleteMapping(path = "{id}/{friend}")
    void deleteFriend(@PathVariable("id") String id, @PathVariable("friend") String friend) {
        users.deleteFriend(id, friend);
    }

}

