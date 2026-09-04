package de.victorarcon.spured.dto;

import de.hansemerkur.port.history.core.HistoryAlgorithm;
import de.hansemerkur.port.history.core.HistoryTuple;
import de.hansemerkur.port.history.core.NumericTransactionIdProvider;
import de.hansemerkur.port.history.core.UserIdProvider;
import de.victorarcon.spured.GrosskundeException;
import de.victorarcon.spured.control.GrosskundeRepository;
import de.victorarcon.spured.entity.HGrosskunde;
import de.victorarcon.spured.entity.SGrosskunde;
import de.victorarcon.spured.entity.SGrosskundeConverter;
import de.victorarcon.spured.entity.SGrosskundeMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Business logic for Grosskunde entities: create, update, retrieve, and terminate,
 * coordinating with {@link HistoryAlgorithm} so every change is kept as a versioned
 * time slice rather than overwriting or deleting prior data.
 */
@ApplicationScoped
@Transactional
public class GrosskundeService {
    private final SGrosskundeMapper grosskundeMapper;
    private final HistoryAlgorithm historyAlgorithm;
    private final GrosskundeRepository repository;

    /**
     * Constructor injecting required dependencies for mapping, history tracking, and data access.
     *
     * @param grosskundeMapper     mapper for converting input models to entities
     * @param historyAlgorithm     algorithm for managing historical versions of entities
     * @param repository           repository for accessing Grosskunde data
     */
    @Inject
    public GrosskundeService(SGrosskundeMapper grosskundeMapper,
                             HistoryAlgorithm historyAlgorithm,
                             GrosskundeRepository repository) {
        this.grosskundeMapper = grosskundeMapper;
        this.historyAlgorithm = historyAlgorithm;
        this.repository = repository;
    }

    /**
     * Persists a new Grosskunde entity with historical tracking.
     *
     * @param grosskundeInput       input model containing customer data
     * @param userIdProvider        provider for user identification
     * @param transactionIdProvider provider for transaction identification
     * @return the persisted Grosskunde entity
     */
    public SGrosskunde save(GrosskundeInput grosskundeInput, UserIdProvider userIdProvider, NumericTransactionIdProvider transactionIdProvider) {
        var grosskunde = grosskundeMapper.fromInput(grosskundeInput);
        historyAlgorithm.persist(grosskunde, userIdProvider, transactionIdProvider);
        return grosskunde;
    }

    /**
     * Updates an existing Grosskunde entity by creating a new time slice.
     *
     * @param grosskundeInput       input model with updated data
     * @param userIdProvider        provider for user identification
     * @param transactionIdProvider provider for transaction identification
     */
    public void update(GrosskundeInput grosskundeInput, UserIdProvider userIdProvider, NumericTransactionIdProvider transactionIdProvider) {
        Objects.requireNonNull(grosskundeInput.id(), "id must not be null");
        Objects.requireNonNull(grosskundeInput, "grosskundeInput must not be null");

        Function<HistoryTuple, HGrosskunde> tupleMapper = tuple -> new SGrosskundeConverter()
                .fromTuple()
                .apply(tuple);

        var id = grosskundeInput.id();
        var currentGrosskunde = repository.findByIdAndMaxDhln(id, tupleMapper)
                .orElseThrow(() -> GrosskundeException.notFound(id));

        var newTimeSlice = grosskundeMapper.merge(grosskundeInput, currentGrosskunde);

        historyAlgorithm.addTimeSlice(newTimeSlice, userIdProvider, transactionIdProvider);
    }

    /**
     * Batch update for multiple Grosskunde entities.
     *
     * @param grosskundeInputs      collection of input models
     * @param userIdProvider        provider for user identification
     * @param transactionIdProvider provider for transaction identification
     */
    public void update(Collection<GrosskundeInput> grosskundeInputs, UserIdProvider userIdProvider, NumericTransactionIdProvider transactionIdProvider) {
        for (GrosskundeInput grosskundeInput : grosskundeInputs) {
            update(grosskundeInput, userIdProvider, transactionIdProvider);
        }
    }

    /**
     * Terminates the technical validity of a Grosskunde entity at a specific time.
     * Used to mark an entity as no longer valid without deleting historical data.
     *
     * @param grosskundeInput       input model of the entity to terminate
     * @param when                  timestamp of termination
     * @param userIdProvider        provider for user identification
     * @param transactionIdProvider provider for transaction identification
     */
    public void terminateTechnicalValidity(GrosskundeInput grosskundeInput, LocalDateTime when, UserIdProvider userIdProvider, NumericTransactionIdProvider transactionIdProvider) {
        Objects.requireNonNull(grosskundeInput, "grosskundeInput must not be null");
        Objects.requireNonNull(grosskundeInput.id(), "id must not be null");

        var id = grosskundeInput.id();
        var currentGrosskunde = findCurrentById(id);
        var newTimeSlice = grosskundeMapper.merge(grosskundeInput, currentGrosskunde);

        historyAlgorithm.unacknowledgeWholeEntity(newTimeSlice, when, userIdProvider, transactionIdProvider);
    }

    /**
     * Retrieves the currently active version of a Grosskunde entity by ID.
     *
     * @param id the Grosskunde ID
     * @return the current historical version of the entity
     */
    public HGrosskunde findCurrentById(Long id) {
        Function<HistoryTuple, HGrosskunde> tupleMapper = tuple -> new SGrosskundeConverter()
                .fromTuple()
                .apply(tuple);

        return repository.findCurrentById(id, tupleMapper)
                .orElseThrow(() -> GrosskundeException.notFound(id));
    }

    /**
     * Retrieves the full historical timeline of a Grosskunde entity by ID.
     *
     * @param id the Grosskunde ID
     * @return list of historical versions of the entity
     */
    public List<HGrosskunde> findHistoryById(Long id) {
        Function<HistoryTuple, HGrosskunde> tupleMapper = tuple -> new SGrosskundeConverter()
                .fromTuple()
                .apply(tuple);

        return repository.findHistory(id, tupleMapper);
    }

    /**
     * Retrieves all current Grosskunde entities.
     *
     * @return list of current Grosskunde entities
     */
    public List<SGrosskunde> findAll() {
        return repository.findAll();
    }

    /**
     * Retrieves a specific Grosskunde entity by ID.
     *
     * @param id the Grosskunde ID
     * @return the entity instance
     */
    public SGrosskunde findById(Long id) {
        return repository.findById(id);
    }
}
