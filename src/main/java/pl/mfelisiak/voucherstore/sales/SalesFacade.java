package pl.mfelisiak.voucherstore.sales;

import pl.mfelisiak.voucherstore.productcatalog.Product;
import pl.mfelisiak.voucherstore.productcatalog.ProductCatalogFacade;
import pl.mfelisiak.voucherstore.sales.basket.Basket;
import pl.mfelisiak.voucherstore.sales.basket.InMemoryBasketStorage;
import pl.mfelisiak.voucherstore.sales.offer.Offer;
import pl.mfelisiak.voucherstore.sales.offer.OfferMaker;
import pl.mfelisiak.voucherstore.sales.ordering.ClientData;
import pl.mfelisiak.voucherstore.sales.ordering.OfferChangedException;
import pl.mfelisiak.voucherstore.sales.ordering.Reservation;
import pl.mfelisiak.voucherstore.sales.ordering.ReservationRepository;
import pl.mfelisiak.voucherstore.sales.payment.PaymentDetails;
import pl.mfelisiak.voucherstore.sales.payment.PaymentGateway;
import pl.mfelisiak.voucherstore.sales.payment.PaymentUpdateStatusRequest;
import pl.mfelisiak.voucherstore.sales.payment.PaymentVerificationException;

public class SalesFacade {

    ProductCatalogFacade productCatalogFacade;
    InMemoryBasketStorage basketStorage;
    CurrentCustomerContext currentCustomerContext;
    Inventory inventory;
    OfferMaker offerMaker;
    PaymentGateway paymentGateway;
    private final ReservationRepository reservationRepository;

    public SalesFacade(ProductCatalogFacade productCatalogFacade, InMemoryBasketStorage basketStorage, CurrentCustomerContext currentCustomerContext, Inventory inventory, OfferMaker offerMaker, PaymentGateway paymentGateway, ReservationRepository reservationRepository) {
        this.productCatalogFacade = productCatalogFacade;
        this.basketStorage = basketStorage;
        this.currentCustomerContext = currentCustomerContext;
        this.inventory = inventory;
        this.offerMaker = offerMaker;
        this.paymentGateway = paymentGateway;
        this.reservationRepository = reservationRepository;
    }

    public void addProduct(String productId1) {
        Product product = productCatalogFacade.getById(productId1);
        Basket basket = basketStorage.loadForCustomer(getCurrentCustomerId())
                .orElse(Basket.empty());

        basket.add(product, inventory);

        basketStorage.addForCustomer(getCurrentCustomerId(), basket);
    }

    private String getCurrentCustomerId() {
        return currentCustomerContext.getCurrentCustomerId();
    }

    public Offer getCurrentOffer() {
        Basket basket = basketStorage.loadForCustomer(getCurrentCustomerId())
                .orElse(Basket.empty());
        return offerMaker.calculateOffer(basket.getBasketItems());
    }

    public PaymentDetails acceptOffer(Offer seenOffer, ClientData clientData) {
        Basket basket = basketStorage.loadForCustomer(getCurrentCustomerId())
                .orElse(Basket.empty());

        Offer currentOffer = offerMaker.calculateOffer(basket.getBasketItems());

        if (!seenOffer.isEqual(currentOffer)) {
            throw new OfferChangedException();
        }

        Reservation reservation = Reservation.of(currentOffer, clientData);

        PaymentDetails reservationPaymentDetails = paymentGateway.register(reservation);

        reservation.fillWithPayment(reservationPaymentDetails);

        reservationRepository.save(reservation);

        return  reservationPaymentDetails;
    }

    public void handlePaymentStatusChanged(PaymentUpdateStatusRequest paymentUpdateStatusRequest) {
        if (!paymentGateway.isTrusted(paymentUpdateStatusRequest)) {
            throw new PaymentVerificationException();
        }
    }
}
