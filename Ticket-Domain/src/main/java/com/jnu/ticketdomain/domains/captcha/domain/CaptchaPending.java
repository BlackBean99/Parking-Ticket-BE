package com.jnu.ticketdomain.domains.captcha.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = "captcha_pending_tb")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EntityListeners(AuditingEntityListener.class)
public class CaptchaPending {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_pending", nullable = false)
    @ColumnDefault("true")
    private Boolean isPending;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "captcha_id", nullable = false)
    private Captcha captcha;

    @Builder
    public CaptchaPending(Captcha captcha) {
        this.captcha = captcha;
    }

    public boolean validate(int answer) {
        if (captcha.getAnswer() != answer) {
            return false;
        }

        return true;
    }

    public void confirm() {
        isPending = false;
    }
}
