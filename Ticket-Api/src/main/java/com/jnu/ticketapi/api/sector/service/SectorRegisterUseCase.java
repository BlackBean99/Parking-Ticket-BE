package com.jnu.ticketapi.api.sector.service;


import com.jnu.ticketapi.api.sector.request.SectorRegisterRequest;
import com.jnu.ticketcommon.annotation.UseCase;
import com.jnu.ticketdomain.domains.coupon.domain.Sector;
import com.jnu.ticketdomain.domains.coupon.out.SectorLoadPort;
import com.jnu.ticketdomain.domains.coupon.out.SectorRecordPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
@Slf4j
public class SectorRegisterUseCase {
    private final SectorRecordPort sectorRecordPort;
    private final SectorLoadPort sectorLoadPort;

    @Transactional
    public void execute(List<SectorRegisterRequest> sectors) {
        // to Sector List
        List<Sector> sectorList =
                sectors.stream()
                        .map(
                                sectorRegisterRequest ->
                                        new Sector(
                                                sectorRegisterRequest.sectorNumber(),
                                                sectorRegisterRequest.name(),
                                                sectorRegisterRequest.sectorCapacity(),
                                                sectorRegisterRequest.reserve()))
                        .toList();
        sectorRecordPort.saveAll(sectorList);
    }

    @Transactional
    public void update(List<SectorRegisterRequest> sectors) {
        // to Sector List
        List<Sector> prevSector = sectorLoadPort.findAll();
        List<Sector> sectorList =
                sectors.stream()
                        .map(
                                sectorRegisterRequest ->
                                        new Sector(
                                                sectorRegisterRequest.sectorNumber(),
                                                sectorRegisterRequest.name(),
                                                sectorRegisterRequest.sectorCapacity(),
                                                sectorRegisterRequest.reserve()))
                        .toList();
        sectorRecordPort.updateAll(prevSector, sectorList);
    }
}