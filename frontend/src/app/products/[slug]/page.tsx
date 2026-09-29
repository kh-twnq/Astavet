import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { AddToCart } from "@/features/cart/add-to-cart";
import { formatCurrency } from "@/lib/format";
import { getProduct } from "@/lib/api";

export const dynamic = "force-dynamic";

type Props = { params: Promise<{ slug: string }> };

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const { slug } = await params;
  const product = await getProduct(slug).catch(() => null);
  return product ? { title: product.name, description: product.shortDescription } : { title: "Sản phẩm" };
}

export default async function ProductPage({ params }: Props) {
  const { slug } = await params;
  const product = await getProduct(slug).catch(() => null);
  if (!product) notFound();
  const variant = product.variants[0];

  return (
    <div className="container product-page">
      <div className="breadcrumbs"><Link href="/">Trang chủ</Link><span>/</span><span>{product.name}</span></div>
      <div className="product-detail-grid">
        <div className="product-gallery">
          <div className="main-product-image">
            <img src={product.images[0]?.url ?? "/images/astavet-product.svg"} alt={product.images[0]?.altText ?? product.name} />
          </div>
        </div>
        <div className="product-info">
          <p className="eyebrow">AstaVet · Chăm sóc thú cưng</p>
          <h1>{product.name}</h1>
          <div className="rating">★★★★★ <span>Sản phẩm dinh dưỡng hằng ngày</span></div>
          <div className="product-price">{variant ? formatCurrency(variant.price) : "Liên hệ"}</div>
          <p className="product-intro">{product.shortDescription}</p>
          <ul className="check-list"><li>Thông tin sản phẩm minh bạch</li><li>Đặt hàng không cần tài khoản</li><li>Thanh toán khi nhận hàng</li></ul>
          <AddToCart product={product} />
        </div>
      </div>
      <section className="product-description">
        <p className="eyebrow">Thông tin sản phẩm</p>
        <h2>Chăm sóc chủ động mỗi ngày</h2>
        <p>{product.description}</p>
        <div className="info-panels">
          <article><h3>Hướng dẫn sử dụng</h3><p>Sử dụng theo hướng dẫn trên nhãn sản phẩm hoặc tư vấn từ chuyên gia thú y.</p></article>
          <article><h3>Lưu ý</h3><p>Bảo quản nơi khô ráo, tránh xa trẻ em. Ngưng sử dụng và hỏi bác sĩ thú y khi có dấu hiệu bất thường.</p></article>
          <article><h3>Giao hàng</h3><p>Shop xác nhận đơn qua điện thoại trước khi giao. Thanh toán COD khi nhận sản phẩm.</p></article>
        </div>
      </section>
    </div>
  );
}
