package com.jnu.ticketdomain.domains.registration.out;


import com.jnu.ticketdomain.domains.registration.domain.Registration;

public interface RegistrationRecordPort {
    Registration save(Registration registration);
}
