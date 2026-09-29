"use client";

import { useState } from "react";
import { useCart } from "./cart-context";
import type { Product } from "@/lib/types";

export function AddToCart({ product }: { product: Product }) {
  const variant = product.variants[0];
  const [quantity, setQuantity] = useState(1);
  const [added, setAdded] = useState(false);
  const { addItem } = useCart();

  if (!variant) return <p>Sản phẩm chưa có lựa chọn bán.</p>;
  const soldOut = variant.stockQuantity < 1;

  function add() {
    addItem({
      variantId: variant.id,
      productSlug: product.slug,
      productName: product.name,
      variantName: variant.name,
      imageUrl: product.images[0]?.url ?? "/images/astavet-product.svg",
      unitPrice: variant.price,
      quantity,
      maxQuantity: variant.stockQuantity,
    });
    setAdded(true);
    window.setTimeout(() => setAdded(false), 1800);
  }

  return (
    <div className="buy-box">
      <div className="stock-line">
        <span className={soldOut ? "dot sold-out" : "dot"} />
        {soldOut ? "Tạm hết hàng" : `Còn ${variant.stockQuantity} sản phẩm`}
      </div>
      <div className="buy-actions">
        <label className="quantity-picker">
          <span className="sr-only">Số lượng</span>
          <button type="button" onClick={() => setQuantity(Math.max(1, quantity - 1))}>−</button>
          <input readOnly value={quantity} aria-label="Số lượng" />
          <button type="button" onClick={() => setQuantity(Math.min(variant.stockQuantity, quantity + 1))}>+</button>
        </label>
        <button className="button primary grow" type="button" disabled={soldOut} onClick={add}>
          {soldOut ? "Hết hàng" : added ? "Đã thêm vào giỏ ✓" : "Thêm vào giỏ"}
        </button>
      </div>
      <p className="cod-note">Thanh toán tiền mặt khi nhận hàng · Không cần tài khoản</p>
    </div>
  );
}

