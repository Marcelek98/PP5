package pl.mfelisiak.voucherstore.sales.payment;

import pl.mfelisiak.payu.exceptions.PayUException;

public class PaymentException extends IllegalStateException {
    public PaymentException(PayUException e) {
        super(e);
    }
}
