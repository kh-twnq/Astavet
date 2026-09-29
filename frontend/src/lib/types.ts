export type ProductStatus = "DRAFT" | "ACTIVE" | "ARCHIVED";

export type ProductImage = {
  id: string | null;
  url: string;
  altText: string | null;
  sortOrder: number;
};

export type ProductVariant = {
  id: string;
  name: string;
  sku: string;
  price: number;
  stockQuantity: number;
  active: boolean;
};

export type Product = {
  id: string;
  slug: string;
  name: string;
  shortDescription: string | null;
  description: string | null;
  status: ProductStatus;
  images: ProductImage[];
  variants: ProductVariant[];
  createdAt: string;
  updatedAt: string;
};

export type CartItem = {
  variantId: string;
  productSlug: string;
  productName: string;
  variantName: string;
  imageUrl: string;
  unitPrice: number;
  quantity: number;
  maxQuantity: number;
};

export type OrderStatus =
  | "NEW"
  | "CONFIRMED"
  | "PACKING"
  | "SHIPPING"
  | "DELIVERED"
  | "CANCELLED"
  | "RETURNED";

export type PaymentStatus = "UNPAID" | "PAID" | "REFUNDED";

export type CheckoutConfig = {
  shippingFee: number;
};

export type Order = {
  id: string;
  orderCode: string;
  customerName: string;
  phone: string;
  address: string;
  note: string | null;
  subtotal: number;
  shippingFee: number;
  total: number;
  paymentMethod: "COD";
  paymentStatus: PaymentStatus;
  status: OrderStatus;
  items: Array<{
    productId: string;
    variantId: string;
    productName: string;
    variantName: string;
    sku: string;
    unitPrice: number;
    quantity: number;
    lineTotal: number;
  }>;
  history: Array<{
    previousStatus: OrderStatus | null;
    newStatus: OrderStatus;
    changedBy: string;
    createdAt: string;
  }>;
  paymentHistory: Array<{
    previousStatus: PaymentStatus;
    newStatus: PaymentStatus;
    changedBy: string;
    createdAt: string;
  }>;
  createdAt: string;
  updatedAt: string;
};

export type PageResponse<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
};
