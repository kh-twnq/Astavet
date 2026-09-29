"use client";

import Link from "next/link";
import { useCart } from "@/features/cart/cart-context";

export function Header() {
  const { count } = useCart();

  return (
    <header className="site-header">
      <div className="announcement">Miễn phí tư vấn sản phẩm · Thanh toán COD khi nhận hàng</div>
      <div className="container nav-row">
        <Link className="brand" href="/" aria-label="AstaVet - Trang chủ">
          <span className="brand-mark">A</span>
          <span><strong>AstaVet</strong><small>Healthy pets, happy homes</small></span>
        </Link>
        <nav aria-label="Điều hướng chính">
          <Link href="/#products">Sản phẩm</Link>
          <Link href="/#story">Về chúng tôi</Link>
          <Link href="/admin/login">Quản trị</Link>
          <Link className="cart-link" href="/cart">Giỏ hàng <span>{count}</span></Link>
        </nav>
      </div>
    </header>
  );
}

