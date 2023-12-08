package com.jnu.ticketapi.api.registration.service;


import com.jnu.ticketapi.api.captcha.service.ValidateCaptchaPendingUseCase;
import com.jnu.ticketapi.api.coupon.service.CouponWithDrawUseCase;
import com.jnu.ticketapi.api.registration.model.request.FinalSaveRequest;
import com.jnu.ticketapi.api.registration.model.request.TemporarySaveRequest;
import com.jnu.ticketapi.api.registration.model.response.FinalSaveResponse;
import com.jnu.ticketapi.api.registration.model.response.GetRegistrationResponse;
import com.jnu.ticketapi.api.registration.model.response.GetRegistrationsResponse;
import com.jnu.ticketapi.api.registration.model.response.TemporarySaveResponse;
import com.jnu.ticketapi.application.helper.Converter;
import com.jnu.ticketapi.application.helper.Encryption;
import com.jnu.ticketapi.config.SecurityUtils;
import com.jnu.ticketcommon.annotation.UseCase;
import com.jnu.ticketcommon.message.ResponseMessage;
import com.jnu.ticketdomain.domains.captcha.adaptor.CaptchaAdaptor;
import com.jnu.ticketdomain.domains.coupon.adaptor.SectorAdaptor;
import com.jnu.ticketdomain.domains.coupon.domain.Sector;
import com.jnu.ticketdomain.domains.registration.adaptor.RegistrationAdaptor;
import com.jnu.ticketdomain.domains.registration.domain.Registration;
import com.jnu.ticketdomain.domains.user.adaptor.UserAdaptor;
import com.jnu.ticketdomain.domains.user.domain.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class RegistrationUseCase {
    private final RegistrationAdaptor registrationAdaptor;
    private final SectorAdaptor sectorAdaptor;
    private final Converter converter;
    private final UserAdaptor userAdaptor;
    private final CouponWithDrawUseCase couponWithDrawUseCase;
    private final Encryption encryption;
    private final CaptchaAdaptor captchaAdaptor;
    private final ValidateCaptchaPendingUseCase validateCaptchaPendingUseCase;

    public Registration findByUserId(Long userId) {
        return registrationAdaptor.findByUserId(userId);
    }

    public Registration save(Registration registration) {
        return registrationAdaptor.save(registration);
    }

    public User findById(Long userId) {
        return userAdaptor.findById(userId);
    }

    @Transactional(readOnly = true)
    public GetRegistrationResponse getRegistration(String email) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Registration registration = findByUserId(currentUserId);
        List<Sector> sectorList = sectorAdaptor.findAll();
        // 신청자가 임시저장을 하지 않았을 경우
        if (registration == null) {
            return GetRegistrationResponse.builder()
                    .sectors(converter.toSectorDto(sectorList))
                    .email(email)
                    .build();
        }
        // 신청자가 임시저장을 했을 경우
        return converter.toGetRegistrationResponseDto(
                email, registration, converter.toSectorDto(sectorList));
    }

    @Transactional
    public TemporarySaveResponse temporarySave(TemporarySaveRequest requestDto, String email) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User user = findById(currentUserId);
        Sector sector = sectorAdaptor.findById(requestDto.selectSectorId());
        Registration registration =
                converter.temporaryToRegistration(requestDto, sector, email, user);
        Registration jpaRegistration = save(registration);
        return converter.toTemporarySaveResponseDto(jpaRegistration);
    }

    @Transactional
    public FinalSaveResponse finalSave(FinalSaveRequest requestDto, String email) {
        Long captchaPendingId = encryption.decrypt(requestDto.captchaPendingCode());
        validateCaptchaPendingUseCase.execute(captchaPendingId, requestDto.captchaAnswer());
        /*
        임시저장을 했으면 isSave만 true로 변경
         */
        Long registrationId = requestDto.registrationId().orElse(null);
        if (registrationId != null) {
            Registration registration = registrationAdaptor.findById(registrationId);
            registration.updateIsSaved(true);
            return FinalSaveResponse.builder()
                    .registrationId(registration.getId())
                    .message(ResponseMessage.SUCCESS_FINAL_SAVE)
                    .build();
        }
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User user = findById(currentUserId);
        Sector sector = sectorAdaptor.findById(requestDto.selectSectorId());
        Registration registration = converter.finalToRegistration(requestDto, sector, email, user);
        Registration jpaRegistration = save(registration);
        couponWithDrawUseCase.issueCoupon();
        return converter.toFinalSaveResponseDto(jpaRegistration);
    }

    @Transactional(readOnly = true)
    public GetRegistrationsResponse getRegistrations() {
        List<Registration> registrations = registrationAdaptor.findAll();
        return GetRegistrationsResponse.of(registrations);
    }
}
