package by.ladyka.poputka.data.repository.impl;

import by.ladyka.poputka.data.enums.BookingOverviewScope;
import by.ladyka.poputka.data.repository.BookingRepositoryCustom;
import by.ladyka.poputka.data.repository.TripBookingOverviewSqlFragments;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@Repository
public class BookingRepositoryImpl implements BookingRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Object[]> pageTripBookingOverview(Long tripId, BookingOverviewScope scope, long nowMillis, Pageable pageable) {
        Query dataQuery = entityManager.createNativeQuery(
                TripBookingOverviewSqlFragments.SELECT_OVERVIEW_BOOKINGS_SQL
                        + """
                                 ORDER BY lm.last_message_created DESC NULLS LAST, b.id DESC
                                 """);
        bindScopeParams(dataQuery, tripId, scope, nowMillis);
        if (pageable.isPaged()) {
            dataQuery.setFirstResult((int) pageable.getOffset())
                    .setMaxResults(pageable.getPageSize());
        }

        @SuppressWarnings("unchecked")
        List<Object[]> content = dataQuery.getResultList();

        if (!pageable.isPaged()) {
            return new PageImpl<>(content, Pageable.unpaged(), content.size());
        }

        Query countQuery =
                entityManager.createNativeQuery(TripBookingOverviewSqlFragments.COUNT_OVERVIEW_BOOKINGS_SQL);
        bindScopeParams(countQuery, tripId, scope, nowMillis);
        long total = extractNumber(countQuery.getSingleResult()).longValue();
        return new PageImpl<>(content, pageable, total);
    }

    private void bindScopeParams(Query query, Long tripId, BookingOverviewScope scope, long nowMillis) {
        query.setParameter("tripId", tripId);
        query.setParameter("nowMillis", nowMillis);
        query.setParameter("scopeAll", scope == BookingOverviewScope.ALL);
        query.setParameter("scopeActive", scope == BookingOverviewScope.ACTIVE);
        query.setParameter("scopeArchived", scope == BookingOverviewScope.ARCHIVED);
    }

    private Number extractNumber(Object value) {
        if (value == null) {
            return BigInteger.ZERO;
        }
        if (value instanceof BigInteger bi) {
            return bi;
        }
        if (value instanceof BigDecimal bd) {
            return bd.longValueExact();
        }
        if (value instanceof Long l) {
            return l;
        }
        if (value instanceof Integer i) {
            return i.longValue();
        }
        throw new IllegalStateException("Unexpected count type: " + value.getClass());
    }
}
