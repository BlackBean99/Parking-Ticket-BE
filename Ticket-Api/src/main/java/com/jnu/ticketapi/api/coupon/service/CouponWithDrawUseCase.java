package com.jnu.ticketapi.api.coupon.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.jnu.ticketcommon.annotation.UseCase;
import com.jnu.ticketdomain.common.aop.redissonLock.RedissonLock;
import com.jnu.ticketdomain.domains.coupon.adaptor.CouponAdaptor;
import com.jnu.ticketdomain.domains.coupon.domain.Coupon;
import com.jnu.ticketdomain.domains.coupon.domain.Sector;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
@Slf4j
public class CouponWithDrawUseCase {
    private final RedissonClient redissonClient;
    private final CouponAdaptor couponAdaptor;
    /** 재고 감소 */
    @Transactional
    @RedissonLock(LockName = "재고감소", identifier = "id", paramClassType = Coupon.class)
    public void issueCoupon() {
        // 재고 감소 로직 구현
        Coupon coupon = couponAdaptor.findOpenCoupon();
        coupon.validateIssuePeriod();
        // 쿠폰 발급 저장소에 데이터 추가
        RList<String> couponStorage = redissonClient.getList("쿠폰 발급 저장소");
        // TODO 구간별 파라미터 추가되면 특정 구감만 재고 감소를 하도록 수정
        log.info("쿠폰 발급 저장소에 데이터 추가" + coupon);
        couponStorage.add(coupon.toString());
    }
    /** Worker method to process coupon issuance from the storage */
    @Scheduled(fixedDelay = 1000)
    @Transactional
    @Async
    public void processCouponIssuance() {
        RList<String> couponStorage = redissonClient.getList("쿠폰 발급 저장소");
        if (couponStorage.size() > 0) {
            // Pop coupon data from the storage
            String couponData = couponStorage.remove(0);
            // LPOP
            log.info(couponData);
            // Process the coupon data / DB insert
            processCouponData(couponData);
            // TODO 추가로 쿠폰 발급 저장소에 데이터 추가 예정 ( applicant table insert )
            // TODO Long userId = SecurityUtil.getCurrentId();
            // TODO applicantAdaptor.save(userId, couponId);
        }
    }

    private void processCouponData(String couponData) {
        Coupon coupon = new ObjectMapper().convertValue(couponData, Coupon.class);
        // TODO 잔여량
        List<Sector> sector = coupon.getSector();
        sector.forEach(Sector::decreaseCouponStock);
        log.info("쿠폰 발급 완료" + coupon.getCouponCode());
    }
}