package de.victorarcon.spured.entity;

import de.hansemerkur.port.history.core.Converter;
import de.hansemerkur.port.history.core.HistoryTuple;

import java.util.function.Function;

/**
 * Reconstructs an {@link HGrosskunde} historical record from a raw {@code HistoryTuple}
 * returned by the history-tracking framework's native queries.
 */
public class SGrosskundeConverter implements Converter<HGrosskunde> {

    /**
     * Converts a HistoryTuple into an HGrosskunde entity.
     * This method extracts all required fields from the tuple and maps them to the HGrosskunde constructor.
     *
     * @return a function that maps HistoryTuple to HGrosskunde
     */
    @Override
    public Function<HistoryTuple, HGrosskunde> fromTuple() {
        return truple -> new HGrosskunde(
                truple.getVersion(),
                truple.getHistoryValues(),
                truple.readNumberOrThrow("GKD_ID").longValue(),
                truple.readStringOrThrow("KURZ_BEZNG"),
                truple.readStringOrThrow( "LANG_BEZNG"),
                truple.readNumberOrThrow("GKD_NR").longValue(),
                truple.readStringOrThrow("VS_DRUCK_TXT")
        );
    }
}
