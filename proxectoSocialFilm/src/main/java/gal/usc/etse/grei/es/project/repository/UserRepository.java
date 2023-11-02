package gal.usc.etse.grei.es.project.repository;

import gal.usc.etse.grei.es.project.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface UserRepository extends MongoRepository<User, String> {
    @Query(value = "{ $or: [{'email' : {$regex :?0 , $options:  'i'}}, " +
            "{'name' : {$regex :?1 , $options:  'i'}}]}",
            fields = "{ 'name' : 1, 'country' : 1, 'bithday' : 1, 'picture' : 1 }")
    List<User> findByEmail(String email, String name);
}
