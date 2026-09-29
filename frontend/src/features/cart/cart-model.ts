import type { CartItem } from "../../lib/types";

export function addCartItem(items: CartItem[], item: CartItem): CartItem[] {
  const existing = items.find((candidate) => candidate.variantId === item.variantId);
  if (!existing) return [...items, item];
  return items.map((candidate) =>
    candidate.variantId === item.variantId
      ? { ...candidate, quantity: Math.min(candidate.quantity + item.quantity, item.maxQuantity) }
      : candidate,
  );
}

export function changeCartQuantity(items: CartItem[], variantId: string, quantity: number): CartItem[] {
  return items.map((item) =>
    item.variantId === variantId
      ? { ...item, quantity: Math.max(1, Math.min(quantity, item.maxQuantity)) }
      : item,
  );
}

export function calculateCartSubtotal(items: CartItem[]): number {
  return items.reduce((sum, item) => sum + item.unitPrice * item.quantity, 0);
}
