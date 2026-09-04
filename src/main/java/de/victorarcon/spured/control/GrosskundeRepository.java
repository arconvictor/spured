package de.victorarcon.spured.control;

import de.hansemerkur.port.history.core.HistoryRepository;
import de.hansemerkur.port.history.core.HistoryTuple;
import de.victorarcon.spured.entity.SGrosskunde;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Data-access layer for Grosskunde entities: current state via plain JPQL,
 * and historical state via the injected {@link HistoryRepository}.
 */
@ApplicationScoped
public class GrosskundeRepository {
    private final HistoryRepository historyRepository;

    private final EntityManager entityManager;

    @Inject
    public GrosskundeRepository(HistoryRepository historyRepository, EntityManager entityManager) {
        this.historyRepository = historyRepository;
        this.entityManager = entityManager;
    }

    /**
     * Returns a Grosskunde's current data by id. If no entity with the given id exists, an empty result is returned.
     * The value returned by this method is a detached object.
     *
     * @param id     the id
     * @param mapper the mapper
     * @return the Grosskunde's current data, mapped by {@code mapper}
     */
    public <O> Optional<O> findCurrentById(Long id, Function<HistoryTuple, O> mapper) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(mapper, "mapper must not be null");

        return historyRepository.findOneHByIdActiveNow(id, SGrosskunde.class, mapper);
    }

    /**
     * Finds the most recent historical version of a Grosskunde entity by ID.
     * Uses native SQL to fetch the latest DHLN (timestamp/version).
     *
     * @param id     the Grosskunde ID
     * @param mapper function to convert HistoryTuple to desired output type
     * @return Optional of mapped result
     */
    public <O> Optional<O> findByIdAndMaxDhln(Long id, Function<HistoryTuple, O> mapper) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(mapper, "mapper must not be null");

        Tuple result = (Tuple) entityManager.createNativeQuery("""
                        select *
                        from H_GKD H
                        WHERE H.GKD_ID = :id
                        ORDER BY DHLN DESC
                        FETCH FIRST 1 ROW ONLY
                        """, Tuple.class
                ).setParameter("id", id)
                .getSingleResult();

        O apply = mapper.apply(HistoryTuple.of(result));
        return Optional.ofNullable(apply);
    }

    /**
     * Retrieves all current Grosskunde (large customer) entities from the database.
     * Executes a JPQL query to fetch all records of type SGrosskunde.
     *
     * @return a list of SGrosskunde entities
     */
    public List<SGrosskunde> findAll() {
        return entityManager.createQuery("FROM SGrosskunde", SGrosskunde.class).getResultList();
    }

    /**
     * Retrieves a specific Grosskunde entity by its unique ID.
     * Executes a parameterized JPQL query to fetch the matching SGrosskunde record.
     *
     * @param id the unique identifier of the Grosskunde entity
     * @return the SGrosskunde entity matching the given ID
     */
    public SGrosskunde findById(Long id) {
        return entityManager.createQuery("FROM SGrosskunde S WHERE S.id = :id", SGrosskunde.class).setParameter("id", id).getSingleResult();
    }

    /**
     * Retrieves the full historical timeline (all time slices) of a Grosskunde entity by ID.
     * Each historical version is mapped using the provided function.
     *
     * @param id     the Grosskunde ID
     * @param mapper function to convert each HistoryTuple to the desired output type
     * @return List of mapped historical versions of the entity
     */
    public <O> List<O> findHistory(Long id, Function<HistoryTuple, O> mapper){
        return historyRepository.findAllTimeslices(id, SGrosskunde.class, mapper);
    }
}
