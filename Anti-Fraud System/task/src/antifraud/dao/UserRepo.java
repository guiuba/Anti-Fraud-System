package antifraud.dao;

import antifraud.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends CrudRepository<User, Long> {
    @Override
    Optional<User> findById(Long aLong);

    Optional<User> findUserByUsernameIgnoreCase(String username);

    List<User> findAll();
}
