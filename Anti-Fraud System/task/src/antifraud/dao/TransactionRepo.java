package antifraud.dao;

import antifraud.model.Transaction;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepo extends CrudRepository<Transaction, Long> {

    @Override
    Optional<Transaction> findById(Long aLong);

    Optional<Transaction> findByNumber(String number);
    List<Transaction> findAllByNumber(String number);
    List<Transaction> findAllByDateBetween(LocalDateTime lastHourTransactionDateTime,
                                           LocalDateTime transactionDateTime);

    @Query("SELECT COUNT(DISTINCT t.region) FROM Transaction t " +
            "WHERE t.region <> ?1 AND t.number = ?2 AND t.date BETWEEN ?3 AND ?4")
    List<Transaction> findAllWithDateBefore(
            @Param("date") LocalDateTime date);
}
