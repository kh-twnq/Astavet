"use client";

import { FormEvent, useRef, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useCart } from "@/features/cart/cart-context";
import { ApiError, publicApi } from "@/lib/api";
import { formatCurrency } from "@/lib/format";
import type { Order } from "@/lib/types";

export default function CheckoutPage() {
  const { items, subtotal, clear } = useCart();
  const router = useRouter();
  const idempotencyKey = useRef<string | null>(null);
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (items.length === 0) return;
    setSubmitting(true);
    setError("");
    const form = new FormData(event.currentTarget);
    idempotencyKey.current ??= crypto.randomUUID();
    try {
      const order = await publicApi<Order>("/orders", {
        method: "POST",
        body: JSON.stringify({
          customerName: form.get("customerName"),
          phone: form.get("phone"),
          address: form.get("address"),
          note: form.get("note"),
          idempotencyKey: idempotencyKey.current,
          items: items.map((item) => ({ variantId: item.variantId, quantity: item.quantity })),
        }),
      });
      clear();
      router.push(`/order-success?code=${encodeURIComponent(order.orderCode)}&total=${order.total}`);
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "Không thể tạo đơn. Vui lòng thử lại.");
    } finally {
      setSubmitting(false);
    }
  }

  if (items.length === 0) {
    return <div className="container narrow-page"><div className="empty-state"><h1>Chưa có sản phẩm để đặt hàng</h1><Link className="button primary" href="/#products">Chọn sản phẩm</Link></div></div>;
  }

  return (
    <div className="container narrow-page">
      <div className="page-title"><p className="eyebrow">Thanh toán COD</p><h1>Thông tin nhận hàng</h1><p>Shop sẽ gọi xác nhận trước khi giao.</p></div>
      <div className="checkout-layout">
        <form className="checkout-form" onSubmit={submit}>
          <label>Họ và tên<input name="customerName" required maxLength={150} autoComplete="name" placeholder="Nguyễn Văn A" /></label>
          <label>Số điện thoại<input name="phone" required pattern="(\+?84|0)[0-9]{9,10}" autoComplete="tel" placeholder="0901234567" /></label>
          <label>Địa chỉ giao hàng<textarea name="address" required maxLength={500} autoComplete="street-address" placeholder="Số nhà, đường, phường/xã, quận/huyện, tỉnh/thành" /></label>
          <label>Ghi chú <span>(không bắt buộc)</span><textarea name="note" maxLength={1000} placeholder="Thời gian nhận hàng hoặc lưu ý khác" /></label>
          {error && <div className="form-error" role="alert">{error}</div>}
          <button className="button primary full" disabled={submitting}>{submitting ? "Đang tạo đơn..." : "Đặt hàng COD"}</button>
        </form>
        <aside className="order-summary"><h2>Đơn hàng</h2>{items.map((item) => <div className="checkout-line" key={item.variantId}><span>{item.productName} × {item.quantity}</span><strong>{formatCurrency(item.unitPrice * item.quantity)}</strong></div>)}<div><span>Phí giao hàng</span><span>30.000 ₫</span></div><div className="summary-total"><span>Tổng dự kiến</span><strong>{formatCurrency(subtotal + 30_000)}</strong></div><p>Giá cuối cùng được hệ thống xác minh khi tạo đơn.</p></aside>
      </div>
    </div>
  );
}
