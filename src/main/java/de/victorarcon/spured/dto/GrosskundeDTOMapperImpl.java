package de.victorarcon.spured.dto;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Default implementation of {@link GrosskundeDTOMapper}.
 */
@ApplicationScoped
public class GrosskundeDTOMapperImpl implements GrosskundeDTOMapper{

    /**
     * Converts a GrosskundeDto into a GrosskundeInput using the ID from the DTO itself.
     * This method is typically used when creating new entities or processing incoming data.
     *
     * @param grosskundeDTO the data transfer object containing Grosskunde information
     * @return a validated GrosskundeInput instance
     */
    @Override
    public GrosskundeInput toGrosskundeInput(GrosskundeDto grosskundeDTO) {
        return new GrosskundeInput(grosskundeDTO.id(), grosskundeDTO);
    }

    /**
     * Converts a GrosskundeDto into a GrosskundeInput using an explicitly provided ID.
     * Useful when the ID needs to be overridden or explicitly set during update operations.
     *
     * @param id             the Grosskunde ID to assign
     * @param grosskundeDTO  the data transfer object containing Grosskunde information
     * @return a validated GrosskundeInput instance with the specified ID
     */
    @Override
    public GrosskundeInput toGrosskundeInput(Long id, GrosskundeDto grosskundeDTO) {
        return new GrosskundeInput(id, grosskundeDTO);
    }
}
