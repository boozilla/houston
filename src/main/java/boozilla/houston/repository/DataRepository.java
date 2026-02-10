package boozilla.houston.repository;

import boozilla.houston.asset.Scope;
import boozilla.houston.entity.Data;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface DataRepository extends ReactiveCrudRepository<Data, Long> {
    default Flux<Data> findByLatest()
    {
        return findByLatest(LocalDateTime.now());
    }

    @Query(value = """
            SELECT * FROM data WHERE id IN (SELECT MAX(id) AS id FROM data WHERE apply_at <= :NOW GROUP BY scope, name)
            """)
    Flux<Data> findByLatest(LocalDateTime now);

    @Modifying
    @Query(value = """
            UPDATE data SET apply_at = :APPLY_AT WHERE commit_id LIKE :COMMIT_ID
            """)
    Mono<Integer> updateByCommitIdIsLike(final LocalDateTime applyAt, final String commitId);

    @Modifying
    @Query(value = """
            UPDATE data SET apply_at = NULL WHERE commit_id LIKE :COMMIT_ID
            """)
    Mono<Integer> updateByCommitIdIsLike(final String commitId);

    @Modifying
    @Query(value = """
            DELETE FROM data WHERE name LIKE :NAME
            """)
    Mono<Integer> deleteByNameStartsWith(final String name);

    Mono<Boolean> existsByCommitIdAndScopeAndNameAndChecksum(String commitId, Scope scope, String name, String checksum);

    @Query(value = """
            SELECT * FROM data WHERE apply_at IS NULL ORDER BY id DESC
            """)
    Flux<Data> findByApplyAtIsNull();

    @Query(value = """
            SELECT * FROM data WHERE apply_at IS NOT NULL ORDER BY id DESC
            """)
    Flux<Data> findByApplyAtIsNotNull();
}
