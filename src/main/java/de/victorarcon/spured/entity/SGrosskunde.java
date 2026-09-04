package de.victorarcon.spured.entity;

import de.hansemerkur.port.history.core.*;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * The current, persistent state of a Grosskunde, mapped to the {@code S_GKD} table.
 * Every change is additionally recorded as a time slice in {@code H_GKD} via the
 * {@code @AuditInfo}/{@code @HistoryType} history-tracking framework, instead of
 * being overwritten or deleted.
 */
@Entity(name = "SGrosskunde")
@Table(name = "S_GKD")
@AuditInfo(tableName = "H_GKD", hClass = HGrosskunde.class)
@TupleConverter(converter = SGrosskundeConverter.class)
@HistoryType
public class SGrosskunde extends TEntity<Long, SGrosskunde> {
    @Id
    @Column(name = "GKD_ID")
    private Long id;
    @Column(name = "KURZ_BEZNG")
    private String kurzBezeichnung;
    @Column(name = "LANG_BEZNG")
    private String langBezeichnung;
    @Column(name = "GKD_NR")
    private Long grosskundeNr;
    @Column(name = "VS_DRUCK_TXT")
    private String druckText;

    protected SGrosskunde(){
        // Do not remove. For JPA.
    }

    /**
     * Public constructor used to create a new Grosskunde entity with default version (dhln = 1).
     *
     * @param historyValues     metadata about business and technical validity
     * @param kurzBezeichnung   short label
     * @param langBezeichnung   long label
     * @param grosskundeNr      customer number
     * @param druckText         print text
     */
    public SGrosskunde(HistoryValues historyValues,
                       String kurzBezeichnung,
                       String langBezeichnung,
                       Long grosskundeNr,
                       String druckText) {

        this(null, 1L, historyValues, kurzBezeichnung, langBezeichnung, grosskundeNr,druckText);

    }

    /**
     * Internal constructor used for full initialization including ID and version.
     *
     * @param id                entity ID
     * @param dhln              version number
     * @param historyValues     metadata about business and technical validity
     * @param kurzBezeichnung   short label
     * @param langBezeichnung   long label
     * @param grosskundeNr      customer number
     * @param druckText         print text
     */
    private SGrosskunde(Long id,
                       Number dhln,
                       HistoryValues historyValues,
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
     * Creates a deep copy of the current entity instance.
     *
     * @return a new SGrosskunde instance with the same data
     */
    @Override
    public SGrosskunde copy() {
        return new SGrosskunde(id,
                getVersion(),
                getHistoryValues(),
                kurzBezeichnung,
                langBezeichnung,
                grosskundeNr,
                druckText);
    }

    /**
     * Converts this entity into its historical record representation.
     *
     * @return HGrosskunde instance representing the historical version
     */
    @Override
    public HRecord<?, ?> hFromT() {
        return new HGrosskunde(getVersion(),
                getHistoryValues(),
                id,
                kurzBezeichnung,
                langBezeichnung,
                grosskundeNr,
                druckText);
    }

    public Long getId() {
        return id;
    }

    @Override
    public void setFachId(Long id) {
        this.id = id;
    }

    public String getkurzBezeichnung() {
        return kurzBezeichnung;
    }

    public void setkurzBezeichnung(String kurzBezeichnug) {
        this.kurzBezeichnung = kurzBezeichnug;
    }

    public String getlangBezeichnung() {
        return langBezeichnung;
    }

    public void setlangBezeichnung(String langBezeichnug) {
        this.langBezeichnung = langBezeichnug;
    }

    public Long getGrosskundeNr() {
        return grosskundeNr;
    }

    public void setGrosskundeNr(Long grosskundeNr) {
        this.grosskundeNr = grosskundeNr;
    }

    public String getDruckText() {
        return druckText;
    }

    public void setDruckText(String druckText) {
        this.druckText = druckText;
    }

    /**
     * Returns the start of the business validity period.
     */
    public LocalDateTime getDgbdat(){
        return getHistoryValues().businessValidity().start();
    }

    /**
     * Returns the end of the business validity period.
     */
    public LocalDateTime getDgedat(){
        return getHistoryValues().businessValidity().end();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SGrosskunde that = (SGrosskunde) o;
        return Objects.equals(id, that.id) && Objects.equals(kurzBezeichnung, that.kurzBezeichnung) && Objects.equals(langBezeichnung, that.langBezeichnung) && Objects.equals(grosskundeNr, that.grosskundeNr) && Objects.equals(druckText, that.druckText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, kurzBezeichnung, langBezeichnung, grosskundeNr, druckText);
    }

    @Override
    public String toString() {
        return "SGrosskunde{" +
                "id=" + id +
                ", kurzBezeichnug='" + kurzBezeichnung + '\'' +
                ", langBezeichnug='" + langBezeichnung + '\'' +
                ", grosskundeNr=" + grosskundeNr +
                ", druckText='" + druckText + '\'' +
                '}';
    }
}
