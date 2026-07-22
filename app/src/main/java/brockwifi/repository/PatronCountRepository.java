package brockwifi.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import brockwifi.domain.PatronCount;

@Repository
public interface PatronCountRepository extends CrudRepository<PatronCount, Long> {
}
