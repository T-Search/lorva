package de.tsearch.lorva.database.postgres.repository;

import de.tsearch.lorva.database.postgres.entity.Broadcaster;
import org.springframework.data.repository.CrudRepository;

public interface BroadcasterRepository extends CrudRepository<Broadcaster, Long> {
}
