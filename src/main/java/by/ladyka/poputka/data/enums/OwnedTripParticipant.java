package by.ladyka.poputka.data.enums;

/**
 * {@code GET /api/trip/owned}: явные значения {@code owner}, {@code passenger};
 * если параметр не передан — оба варианта (владелец или бронь пассажира).
 */
public enum OwnedTripParticipant {

    /** Владелец поездки или есть бронь как пассажир (параметр {@code participant} не передан). */
    ALL,
    OWNER,
    PASSENGER
}
