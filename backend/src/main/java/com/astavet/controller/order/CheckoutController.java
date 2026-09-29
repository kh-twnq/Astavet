package com.astavet.controller.order;

import com.astavet.config.StoreProperties;
import com.astavet.dto.response.order.CheckoutConfigResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/checkout")
public class CheckoutController {

    private final StoreProperties storeProperties;

    public CheckoutController(StoreProperties storeProperties) {
        this.storeProperties = storeProperties;
    }

    @GetMapping("/config")
    public CheckoutConfigResponse config() {
        return new CheckoutConfigResponse(storeProperties.shippingFee());
    }
}
