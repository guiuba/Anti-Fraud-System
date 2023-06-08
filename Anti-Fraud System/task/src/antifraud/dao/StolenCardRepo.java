package antifraud.dao;

import antifraud.model.StolenCard;
import antifraud.model.SuspiciousIp;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StolenCardRepo extends CrudRepository<StolenCard, Long>{
    Optional<StolenCard> findByNumber(String ip);
    List<StolenCard> findAll();
}