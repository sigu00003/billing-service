package de.abatultra.billingservice.grpc;

import de.abatultra.billing.BillingServiceGrpc;
import de.abatultra.billing.InvoiceRequest;
import de.abatultra.billing.InvoiceResponse;
import de.abatultra.billingservice.service.BillingService;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@GrpcService
public class BillingGrpcService extends BillingServiceGrpc.BillingServiceImplBase {

    @Override
    public void createInvoice (InvoiceRequest request, StreamObserver<InvoiceResponse> responseObserver) {

        System.out.println("Request erhalten");

        log.info ("createInvoice request received {}", request);
        log.info("calculate price...");

        BigDecimal fee = BillingService.calculateRegistrationFee(
                request.getOwner(),
                request.getStartLocation(),
                request.getEndLocation());

        String status = fee.compareTo(BigDecimal.ZERO) == 0 ? "PAID" : "ISSUED";

        InvoiceResponse response = InvoiceResponse.newBuilder()
                .setBillId(UUID.randomUUID().toString())
                .setOwner(request.getOwner())
                .setRegistrationFee(fee.toString())
                .setStatus(status)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
