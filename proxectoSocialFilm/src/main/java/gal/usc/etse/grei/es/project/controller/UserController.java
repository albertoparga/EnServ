package gal.usc.etse.grei.es.project.controller;

import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.service.CommentService;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("users")
@Tag(name = "User API", description = "User related operations")
@SecurityRequirement(name = "JWT")
public class UserController {
    private final UserService users;
    private final CommentService comments;
    private final LinkRelationProvider relationProvider;

    @Autowired
    public UserController(UserService users, CommentService comments, LinkRelationProvider relationProvider) {
        this.users = users;
        this.comments = comments;
        this.relationProvider = relationProvider;
    }

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("isAuthenticated()")
    @Operation(
            operationId = "getAllUsers",
            summary = "Get all users or find users details",
            description = "Get the details for all users or for those that match the search " +
                    "To see user details " +
                    "you must be an authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "List of users or user details",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad token",
                    content = @Content
            ),
    })
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
    @Operation(
            operationId = "getOneUser",
            summary = "Get a single user details",
            description = "Get the details for a given user. To see user details " +
                    "you must be the requested user, his friend, or have admin permissions."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The user details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad token",
                    content = @Content
            ),
    })
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
    @Operation(
            operationId = "postUser",
            summary = "Create a single user",
            description = "Create a user by stating, at least, email, name, birthday and password. " +
                    "Every user has permission, even non authenticated ones. "
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The user was created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User email already exists",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad token",
                    content = @Content
            ),
    })
    public ResponseEntity<User> createUser(@RequestBody @Valid User u) {
        Optional<User> user = users.create(u);

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
    @Operation(
            operationId = "deleteUser",
            summary = "Delete an user",
            description = "Delete an user given its email. " +
                    "You must be the user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The user was deleted",
                    content = @Content(
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad token",
                    content = @Content
            ),
    })
    public ResponseEntity<User> deleteUser(@PathVariable("id") String id) {
        Optional<User> user = users.get(id);

        if(user.isPresent()) {
            users.delete(id);
            comments.deleteCommentsU(id);

            Link all = linkTo(UserController.class).withRel(relationProvider.getCollectionResourceRelFor(User.class));

            return ResponseEntity.ok()
                    .header(HttpHeaders.LINK, all.toString())
                    .body(null);
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping(path = "{email}")
    @PreAuthorize("#email == principal")
    @Operation(
            operationId = "pacthUser",
            summary = "Change a single user details",
            description = "Change the details for a given user. " +
                    "You must be the user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The user was patched",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad token",
                    content = @Content
            ),
    })
    public ResponseEntity<User> patchUser(@PathVariable("email") String email, @RequestBody List<Map<String, Object>> u) throws JsonPatchException {
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
    @Operation(
            operationId = "postUserFriend",
            summary = "Add a friend to an user",
            description = "Add a friend for a given user. You must especify friends email and name. " +
                    "You must be the user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The friend was added",
                    content = @Content(
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad token",
                    content = @Content
            ),
    })
    public ResponseEntity<User> addFriend(@PathVariable("id") String id, @RequestBody User friend) {
        Optional<User> f = users.get(friend.getEmail());

        if(f.isPresent()) {
            users.addFriend(id, friend);

            return ResponseEntity.ok()
                    .body(null);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping(path = "{id}/friends/{friend}")
    @PreAuthorize("#id == principal")
    @Operation(
            operationId = "deleteUserFriend",
            summary = "Delete a friend from an user",
            description = "Delete a friend of a given user. You must especify the friends email" +
                    "You must be the user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The friend was deleted",
                    content = @Content(
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad token",
                    content = @Content
            ),
    })
    public ResponseEntity<User> deleteFriend(@PathVariable("id") String id, @PathVariable("friend") String friend) {
        if(users.areFriends(id, friend)) {
            users.deleteFriend(id, friend);

            return ResponseEntity.ok()
                    .body(null);
        }
        return ResponseEntity.notFound().build();
    }

}

