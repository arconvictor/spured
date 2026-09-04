package de.victorarcon.spured.entity;

import de.hansemerkur.port.history.core.HistoryValues;
import de.victorarcon.spured.dto.GrosskundeInput;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Clock;
import java.util.Objects;
import java.util.Optional;

/**
 * Converts between {@link GrosskundeInput} and {@link SGrosskunde}: builds a brand-new entity,
 * or merges an update into a new time slice on top of an existing historical record.
 */
@ApplicationScoped
public class SGrosskundeMapper {
    @Inject
    private Clock clock;

    /**
     * Constructor injecting a Clock instance used for timestamping historical records.
     *
     * @param clock system clock used to generate HistoryValues
     */
    public SGrosskundeMapper(Clock clock) {
        this.clock = clock;
    }

    /**
     * Converts a GrosskundeInput model into a new SGrosskunde entity.
     * Initializes the business validity period using the input dates.
     *
     * @param input the input model containing customer data
     * @return a new SGrosskunde entity with assigned history values
     */
    public SGrosskunde fromInput(GrosskundeInput input) {
        Objects.requireNonNull(input, "input must not be null");

        HistoryValues historyValues = HistoryValues.HistoryValuesBuilder.newInstance(clock)
                .withDgbdat(input.gueltigAb())
                .withDgedat(input.ungueltigAb())
                .build();

        var sgrosskunde = new SGrosskunde(historyValues,
                input.kurzBezeichnung(),
                input.langBezeichnung(),
                input.grosskunderNr(),
                input.druckText());

        sgrosskunde.setFachId(input.id());

        return sgrosskunde;
    }

    /**
     * Merges a GrosskundeInput model into an existing historical HGrosskunde record.
     * Creates a new SGrosskunde time slice with updated values while preserving unchanged fields.
     *
     * @param grosskundeInput     the input model with updated data
     * @param currentHGrosskunde  the current historical record to merge into
     * @return a new SGrosskunde entity representing the updated time slice
     */
    public SGrosskunde merge(GrosskundeInput grosskundeInput, HGrosskunde currentHGrosskunde) {
        if (!Objects.equals(grosskundeInput.id(), currentHGrosskunde.getId())) {
            throw new IllegalArgumentException("cannot merge unrelated data");
        }
        Objects.requireNonNull(grosskundeInput, "grosskundeInput must not be null");
        Objects.requireNonNull(currentHGrosskunde, "currentHGrosskunde must not be null");

        var historyValues = HistoryValues.HistoryValuesBuilder.copyInstance(currentHGrosskunde.getHistoryValues())
                .withDgbdat(Optional.ofNullable(grosskundeInput.gueltigAb()).orElse(currentHGrosskunde.getHistoryValues().businessValidity().start()))
                .withDgedat(Optional.ofNullable(grosskundeInput.ungueltigAb()).orElse(currentHGrosskunde.getHistoryValues().businessValidity().end()))
                .build();

        var newTimeSlice = new SGrosskunde(historyValues,
                Optional.ofNullable(grosskundeInput.kurzBezeichnung()).orElse(currentHGrosskunde.getkurzBezeichnung()),
                Optional.ofNullable(grosskundeInput.langBezeichnung()).orElse(currentHGrosskunde.getlangBezeichnung()),
                Optional.ofNullable(grosskundeInput.grosskunderNr()).orElse(currentHGrosskunde.getGrosskundeNr()),
                Optional.ofNullable(grosskundeInput.druckText()).orElse(currentHGrosskunde.getDruckText()));

        newTimeSlice.setFachId(grosskundeInput.id());
        return newTimeSlice;
    }
}
