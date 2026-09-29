"use client";

import { FormEvent, useState } from "react";
import { adminApi, ApiError } from "@/lib/api";
import type { Product } from "@/lib/types";

type ProductFormProps = {
  product?: Product | null;
  onSaved: (product: Product) => void;
  onCancel?: () => void;
};

export function ProductForm({ product, onSaved, onCancel }: ProductFormProps) {
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSaving(true);
    setError("");
    const form = event.currentTarget;
    const data = new FormData(form);
    try {
      const saved = await adminApi<Product>(product ? `/admin/products/${product.id}` : "/admin/products", {
        method: product ? "PUT" : "POST",
        body: JSON.stringify({
          name: data.get("name"),
          slug: data.get("slug"),
          shortDescription: data.get("shortDescription"),
          description: data.get("description"),
          status: product?.status ?? "ACTIVE",
          images: data.get("imageUrl") ? [{ url: data.get("imageUrl"), altText: data.get("name") }] : [],
          variants: [{
            id: product?.variants[0]?.id,
            name: data.get("variantName") || "Mặc định",
            sku: data.get("sku"),
            price: Number(data.get("price")),
            stockQuantity: Number(data.get("stockQuantity")),
            active: true,
          }],
        }),
      });
      form.reset();
      onSaved(saved);
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "Không thể lưu sản phẩm.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <form className="admin-form" onSubmit={submit}>
      <label>Tên sản phẩm<input name="name" required maxLength={255} defaultValue={product?.name} /></label>
      <label>Đường dẫn (slug)<input name="slug" required maxLength={180} pattern="[a-z0-9-]+" defaultValue={product?.slug} /></label>
      <label>Mô tả ngắn<textarea name="shortDescription" maxLength={500} defaultValue={product?.shortDescription ?? ""} /></label>
      <label>Mô tả đầy đủ<textarea name="description" maxLength={20000} defaultValue={product?.description ?? ""} /></label>
      <label>URL ảnh<input name="imageUrl" type="text" placeholder="/images/astavet-product.svg" defaultValue={product?.images[0]?.url ?? ""} /></label>
      <label>Tên quy cách<input name="variantName" placeholder="Hộp 130g" defaultValue={product?.variants[0]?.name ?? ""} /></label>
      <label>SKU<input name="sku" required maxLength={100} defaultValue={product?.variants[0]?.sku} /></label>
      <label>Giá (VND)<input name="price" type="number" min="0" required defaultValue={product?.variants[0]?.price} /></label>
      <label>Tồn kho<input name="stockQuantity" type="number" min="0" required defaultValue={product?.variants[0]?.stockQuantity} /></label>
      {error && <div className="form-error">{error}</div>}
      <div className="admin-actions">
        <button className="button primary" disabled={saving}>{saving ? "Đang lưu..." : product ? "Lưu thay đổi" : "Thêm sản phẩm"}</button>
        {product && <button className="button secondary" type="button" onClick={onCancel}>Hủy chỉnh sửa</button>}
      </div>
    </form>
  );
}
