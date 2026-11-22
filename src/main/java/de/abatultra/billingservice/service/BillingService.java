package de.abatultra.billingservice.service;

import de.abatultra.billingservice.model.FreeOwner;
import de.abatultra.billingservice.model.StateCapital;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class BillingService {

    private static final BigDecimal CAPITAL_FEE = BigDecimal.valueOf(15);
    private static final BigDecimal STANDARD_FEE = BigDecimal.valueOf(7.5);

    public static BigDecimal calculateRegistrationFee(String owner, String startLocation, String endLocation) {

        BigDecimal fee = BigDecimal.ZERO;

        if (FreeOwner.isFreeOwner(owner)) {
            return fee;
        }

        if (StateCapital.isStateCapital(startLocation)) {
            fee = fee.add(CAPITAL_FEE);
        }

        if (StateCapital.isStateCapital(endLocation)) {
            fee = fee.add(CAPITAL_FEE);
        }

        // Standard fee
        fee = fee.add(STANDARD_FEE);
        return fee;

    }



}
