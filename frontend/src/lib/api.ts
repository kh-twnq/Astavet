import type { PageResponse, Product, Order, OrderStatus } from "./types";

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080/api/v1";

export class ApiError extends Error {
  constructor(
    message: string,
    public readonly status: number,
    public readonly code?: string,
  ) {
    super(message);
  }
}

async function parseResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const problem = await response.json().catch(() => null);
    throw new ApiError(problem?.detail ?? "Không thể xử lý yêu cầu.", response.status, problem?.code);
  }
  if (response.status === 204) {
    return undefined as T;
  }
  return response.json() as Promise<T>;
}

function csrfToken() {
  if (typeof document === "undefined") return null;
  const entry = document.cookie.split("; ").find((cookie) => cookie.startsWith("XSRF-TOKEN="));
  return entry ? decodeURIComponent(entry.split("=")[1]) : null;
}

export async function publicApi<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_URL}${path}`, {
    ...init,
    headers: { "Content-Type": "application/json", ...init?.headers },
    cache: init?.method ? undefined : "no-store",
  });
  return parseResponse<T>(response);
}

export async function adminApi<T>(path: string, init?: RequestInit): Promise<T> {
  const method = init?.method?.toUpperCase() ?? "GET";
  if (method !== "GET" && method !== "HEAD" && !csrfToken()) {
    await fetch(`${API_URL}/admin/auth/session`, { credentials: "include" });
  }
  const token = csrfToken();
  const response = await fetch(`${API_URL}${path}`, {
    ...init,
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
      ...(token ? { "X-XSRF-TOKEN": token } : {}),
      ...init?.headers,
    },
  });
  return parseResponse<T>(response);
}

export const getProducts = () => publicApi<Product[]>("/products");
export const getProduct = (slug: string) => publicApi<Product>(`/products/${encodeURIComponent(slug)}`);
export const getAdminProducts = () => adminApi<Product[]>("/admin/products");
export const getAdminOrders = (status?: OrderStatus) =>
  adminApi<PageResponse<Order>>(`/admin/orders?size=100${status ? `&status=${status}` : ""}`);

