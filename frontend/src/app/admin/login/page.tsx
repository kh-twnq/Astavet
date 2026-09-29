"use client";

import { FormEvent, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { adminApi, ApiError } from "@/lib/api";

type Session = { authenticated: boolean; email: string | null };

export default function AdminLoginPage() {
  const router = useRouter();
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    adminApi<Session>("/admin/auth/session").then((session) => {
      if (session.authenticated) router.replace("/admin");
    }).catch(() => undefined);
  }, [router]);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitting(true);
    setError("");
    const data = new FormData(event.currentTarget);
    try {
      await adminApi<Session>("/admin/auth/login", {
        method: "POST",
        body: JSON.stringify({ email: data.get("email"), password: data.get("password") }),
      });
      router.replace("/admin");
    } catch (caught) {
      setError(caught instanceof ApiError && caught.status === 401
        ? "Email hoặc mật khẩu không đúng."
        : "Không thể đăng nhập. Vui lòng thử lại.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="admin-shell">
      <div className="admin-card admin-login">
        <p className="eyebrow">Khu vực nội bộ</p>
        <h1>Đăng nhập quản trị</h1>
        <p className="admin-meta">Quản lý sản phẩm, tồn kho và trạng thái đơn hàng.</p>
        <form className="admin-form" onSubmit={submit}>
          <label>Email<input name="email" type="email" required autoComplete="username" /></label>
          <label>Mật khẩu<input name="password" type="password" required autoComplete="current-password" /></label>
          {error && <div className="form-error">{error}</div>}
          <button className="button primary full" disabled={submitting}>{submitting ? "Đang đăng nhập..." : "Đăng nhập"}</button>
        </form>
      </div>
    </section>
  );
}

