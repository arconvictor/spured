package de.victorarcon.spured.entity;

import de.hansemerkur.port.history.core.FieldIntrospector;
import de.hansemerkur.port.history.core.HRecord;
import de.hansemerkur.port.history.core.HistoryValues;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A single historical version ("time slice") of a Grosskunde, as stored in the {@code H_GKD}
 * audit table. Extends {@code HRecord} to plug into the shared history-tracking framework.
 */
public class HGrosskunde extends HRecord<Long, HGrosskunde> {
    @FieldIntrospector(columnName = "GKD_ID")
    private Long id;
    @FieldIntrospector(columnName = "KURZ_BEZNG")
    private String kurzBezeichnung;
    @FieldIntrospector(columnName = "LANG_BEZNG")
    private String langBezeichnung;
    @FieldIntrospector(columnName = "GKD_NR")
    private Long grosskundeNr;
    @FieldIntrospector(columnName = "VS_DRUCK_TXT")
    private String druckText;

    /**
     * Full constructor used internally for creating a historical record with a specific version (dhln).
     *
     * @param dhln            version number or historical identifier
     * @param historyValues   metadata about business and technical validity
     * @param id              entity ID
     * @param kurzBezeichnung short label
     * @param langBezeichnung long label
     * @param grosskundeNr    customer number
     * @param druckText       print text
     */
    HGrosskunde(Number dhln,
                HistoryValues historyValues,
                Long id,
                String kurzBezeichnung,
                String langBezeichnung,
                Long grosskundeNr,
                String druckText) {

        super(dhln, historyValues);
        this.id = id;
        this.kurzBezeichnung = kurzBezeichnung;
        this.langBezeichnung = langBezeichnung;
        this.grosskundeNr = grosskundeNr;
        this.druckText = druckText;
    }

    /**
     * Convenience constructor that defaults the version (dhln) to 1.
     *
     * @param historyValues   metadata about business and technical validity
     * @param id              entity ID
     * @param kurzBezeichnung short label
     * @param langBezeichnung long label
     * @param grosskundeNr    customer number
     * @param druckText       print text
     */
    public HGrosskunde(HistoryValues historyValues,
                       Long id,
                       String kurzBezeichnung,
                       String langBezeichnung,
                       Long grosskundeNr,
                       String druckText) {

        this(1L, historyValues, id, kurzBezeichnung, langBezeichnung, grosskundeNr, druckText);
    }

    /**
     * Creates a deep copy of the current historical record.
     *
     * @return a new instance with the same data and version
     */
    @Override
    public HGrosskunde copy() {
        return new HGrosskunde(getVersion(),
                getHistoryValues(),
                id,
                kurzBezeichnung,
                langBezeichnung,
                grosskundeNr,
                druckText);
    }

    @Override
    public Long getId() {
        return id;
    }

    public String getkurzBezeichnung() {
        return kurzBezeichnung;
    }

    public String getlangBezeichnung() {
        return langBezeichnung;
    }

    public Long getGrosskundeNr() {
        return grosskundeNr;
    }

    public String getDruckText() {
        return druckText;
    }

    /**
     * Returns the start of the business validity period.
     */
    public LocalDateTime getDgbdat() {
        return getHistoryValues().businessValidity().start();
    }

    /**
     * Returns the end of the business validity period.
     */
    public LocalDateTime getDgedat() {
        return getHistoryValues().businessValidity().end();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HGrosskunde that = (HGrosskunde) o;
        return Objects.equals(id, that.id) && Objects.equals(kurzBezeichnung, that.kurzBezeichnung) && Objects.equals(langBezeichnung, that.langBezeichnung) && Objects.equals(grosskundeNr, that.grosskundeNr) && Objects.equals(druckText, that.druckText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, kurzBezeichnung, langBezeichnung, grosskundeNr, druckText);
    }

    @Override
    public String toString() {
        return "HGrosskunde{" +
                "id=" + id +
                ", kurzBezeichnug='" + kurzBezeichnung + '\'' +
                ", langBezeichnug='" + langBezeichnung + '\'' +
                ", grosskunderNr='" + grosskundeNr + '\'' +
                ", druckText='" + druckText + '\'' +
                ", dhln=" + dhln +
                ", historyValues=" + historyValues +
                '}';
    }
}
