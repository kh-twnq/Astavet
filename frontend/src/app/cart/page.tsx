"use client";

import Link from "next/link";
import { useCart } from "@/features/cart/cart-context";
import { formatCurrency } from "@/lib/format";

export default function CartPage() {
  const { items, subtotal, updateQuantity, removeItem } = useCart();

  return (
    <div className="container narrow-page">
      <div className="page-title"><p className="eyebrow">Đơn hàng của bạn</p><h1>Giỏ hàng</h1></div>
      {items.length === 0 ? (
        <div className="empty-state"><h2>Giỏ hàng đang trống</h2><p>Hãy chọn sản phẩm phù hợp cho thú cưng của bạn.</p><Link className="button primary" href="/#products">Tiếp tục mua hàng</Link></div>
      ) : (
        <div className="cart-layout">
          <div className="cart-list">
            {items.map((item) => (
              <article className="cart-item" key={item.variantId}>
                <img src={item.imageUrl} alt={item.productName} />
                <div className="cart-item-main"><Link href={`/products/${item.productSlug}`}><h3>{item.productName}</h3></Link><p>{item.variantName}</p><button className="link-button" onClick={() => removeItem(item.variantId)}>Xóa</button></div>
                <div className="cart-item-controls"><div className="quantity-picker"><button onClick={() => updateQuantity(item.variantId, item.quantity - 1)}>−</button><input readOnly value={item.quantity} /><button onClick={() => updateQuantity(item.variantId, item.quantity + 1)}>+</button></div><strong>{formatCurrency(item.unitPrice * item.quantity)}</strong></div>
              </article>
            ))}
          </div>
          <aside className="order-summary"><h2>Tóm tắt đơn hàng</h2><div><span>Tạm tính</span><strong>{formatCurrency(subtotal)}</strong></div><div><span>Phí giao hàng</span><span>Tính khi đặt hàng</span></div><div className="summary-total"><span>Tổng dự kiến</span><strong>{formatCurrency(subtotal)}</strong></div><Link className="button primary full" href="/checkout">Tiến hành đặt hàng</Link><p>Chỉ thanh toán COD khi nhận hàng.</p></aside>
        </div>
      )}
    </div>
  );
}

