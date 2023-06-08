package antifraud.dao;

import antifraud.model.Card;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepo extends CrudRepository<Card, String> {

    Card findByCardNumber(String number);

    boolean existsByCardNumber(String cardNumber);

    List<Card> findAll();

}
