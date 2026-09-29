"use client";

import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { Suspense } from "react";
import { formatCurrency } from "@/lib/format";

function SuccessContent() {
  const params = useSearchParams();
  const code = params.get("code");
  const total = Number(params.get("total") ?? 0);
  return <div className="success-card"><div className="success-icon">✓</div><p className="eyebrow">Đặt hàng thành công</p><h1>Cảm ơn bạn!</h1><p>Mã đơn hàng của bạn là <strong>{code}</strong>.</p><p>Tổng thanh toán khi nhận hàng: <strong>{formatCurrency(total)}</strong>.</p><p>Shop sẽ liên hệ theo số điện thoại bạn đã cung cấp để xác nhận đơn.</p><Link className="button primary" href="/">Về trang chủ</Link></div>;
}

export default function OrderSuccessPage() {
  return <div className="container success-page"><Suspense><SuccessContent /></Suspense></div>;
}
