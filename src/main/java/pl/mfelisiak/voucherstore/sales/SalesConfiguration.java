package pl.mfelisiak.voucherstore.sales;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.mfelisiak.payu.http.JavaHttpPayUApiClient;
import pl.mfelisiak.payu.PayU;
import pl.mfelisiak.payu.PayUCredentials;
import pl.mfelisiak.voucherstore.productcatalog.ProductCatalogFacade;
import pl.mfelisiak.voucherstore.sales.basket.InMemoryBasketStorage;
import pl.mfelisiak.voucherstore.sales.offer.OfferMaker;
import pl.mfelisiak.voucherstore.sales.ordering.ReservationRepository;
import pl.mfelisiak.voucherstore.sales.payment.PayUPaymentGateway;
import pl.mfelisiak.voucherstore.sales.payment.PaymentGateway;
import pl.mfelisiak.voucherstore.sales.product.ProductCatalogProductDetailsProvider;
import pl.mfelisiak.voucherstore.sales.product.ProductDetailsProvider;

@Configuration
public class SalesConfiguration {

    @Bean
    SalesFacade salesFacade(ProductCatalogFacade productCatalogFacade, OfferMaker offerMaker, PaymentGateway paymentGateway, ReservationRepository reservationRepository) {
        return new SalesFacade(
                productCatalogFacade,
                new InMemoryBasketStorage(),
                () -> "customer_1",
                (productId) -> true,
                offerMaker,
                paymentGateway,
                reservationRepository);
    }

    @Bean
    PaymentGateway payUPaymentGateway() {
        return new PayUPaymentGateway(new PayU(
                PayUCredentials.productionOfEnv(),
                new JavaHttpPayUApiClient()
        ));
    }

    @Bean
    OfferMaker offerMaker(ProductDetailsProvider productDetailsProvider) {
        return new OfferMaker(productDetailsProvider);
    }

    @Bean
    ProductDetailsProvider productDetailsProvider(ProductCatalogFacade productCatalogFacade) {
        return new ProductCatalogProductDetailsProvider(productCatalogFacade);
    }
}
