import { ProductCard } from "@/components/product-card";
import { getProducts } from "@/lib/api";

export const dynamic = "force-dynamic";

export default async function HomePage() {
  const products = await getProducts().catch(() => []);

  return (
    <>
      <section className="hero">
        <div className="container hero-grid">
          <div className="hero-copy">
            <p className="eyebrow">Dinh dưỡng khoa học cho thú cưng</p>
            <h1>Sức khỏe bền bỉ.<br /><em>Niềm vui mỗi ngày.</em></h1>
            <p className="hero-lead">Giải pháp bổ sung dinh dưỡng dễ sử dụng, được trình bày minh bạch và giao tận nhà trên toàn quốc.</p>
            <div className="hero-actions">
              <a className="button primary" href="#products">Khám phá sản phẩm</a>
              <a className="button secondary" href="#story">Tìm hiểu AstaVet</a>
            </div>
            <div className="trust-row">
              <span>✓ Đặt hàng nhanh</span><span>✓ COD an toàn</span><span>✓ Tư vấn tận tâm</span>
            </div>
          </div>
          <div className="hero-visual">
            <div className="sun-shape" />
            <img src="/images/astavet-product.svg" alt="Sản phẩm AstaVet" />
            <div className="floating-card top"><strong>100%</strong><span>Thanh toán COD</span></div>
            <div className="floating-card bottom"><strong>Hỗ trợ</strong><span>Chăm sóc mỗi ngày</span></div>
          </div>
        </div>
      </section>

      <section className="benefits">
        <div className="container benefit-grid">
          <div><span>01</span><h3>Chọn lọc kỹ</h3><p>Thông tin thành phần và hướng dẫn rõ ràng.</p></div>
          <div><span>02</span><h3>Dễ sử dụng</h3><p>Phù hợp để bổ sung vào khẩu phần hằng ngày.</p></div>
          <div><span>03</span><h3>Giao tận nơi</h3><p>Đặt trực tuyến, thanh toán khi nhận hàng.</p></div>
        </div>
      </section>

      <section className="section container" id="products">
        <div className="section-heading">
          <div><p className="eyebrow">Cửa hàng</p><h2>Sản phẩm nổi bật</h2></div>
          <p>Được thiết kế cho nhịp sống khỏe mạnh của thú cưng và sự an tâm của bạn.</p>
        </div>
        {products.length > 0 ? (
          <div className="product-grid">{products.map((product) => <ProductCard key={product.id} product={product} />)}</div>
        ) : (
          <div className="empty-state">Sản phẩm sẽ xuất hiện khi backend đang chạy.</div>
        )}
      </section>

      <section className="story-section" id="story">
        <div className="container story-grid">
          <div className="story-art"><span>Vì những người bạn bốn chân</span></div>
          <div>
            <p className="eyebrow">Câu chuyện AstaVet</p>
            <h2>Chăm sóc tốt bắt đầu từ những điều nhỏ mỗi ngày.</h2>
            <p>Chúng tôi hướng đến trải nghiệm mua hàng đơn giản, thông tin dễ hiểu và dịch vụ đồng hành lâu dài cùng người nuôi thú cưng.</p>
            <div className="story-stats"><div><strong>COD</strong><span>Nhận hàng rồi trả</span></div><div><strong>24h</strong><span>Tiếp nhận đơn nhanh</span></div></div>
          </div>
        </div>
      </section>
    </>
  );
}

