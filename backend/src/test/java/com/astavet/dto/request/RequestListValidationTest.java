package com.astavet.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import com.astavet.dto.request.order.CreateOrderRequest;
import com.astavet.dto.request.product.UpsertProductRequest;
import com.astavet.entity.product.ProductStatus;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class RequestListValidationTest {

    @Test
    void rejectsNullCheckoutItem() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            CreateOrderRequest request = new CreateOrderRequest("Khách", "0901234567", "Địa chỉ", null,
                    "request-key", Collections.singletonList(null));

            assertThat(validator.validate(request)).anySatisfy(violation ->
                    assertThat(violation.getPropertyPath().toString()).contains("items"));
        }
    }

    @Test
    void rejectsNullProductImageAndVariant() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            UpsertProductRequest request = new UpsertProductRequest("product", "Sản phẩm", null, null,
                    ProductStatus.ACTIVE, Collections.singletonList(null), Collections.singletonList(null));

            assertThat(validator.validate(request)).extracting(violation ->
                    violation.getPropertyPath().toString()).anySatisfy(path -> assertThat(path).contains("images"))
                    .anySatisfy(path -> assertThat(path).contains("variants"));
        }
    }
}
