import type { Metadata } from "next";
import { Be_Vietnam_Pro, DM_Serif_Display } from "next/font/google";
import { CartProvider } from "@/features/cart/cart-context";
import { Footer } from "@/components/footer";
import { Header } from "@/components/header";
import "./globals.css";

const bodyFont = Be_Vietnam_Pro({ subsets: ["vietnamese", "latin"], variable: "--font-body", weight: ["400", "500", "600", "700"] });
const displayFont = DM_Serif_Display({ subsets: ["latin"], variable: "--font-display", weight: "400" });

export const metadata: Metadata = {
  title: { default: "AstaVet Việt Nam", template: "%s | AstaVet" },
  description: "Sản phẩm chăm sóc sức khỏe thú cưng AstaVet. Đặt hàng nhanh, thanh toán COD.",
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="vi">
      <body className={`${bodyFont.variable} ${displayFont.variable}`}>
        <CartProvider>
          <Header />
          <main>{children}</main>
          <Footer />
        </CartProvider>
      </body>
    </html>
  );
}

