package gal.usc.etse.grei.es.project.controller;

import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.LinkRelationProvider;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    private final LinkRelationProvider relationProvider;

    @Autowired
    public UserController(UserService users, LinkRelationProvider relationProvider) {
        this.users = users;
        this.relationProvider = relationProvider;
    }

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<Page<User>> get(
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
        Optional<Page<User>> result = users.getBy(page, size, Sort.by(criteria), email, name);

        if(result.isPresent()) {
            Page<User> data = result.get();
            Pageable metadata = data.getPageable();

            Link self = linkTo(
                    methodOn(UserController.class).get(name, email, page, size, sort)
            ).withSelfRel();
            Link first = linkTo(
                    methodOn(UserController.class).get(name, email, metadata.first().getPageNumber(), size, sort)
            ).withRel(IanaLinkRelations.FIRST);
            Link last = linkTo(
                    methodOn(UserController.class).get(name, email, data.getTotalPages() - 1, size, sort)
            ).withRel(IanaLinkRelations.LAST);
            Link next = linkTo(
                    methodOn(UserController.class).get(name, email, metadata.next().getPageNumber(), size, sort)
            ).withRel(IanaLinkRelations.NEXT);
            Link previous = linkTo(
                    methodOn(UserController.class).get(name, email, metadata.previousOrFirst().getPageNumber(), size, sort)
            ).withRel(IanaLinkRelations.PREVIOUS);
            Link one = linkTo(
                    methodOn(UserController.class).get(null)
            ).withRel(relationProvider.getItemResourceRelFor(User.class));

            return ResponseEntity.ok()
                    .header(HttpHeaders.LINK, self.toString())
                    .header(HttpHeaders.LINK, first.toString())
                    .header(HttpHeaders.LINK, last.toString())
                    .header(HttpHeaders.LINK, next.toString())
                    .header(HttpHeaders.LINK, previous.toString())
                    .header(HttpHeaders.LINK, one.toString())
                    .body(result.get());
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping(
            path = "{email}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('ROLE_ADMIN') or #email == principal or @userService.areFriends(#email, principal)")
    public ResponseEntity<User> get(@PathVariable("email") String email) {
        Optional<User> user = users.get(email);

        if(user.isPresent()) {
            Link self = linkTo(methodOn(UserController.class).get(email)).withSelfRel();
            Link all = linkTo(UserController.class).withRel(relationProvider.getCollectionResourceRelFor(User.class));

            return ResponseEntity.ok()
                    .header(HttpHeaders.LINK, self.toString())
                    .header(HttpHeaders.LINK, all.toString())
                    .body(user.get());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("")
    @PreAuthorize("permitAll()")
    public ResponseEntity<User> createUser(@RequestBody @Valid User u) {
        users.create(u);
        Optional<User> user = users.get(u.getEmail());

        if(user.isPresent()) {
            Link self = linkTo(methodOn(UserController.class).get(u.getEmail())).withSelfRel();
            Link all = linkTo(UserController.class).withRel(relationProvider.getCollectionResourceRelFor(User.class));

            return ResponseEntity.ok()
                    .header(HttpHeaders.LINK, self.toString())
                    .header(HttpHeaders.LINK, all.toString())
                    .body(user.get());
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping(path = "{id}")
    @PreAuthorize("#id == principal")
    public ResponseEntity<User> deleteUser(@PathVariable("id") String id) {
        Optional<User> user = users.get(id);

        if(user.isPresent()) {
            users.delete(id);

            Link all = linkTo(UserController.class).withRel(relationProvider.getCollectionResourceRelFor(User.class));

            return ResponseEntity.ok()
                    .header(HttpHeaders.LINK, all.toString())
                    .body(null);
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping(path = "{id}")
    @PreAuthorize("#id == principal")
    public ResponseEntity<User> patchUser(@PathVariable("id") String email, @RequestBody List<Map<String, Object>> u) throws JsonPatchException {
        users.patch(email, u);
        Optional<User> user = users.get(email);

        if(user.isPresent()) {
            Link self = linkTo(methodOn(UserController.class).get(email)).withSelfRel();
            Link all = linkTo(UserController.class).withRel(relationProvider.getCollectionResourceRelFor(User.class));

            return ResponseEntity.ok()
                    .header(HttpHeaders.LINK, self.toString())
                    .header(HttpHeaders.LINK, all.toString())
                    .body(user.get());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping(path = "{id}/friends")
    @PreAuthorize("#id == principal")
    Optional<User> addFriend(@PathVariable("id") String id, @RequestBody User friend) {
        return users.addFriend(id, friend);
    }

    @DeleteMapping(path = "{id}/friends/{friend}")
    @PreAuthorize("#id == principal")
    void deleteFriend(@PathVariable("id") String id, @PathVariable("friend") String friend) {
        users.deleteFriend(id, friend);
    }

}

