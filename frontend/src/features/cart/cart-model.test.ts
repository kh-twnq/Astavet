import { describe, expect, it } from "vitest";
import { addCartItem, calculateCartSubtotal, changeCartQuantity } from "./cart-model";
import type { CartItem } from "../../lib/types";

const item: CartItem = {
  variantId: "variant-1",
  productSlug: "astavet-130g",
  productName: "AstaVet 130g",
  variantName: "Hộp 130g",
  imageUrl: "/product.svg",
  unitPrice: 649_000,
  quantity: 1,
  maxQuantity: 3,
};

describe("cart model", () => {
  it("merges duplicate variants without exceeding available stock", () => {
    const result = addCartItem([item], { ...item, quantity: 5 });
    expect(result).toHaveLength(1);
    expect(result[0].quantity).toBe(3);
  });

  it("keeps quantity within one and current stock", () => {
    expect(changeCartQuantity([item], item.variantId, 0)[0].quantity).toBe(1);
    expect(changeCartQuantity([item], item.variantId, 10)[0].quantity).toBe(3);
  });

  it("calculates subtotal from unit prices and quantities", () => {
    expect(calculateCartSubtotal([{ ...item, quantity: 2 }])).toBe(1_298_000);
  });
});
