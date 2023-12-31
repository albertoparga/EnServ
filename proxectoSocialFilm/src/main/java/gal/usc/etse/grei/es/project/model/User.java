package gal.usc.etse.grei.es.project.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

@Document(collection = "users")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        name = "User",
        description = "A complete user representation"
)
public class User {
    @Id
    @NotBlank(message = "El campo email es obligatorio")
    @Email
    @Schema(required = true, example = "test@test.com")
    private String email;
    @NotEmpty(message = "El campo name es obligatorio")
    @Schema(example = "Usuario Usuario")
    private String name;
    @Schema(example = "Spain")
    private String country;
    @Schema(example = "https://placekitten.com/200/287")
    private String picture;
    @NotNull(message = "El campo birthday es obligatorio")
    private Date birthday;
    private List<User> friends;
    private String password;
    private List<String> roles;

    public User() {
    }

    public User(String email, String name, String country, String picture, Date birthday, List<User> friends, String password, List<String> roles) {
        this.email = email;
        this.name = name;
        this.country = country;
        this.picture = picture;
        this.birthday = birthday;
        this.friends = friends;
        this.password = password;
        this.roles = roles;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public String getPicture() {
        return picture;
    }

    public Date getBirthday() {
        return birthday;
    }

    public List<User> getFriends() {
        return friends;
    }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }

    public User setCountry(String country) {
        this.country = country;
        return this;
    }

    public User setPicture(String picture) {
        this.picture = picture;
        return this;
    }

    public User setBirthday(Date birthday) {
        this.birthday = birthday;
        return this;
    }

    public User setFriends(List<User> friends) {
        this.friends = friends;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(email, user.email) && Objects.equals(name, user.name) && Objects.equals(country, user.country) && Objects.equals(picture, user.picture) && Objects.equals(birthday, user.birthday) && Objects.equals(friends, user.friends);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, name, country, picture, birthday, friends);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", User.class.getSimpleName() + "[", "]")
                .add("email='" + email + "'")
                .add("name='" + name + "'")
                .add("country='" + country + "'")
                .add("picture='" + picture + "'")
                .add("birthday=" + birthday)
                .add("friends=" + friends)
                .toString();
    }
}
