package de.victorarcon.spured.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

/**
 * Validated, internal representation of a Grosskunde create/update request — the service
 * layer works with this instead of the raw {@link GrosskundeDto}.
 *
 * @param id customer ID
 * @param kurzBezeichnung short label (required)
 * @param langBezeichnung long label (required)
 * @param gueltigAb start of business validity
 * @param ungueltigAb end of business validity
 * @param grosskunderNr customer number
 * @param druckText print text
 */
public record GrosskundeInput(Long id,
                              @NotBlank(message = "kurzBezeichnung must not be empty")
                              String kurzBezeichnung,
                              @NotBlank(message = "langBezeichnung must not be empty")
                              String langBezeichnung,
                              LocalDateTime gueltigAb,
                              LocalDateTime ungueltigAb,
                              Long grosskunderNr,
                              String druckText
) {

    // Constructor to convert from DTO to Input model
    public GrosskundeInput(Long id, GrosskundeDto dto) {
        this(id, dto.kurzBezeichnung(), dto.langBezeichnung(), dto.gueltigAb(), dto.ungueltigAb(), dto.grosskundeNr(), dto.druckText());
    }
}
