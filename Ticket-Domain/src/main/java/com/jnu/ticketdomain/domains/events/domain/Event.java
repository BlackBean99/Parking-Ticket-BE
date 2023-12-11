package com.jnu.ticketdomain.domains.events.domain;

import static com.jnu.ticketdomain.domains.events.domain.EventStatus.CALCULATING;
import static com.jnu.ticketdomain.domains.events.domain.EventStatus.CLOSED;
import static com.jnu.ticketdomain.domains.events.domain.EventStatus.OPEN;
import static com.jnu.ticketdomain.domains.events.domain.EventStatus.READY;

import com.jnu.ticketcommon.exception.TicketCodeException;
import com.jnu.ticketdomain.common.domainEvent.Events;
import com.jnu.ticketdomain.common.vo.DateTimePeriod;
import com.jnu.ticketdomain.domains.events.event.CouponExpiredEvent;
import com.jnu.ticketdomain.domains.events.event.EventStatusChangeEvent;
import com.jnu.ticketdomain.domains.events.exception.AlreadyCalculatingStatusException;
import com.jnu.ticketdomain.domains.events.exception.AlreadyCloseStatusException;
import com.jnu.ticketdomain.domains.events.exception.AlreadyOpenStatusException;
import com.jnu.ticketdomain.domains.events.exception.AlreadyReadyStatusException;
import com.jnu.ticketdomain.domains.events.exception.CannotModifyOpenEventException;
import com.jnu.ticketdomain.domains.events.exception.InvalidPeriodEventException;
import com.jnu.ticketdomain.domains.events.exception.NotOpenEventPeriodException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long id;

    // 쿠폰 pubsub을 위한 일련번호 -> UUID String 6자리
    @Column(name = "coupon_code")
    private String couponCode;

    @Embedded private DateTimePeriod dateTimePeriod;

    @Enumerated(EnumType.STRING)
    private EventStatus eventStatus;
    // 쿠폰 발행 가능 기간

    // 구간별 정보
    //    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @OneToMany(mappedBy = "coupon", cascade = CascadeType.ALL, orphanRemoval = true)
    //    @JoinColumn(name = "sector_id")
    private List<Sector> sector = new ArrayList<>();

    @Builder
    public Event(DateTimePeriod dateTimePeriod, List<Sector> sector) {
        this.couponCode = UUID.randomUUID().toString().substring(0, 6);
        this.dateTimePeriod = dateTimePeriod;
        this.sector = sector;
        this.eventStatus = EventStatus.READY;
    }

    @PostPersist
    public void postPersist() {
        Events.raise(CouponExpiredEvent.from(dateTimePeriod));
    }

    public void validateOpenStatus() {
        if (eventStatus == OPEN) throw CannotModifyOpenEventException.EXCEPTION;
    }

    public void validateNotOpenStatus() {
        if (eventStatus != OPEN) throw NotOpenEventPeriodException.EXCEPTION;
    }

    public void validateIssuePeriod() {
        LocalDateTime nowTime = LocalDateTime.now();
        if (dateTimePeriod.contains(nowTime)
                || dateTimePeriod.getEndAt().isBefore(nowTime)
                || dateTimePeriod.getEndAt().isBefore(dateTimePeriod.getStartAt())) {
            throw InvalidPeriodEventException.EXCEPTION;
        }
    }

    private void updateStatus(EventStatus status, TicketCodeException exception) {
        if (this.eventStatus == status) throw exception;
        this.eventStatus = status;
        Events.raise(EventStatusChangeEvent.of(this));
    }

    public void open() {
        validateOpenStatus();

        updateStatus(OPEN, AlreadyOpenStatusException.EXCEPTION);
    }

    public void calculate() {
        updateStatus(CALCULATING, AlreadyCalculatingStatusException.EXCEPTION);
    }

    public void ready() {
        updateStatus(READY, AlreadyReadyStatusException.EXCEPTION);
    }

    public void close() {
        updateStatus(CLOSED, AlreadyCloseStatusException.EXCEPTION);
    }
}
