package de.abatultra.billingservice.grpc;

import de.abatultra.billing.BillingServiceGrpc;
import de.abatultra.billing.InvoiceRequest;
import de.abatultra.billing.InvoiceResponse;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
public class BillingGrpcService extends BillingServiceGrpc.BillingServiceImplBase {

    @Override
    public void createInvoice (InvoiceRequest request, StreamObserver<InvoiceResponse> responseObserver) {

        System.out.println("Request erhalten");

        log.info ("createInvoice request received {}", request);

        InvoiceResponse response = InvoiceResponse.newBuilder()
                .setBillId("123")
                .setOwner("Test")
                .setRegistrationFee("10.5")
                .setStatus("ACTIVE")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
