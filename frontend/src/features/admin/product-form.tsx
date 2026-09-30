"use client";

import { FormEvent, useState } from "react";
import { adminApi, ApiError } from "@/lib/api";
import type { Product } from "@/lib/types";

type ProductFormProps = {
  product?: Product | null;
  onSaved: (product: Product) => void;
  onCancel?: () => void;
};

type ImageDraft = { url: string; altText: string };
type VariantDraft = {
  id: string | null;
  version: number | null;
  name: string;
  sku: string;
  price: number;
  stockQuantity: number;
  active: boolean;
};

const emptyImage = (): ImageDraft => ({ url: "", altText: "" });
const emptyVariant = (): VariantDraft => ({
  id: null,
  version: null,
  name: "Mặc định",
  sku: "",
  price: 0,
  stockQuantity: 0,
  active: true,
});

export function ProductForm({ product, onSaved, onCancel }: ProductFormProps) {
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);
  const [images, setImages] = useState<ImageDraft[]>(() =>
    product?.images.map((image) => ({ url: image.url, altText: image.altText ?? "" })) ?? [],
  );
  const [variants, setVariants] = useState<VariantDraft[]>(() =>
    product?.variants.map((variant) => ({ ...variant })) ?? [emptyVariant()],
  );

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
          images: images.filter((image) => image.url.trim()).map((image) => ({
            url: image.url,
            altText: image.altText,
          })),
          variants,
        }),
      });
      form.reset();
      setImages([]);
      setVariants([emptyVariant()]);
      onSaved(saved);
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "Không thể lưu sản phẩm.");
    } finally {
      setSaving(false);
    }
  }

  function updateImage(index: number, field: keyof ImageDraft, value: string) {
    setImages((current) => current.map((image, position) =>
      position === index ? { ...image, [field]: value } : image));
  }

  function updateVariant<K extends keyof VariantDraft>(index: number, field: K, value: VariantDraft[K]) {
    setVariants((current) => current.map((variant, position) =>
      position === index ? { ...variant, [field]: value } : variant));
  }

  return (
    <form className="admin-form" onSubmit={submit}>
      <label>Tên sản phẩm<input name="name" required maxLength={255} defaultValue={product?.name} /></label>
      <label>Đường dẫn (slug)<input name="slug" required maxLength={180} pattern="[a-z0-9-]+" defaultValue={product?.slug} /></label>
      <label>Mô tả ngắn<textarea name="shortDescription" maxLength={500} defaultValue={product?.shortDescription ?? ""} /></label>
      <label>Mô tả đầy đủ<textarea name="description" maxLength={20000} defaultValue={product?.description ?? ""} /></label>

      <div className="admin-list">
        <div className="admin-list-item-head"><h3>Hình ảnh</h3><button type="button" onClick={() => setImages((current) => [...current, emptyImage()])}>Thêm ảnh</button></div>
        {images.length === 0 && <p className="admin-meta">Sản phẩm chưa có hình ảnh.</p>}
        {images.map((image, index) => (
          <div className="admin-list-item" key={`image-${index}`}>
            <label>URL ảnh<input required value={image.url} onChange={(event) => updateImage(index, "url", event.target.value)} placeholder="/images/product.svg" /></label>
            <label>Mô tả ảnh<input maxLength={255} value={image.altText} onChange={(event) => updateImage(index, "altText", event.target.value)} /></label>
            <button type="button" onClick={() => setImages((current) => current.filter((_, position) => position !== index))}>Xóa ảnh</button>
          </div>
        ))}
      </div>

      <div className="admin-list">
        <div className="admin-list-item-head"><h3>Biến thể</h3><button type="button" onClick={() => setVariants((current) => [...current, emptyVariant()])}>Thêm biến thể</button></div>
        {variants.map((variant, index) => (
          <div className="admin-list-item" key={variant.id ?? `variant-${index}`}>
            <label>Tên quy cách<input required maxLength={255} value={variant.name} onChange={(event) => updateVariant(index, "name", event.target.value)} /></label>
            <label>SKU<input required maxLength={100} value={variant.sku} onChange={(event) => updateVariant(index, "sku", event.target.value)} /></label>
            <label>Giá (VND)<input type="number" min="0" required value={variant.price} onChange={(event) => updateVariant(index, "price", Number(event.target.value))} /></label>
            <label>Tồn kho<input type="number" min="0" required value={variant.stockQuantity} onChange={(event) => updateVariant(index, "stockQuantity", Number(event.target.value))} /></label>
            <label><input type="checkbox" checked={variant.active} onChange={(event) => updateVariant(index, "active", event.target.checked)} /> Đang bán</label>
            <button type="button" disabled={variants.length === 1} onClick={() => setVariants((current) => current.filter((_, position) => position !== index))}>Xóa biến thể</button>
          </div>
        ))}
      </div>

      {error && <div className="form-error">{error}</div>}
      <div className="admin-actions">
        <button className="button primary" disabled={saving}>{saving ? "Đang lưu..." : product ? "Lưu thay đổi" : "Thêm sản phẩm"}</button>
        {product && <button className="button secondary" type="button" onClick={onCancel}>Hủy chỉnh sửa</button>}
      </div>
    </form>
  );
}
