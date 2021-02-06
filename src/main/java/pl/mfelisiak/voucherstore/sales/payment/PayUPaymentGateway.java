package pl.mfelisiak.voucherstore.sales.payment;

import pl.mfelisiak.payu.PayU;
import pl.mfelisiak.payu.exceptions.PayUException;
import pl.mfelisiak.payu.model.Buyer;
import pl.mfelisiak.payu.model.OrderCreateRequest;
import pl.mfelisiak.payu.model.Product;
import pl.mfelisiak.voucherstore.sales.ordering.Reservation;

import java.util.Arrays;
import java.util.stream.Collectors;

public class PayUPaymentGateway implements PaymentGateway {
    private final PayU payU;
    public PayUPaymentGateway(PayU payU) {
        this.payU = payU;
    }

    @Override
    public PaymentDetails register(Reservation reservation) {
        var orderCreateRequest = formReservation(reservation);

        try {
            var response = payU.handle(orderCreateRequest);
            return PaymentDetails.builder()
                    .paymentId(response.getOrderId())
                    .paymentUrl(response.getRedirectUri())
                    .reservationId(reservation.getId())
                    .build();
        } catch (PayUException e) {
            throw new PaymentException(e);
        }
    }

    private OrderCreateRequest formReservation(Reservation reservation) {
        return OrderCreateRequest.builder()
                .customerIp("127.0.0.1")
                .description(String.format("Payment for reservations with id %s", reservation.getId()))
                .currencyCode("PLN")
                .totalAmount(reservation.getTotal())
                .extOrderId(reservation.getId())
                .buyer(Buyer.builder()
                        .email(reservation.getCustomerEmail())
                        .firstName(reservation.getCustomerFirstname())
                        .lastName(reservation.getCustomerLastname())
                        .language("pl")
                        .build())
                .products(
                        reservation.getItems().stream()
                            .map(ri -> new Product(ri.getName(), ri.getUnitPrice(), ri.getQuantity()))
                            .collect(Collectors.toList()))
                .build();
    }

    @Override
    public boolean isTrusted(PaymentUpdateStatusRequest paymentUpdateStatusRequest) {
        return payU.isTrusted(paymentUpdateStatusRequest.getBody(), paymentUpdateStatusRequest.getSignature());
    }
}
