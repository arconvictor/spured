package de.victorarcon.spured.api;

import de.victorarcon.spured.dto.GrosskundeDto;
import jakarta.ws.rs.core.Response;

/**
 * API contract for managing Grosskunde (large customer) entities over REST.
 * Implemented by {@link de.victorarcon.spured.resource.GrosskundeResource}.
 */
public interface GrosskundeApi {

    /**
     * Retrieves all currently active Grosskunden.
     *
     * @return HTTP 200 with the list of current entities
     */
    Response readAllGrosskunden();

    /**
     * Retrieves a single Grosskunde by ID.
     *
     * @param id the Grosskunde ID
     * @param history if {@code true}, returns the full historical timeline instead of just the current version
     * @return HTTP 200 with the entity (or its history), or 404 if not found
     */
    Response readGrosskunde(Long id, boolean history);

    /**
     * Creates a new Grosskunde entity.
     *
     * @param grosskundeDTO the data for the new entity
     * @return HTTP 201 with the created entity
     */
    Response createGrosskunde(GrosskundeDto grosskundeDTO);

    /**
     * Updates an existing Grosskunde entity, creating a new historical time slice.
     *
     * @param grosskundeId the ID of the entity to update
     * @param grosskundeDTO the updated data
     * @return HTTP 200 on success
     */
    Response updateGrosskunde(Long grosskundeId, GrosskundeDto grosskundeDTO);

}
