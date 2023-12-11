package com.jnu.ticketdomain.domains.captcha.adaptor;


import com.jnu.ticketcommon.annotation.Adaptor;
import com.jnu.ticketdomain.domains.captcha.domain.Captcha;
import com.jnu.ticketdomain.domains.captcha.exception.NotFoundCaptchaException;
import com.jnu.ticketdomain.domains.captcha.exception.NotFoundCaptchaException;
import com.jnu.ticketdomain.domains.captcha.out.CaptchaLoadPort;
import com.jnu.ticketdomain.domains.captcha.repository.CaptchaRepository;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
@Adaptor
public class CaptchaAdaptor implements CaptchaLoadPort {
    private final CaptchaRepository captchaRepository;

    @Override
    public Captcha findByRandom() {
        long captchaTotalCount = captchaRepository.count();
        long randomOffset = ThreadLocalRandom.current().nextLong(captchaTotalCount);

        // id가 offset + 1 인 캡챠 조회
        return captchaRepository.findOneByOffset(randomOffset);
    }

    @Override
    public Captcha findById(long id) {
        return captchaRepository.findById(id)
                .orElseThrow(() -> NotFoundCaptchaException.EXCEPTION);
    }
}
