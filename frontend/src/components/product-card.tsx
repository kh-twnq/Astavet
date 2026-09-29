import Link from "next/link";
import { formatCurrency } from "@/lib/format";
import type { Product } from "@/lib/types";

export function ProductCard({ product }: { product: Product }) {
  const variant = product.variants[0];
  const image = product.images[0];

  return (
    <article className="product-card">
      <Link className="product-image" href={`/products/${product.slug}`}>
        <img src={image?.url ?? "/images/astavet-product.svg"} alt={image?.altText ?? product.name} />
        {variant?.stockQuantity === 0 && <span className="stock-badge">Hết hàng</span>}
      </Link>
      <div className="product-card-body">
        <p className="eyebrow">Dinh dưỡng thú cưng</p>
        <h3><Link href={`/products/${product.slug}`}>{product.name}</Link></h3>
        <p>{product.shortDescription}</p>
        <div className="product-card-footer">
          <strong>{variant ? formatCurrency(variant.price) : "Liên hệ"}</strong>
          <Link className="text-link" href={`/products/${product.slug}`}>Xem chi tiết →</Link>
        </div>
      </div>
    </article>
  );
}

