import Link from "next/link";

export function Footer() {
  return (
    <footer className="site-footer">
      <div className="container footer-grid">
        <div>
          <div className="footer-brand">AstaVet</div>
          <p>Chăm sóc sức khỏe thú cưng bằng sản phẩm được chọn lọc và hướng dẫn minh bạch.</p>
        </div>
        <div>
          <h3>Hỗ trợ</h3>
          <Link href="/#products">Sản phẩm</Link>
          <Link href="/cart">Giỏ hàng</Link>
        </div>
        <div>
          <h3>Liên hệ</h3>
          <p>Hotline: 0900 000 000</p>
          <p>Email: hello@astavet.vn</p>
        </div>
      </div>
      <div className="copyright">© {new Date().getFullYear()} AstaVet. Thanh toán khi nhận hàng.</div>
    </footer>
  );
}

