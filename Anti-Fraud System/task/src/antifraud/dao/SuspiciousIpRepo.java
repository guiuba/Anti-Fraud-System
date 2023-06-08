package antifraud.dao;

import antifraud.model.SuspiciousIp;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SuspiciousIpRepo extends CrudRepository<SuspiciousIp, Long> {
    Optional<SuspiciousIp> findByIp(String ip);
    List<SuspiciousIp> findAll();
}
