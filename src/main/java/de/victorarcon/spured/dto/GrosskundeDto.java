package de.victorarcon.spured.dto;

import java.time.LocalDateTime;

/**
 * REST-facing representation of a Grosskunde: what the API accepts and returns.
 *
 * @param id customer ID (null when creating a new entity)
 * @param kurzBezeichnung short label
 * @param langBezeichnung long label
 * @param gueltigAb start of business validity
 * @param ungueltigAb end of business validity
 * @param grosskundeNr customer number
 * @param druckText print text
 */
public record GrosskundeDto(Long id,
                            String kurzBezeichnung,
                            String langBezeichnung,
                            LocalDateTime gueltigAb,
                            LocalDateTime ungueltigAb,
                            Long grosskundeNr,
                            String druckText) {
}
