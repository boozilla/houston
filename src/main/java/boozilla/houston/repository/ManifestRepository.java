package boozilla.houston.repository;

import boozilla.houston.entity.Manifest;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ManifestRepository extends ReactiveCrudRepository<Manifest, String> {
}
