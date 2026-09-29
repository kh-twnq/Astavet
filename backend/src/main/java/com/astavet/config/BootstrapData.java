package com.astavet.config;

import com.astavet.entity.AdminUser;
import com.astavet.entity.Product;
import com.astavet.entity.ProductStatus;
import com.astavet.entity.ProductVariant;
import com.astavet.repository.AdminUserRepository;
import com.astavet.repository.ProductRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BootstrapData implements ApplicationRunner {

    private final AdminUserRepository adminRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;
    private final StoreProperties properties;

    public BootstrapData(AdminUserRepository adminRepository, ProductRepository productRepository,
            PasswordEncoder passwordEncoder, StoreProperties properties) {
        this.adminRepository = adminRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedAdmin();
        seedProduct();
    }

    private void seedAdmin() {
        String email = properties.admin().email();
        String password = properties.admin().password();
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return;
        }
        adminRepository.findByEmailIgnoreCase(email).orElseGet(() -> adminRepository.save(
                new AdminUser(email.trim().toLowerCase(Locale.ROOT), passwordEncoder.encode(password))));
    }

    private void seedProduct() {
        if (productRepository.existsBySlug("astavet-130g")) {
            return;
        }
        Product product = new Product(
                "astavet-130g",
                "AstaVet 130g",
                "Bổ sung dinh dưỡng hằng ngày, hỗ trợ sức khỏe và sức bền cho thú cưng.",
                "AstaVet là sản phẩm bổ sung dành cho thú cưng. Vui lòng sử dụng theo hướng dẫn trên nhãn và tham khảo bác sĩ thú y khi cần thiết.",
                ProductStatus.ACTIVE);
        product.replaceImages(List.of(new Product.ProductImageInput(
                "/images/astavet-product.svg", "Hộp sản phẩm AstaVet 130g")));
        product.addVariant(new ProductVariant(product, "Hộp 130g", "ASTA-130G", 649000, 100, true));
        productRepository.save(product);
    }
}
