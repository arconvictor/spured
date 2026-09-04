package de.victorarcon.spured.dto;

/**
 * Maps the REST-facing {@link GrosskundeDto} to the internal {@link GrosskundeInput} used by the service layer.
 */
public interface GrosskundeDTOMapper {
    GrosskundeInput toGrosskundeInput(GrosskundeDto grosskundeDTO);

    GrosskundeInput toGrosskundeInput(Long id, GrosskundeDto grosskundeDTO);
}
