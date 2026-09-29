"use client";

import { useCallback, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { ProductForm } from "@/features/admin/product-form";
import { adminApi, ApiError, getAdminOrders, getAdminProducts } from "@/lib/api";
import { formatCurrency, formatDate } from "@/lib/format";
import type { Order, OrderStatus, PaymentStatus, Product } from "@/lib/types";

type Session = { authenticated: boolean; email: string | null };
type Tab = "orders" | "products";

const statusLabels: Record<OrderStatus, string> = {
  NEW: "Mới",
  CONFIRMED: "Đã xác nhận",
  PACKING: "Đang đóng gói",
  SHIPPING: "Đang giao",
  DELIVERED: "Đã giao",
  CANCELLED: "Đã hủy",
  RETURNED: "Hoàn hàng",
};

const nextStatuses: Partial<Record<OrderStatus, OrderStatus[]>> = {
  NEW: ["CONFIRMED", "CANCELLED"],
  CONFIRMED: ["PACKING", "CANCELLED"],
  PACKING: ["SHIPPING", "CANCELLED"],
  SHIPPING: ["DELIVERED", "RETURNED"],
  DELIVERED: ["RETURNED"],
};

const paymentStatusLabels: Record<PaymentStatus, string> = {
  UNPAID: "Chưa thu COD",
  PAID: "Đã thu COD",
  REFUNDED: "Đã hoàn tiền",
};

export default function AdminPage() {
  const router = useRouter();
  const [tab, setTab] = useState<Tab>("orders");
  const [email, setEmail] = useState("");
  const [orders, setOrders] = useState<Order[]>([]);
  const [orderPage, setOrderPage] = useState(0);
  const [orderTotalPages, setOrderTotalPages] = useState(0);
  const [orderTotalElements, setOrderTotalElements] = useState(0);
  const [products, setProducts] = useState<Product[]>([]);
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadData = useCallback(async () => {
    try {
      const session = await adminApi<Session>("/admin/auth/session");
      if (!session.authenticated) {
        router.replace("/admin/login");
        return;
      }
      setEmail(session.email ?? "");
      const [ordersResult, productList] = await Promise.all([getAdminOrders(undefined, orderPage), getAdminProducts()]);
      setOrders(ordersResult.content);
      setOrderTotalPages(ordersResult.totalPages);
      setOrderTotalElements(ordersResult.totalElements);
      setProducts(productList);
    } catch (caught) {
      if (caught instanceof ApiError && caught.status === 401) router.replace("/admin/login");
      else setError("Không thể tải dữ liệu quản trị.");
    } finally {
      setLoading(false);
    }
  }, [orderPage, router]);

  useEffect(() => {
    const timeout = window.setTimeout(() => void loadData(), 0);
    return () => window.clearTimeout(timeout);
  }, [loadData]);

  async function updateOrderStatus(order: Order, status: OrderStatus) {
    try {
      const updated = await adminApi<Order>(`/admin/orders/${order.id}/status`, {
        method: "PATCH",
        body: JSON.stringify({ status }),
      });
      setOrders((current) => current.map((item) => item.id === updated.id ? updated : item));
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "Không thể cập nhật đơn hàng.");
    }
  }

  async function updatePaymentStatus(order: Order, paymentStatus: PaymentStatus) {
    try {
      const updated = await adminApi<Order>(`/admin/orders/${order.id}/payment-status`, {
        method: "PATCH",
        body: JSON.stringify({ paymentStatus }),
      });
      setOrders((current) => current.map((item) => item.id === updated.id ? updated : item));
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "Không thể cập nhật trạng thái thanh toán.");
    }
  }

  async function toggleProduct(product: Product) {
    const status = product.status === "ACTIVE" ? "ARCHIVED" : "ACTIVE";
    try {
      const updated = await adminApi<Product>(`/admin/products/${product.id}`, {
        method: "PUT",
        body: JSON.stringify({
          slug: product.slug,
          name: product.name,
          shortDescription: product.shortDescription,
          description: product.description,
          status,
          images: product.images.map((image) => ({ url: image.url, altText: image.altText })),
          variants: product.variants.map((variant) => ({ ...variant })),
        }),
      });
      setProducts((current) => current.map((item) => item.id === updated.id ? updated : item));
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "Không thể cập nhật sản phẩm.");
    }
  }

  function productSaved(product: Product) {
    setProducts((current) => {
      const exists = current.some((item) => item.id === product.id);
      return exists
        ? current.map((item) => item.id === product.id ? product : item)
        : [product, ...current];
    });
    setEditingProduct(null);
  }

  async function logout() {
    await adminApi<void>("/admin/auth/logout", { method: "POST" });
    router.replace("/admin/login");
  }

  if (loading) return <section className="admin-shell"><div className="container empty-state">Đang tải khu vực quản trị...</div></section>;

  return (
    <section className="admin-shell">
      <div className="container">
        <div className="admin-header"><div><p className="eyebrow">AstaVet Admin</p><h1>Quản lý cửa hàng</h1><p className="admin-meta">Đăng nhập: {email}</p></div><button className="button secondary" onClick={logout}>Đăng xuất</button></div>
        {error && <div className="form-error">{error}</div>}
        <div className="admin-tabs"><button className={tab === "orders" ? "active" : ""} onClick={() => setTab("orders")}>Đơn hàng ({orderTotalElements})</button><button className={tab === "products" ? "active" : ""} onClick={() => setTab("products")}>Sản phẩm ({products.length})</button></div>

        {tab === "orders" ? (
          <div className="admin-card admin-list">
            {orders.length === 0 && <p className="admin-meta">Chưa có đơn hàng.</p>}
            {orders.map((order) => (
              <article className="admin-list-item" key={order.id}>
                <div className="admin-list-item-head"><div><h3>{order.orderCode} · {order.customerName}</h3><p className="admin-meta">{order.phone} · {order.address} · {formatDate(order.createdAt)}</p></div><span className="status-pill">{statusLabels[order.status]}</span></div>
                <p>{order.items.map((item) => `${item.productName} × ${item.quantity}`).join(", ")}</p>
                <strong>{formatCurrency(order.total)} · COD · {paymentStatusLabels[order.paymentStatus]}</strong>
                {order.paymentHistory.length > 0 && <p className="admin-meta">Thanh toán: {order.paymentHistory.map((item) => `${paymentStatusLabels[item.newStatus]} bởi ${item.changedBy} lúc ${formatDate(item.createdAt)}`).join(" · ")}</p>}
                <div className="admin-actions">
                  {nextStatuses[order.status]?.map((status) => <button key={status} onClick={() => updateOrderStatus(order, status)}>Chuyển: {statusLabels[status]}</button>)}
                  {order.status === "DELIVERED" && order.paymentStatus === "UNPAID" && <button onClick={() => updatePaymentStatus(order, "PAID")}>Xác nhận đã thu COD</button>}
                  {order.status === "RETURNED" && order.paymentStatus === "PAID" && <button onClick={() => updatePaymentStatus(order, "REFUNDED")}>Xác nhận hoàn tiền</button>}
                </div>
              </article>
            ))}
            <div className="admin-actions">
              <button disabled={orderPage === 0} onClick={() => setOrderPage((current) => current - 1)}>Trang trước</button>
              <span>Trang {orderTotalPages === 0 ? 0 : orderPage + 1}/{orderTotalPages}</span>
              <button disabled={orderPage + 1 >= orderTotalPages} onClick={() => setOrderPage((current) => current + 1)}>Trang sau</button>
            </div>
          </div>
        ) : (
          <div className="admin-grid">
            <div className="admin-card"><h2>Danh sách sản phẩm</h2><div className="admin-list">{products.map((product) => <article className="admin-list-item" key={product.id}><div className="admin-list-item-head"><div><h3>{product.name}</h3><p className="admin-meta">{product.variants[0]?.sku} · Tồn {product.variants[0]?.stockQuantity ?? 0}</p></div><span className="status-pill">{product.status}</span></div><p>{product.variants[0] ? formatCurrency(product.variants[0].price) : "Chưa có giá"}</p><div className="admin-actions"><button onClick={() => setEditingProduct(product)}>Chỉnh sửa</button><button onClick={() => toggleProduct(product)}>{product.status === "ACTIVE" ? "Ẩn sản phẩm" : "Mở bán"}</button></div></article>)}</div></div>
            <div className="admin-card"><h2>{editingProduct ? "Chỉnh sửa sản phẩm" : "Thêm sản phẩm"}</h2><ProductForm key={editingProduct?.id ?? "new"} product={editingProduct} onSaved={productSaved} onCancel={() => setEditingProduct(null)} /></div>
          </div>
        )}
      </div>
    </section>
  );
}
