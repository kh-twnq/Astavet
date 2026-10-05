"use strict";
const app = document.querySelector("#app");
const escapeHtml = value => String(value).replace(/[&<>"']/g, char => ({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[char]));
const money = (amount, currency = "AUD") => new Intl.NumberFormat("en-AU", {style:"currency", currency}).format(amount);
let csrfPromise;
let cart;
let catalog = [];
let routeVersion = 0;
let submitting = false;
const pendingKey = "astavet-pending-checkout";
async function api(path, options = {}) {
    if (!csrfPromise) csrfPromise = fetch("/api/v1/csrf", {credentials:"same-origin"}).then(response => {
        if (!response.ok) throw new Error("The shop could not be reached. Please refresh.");
        return response.json();
    }).catch(error => { csrfPromise = null; throw error; });
    const csrf = await csrfPromise;
    const response = await fetch(`/api/v1${path}`, {
        credentials:"same-origin", ...options,
        headers:{"Content-Type":"application/json", [csrf.headerName]:csrf.token, ...options.headers}
    });
    if (!response.ok) {
        let message = "The shop could not complete the request. Please try again.";
        try { const error = await response.json(); message = error.message || message; } catch (error) { message = response.status === 403 ? "Your session expired. Please refresh and try again." : message; }
        const error = new Error(message); error.status = response.status; throw error;
    }
    return response.json();
}
function notify(message) {
    const notice = document.querySelector("#notice");
    notice.textContent = message; notice.hidden = false;
    setTimeout(() => { notice.hidden = true; }, 4500);
}
function updateCount() {
    document.querySelector("#cart-count").textContent = cart ? cart.lines.reduce((sum, line) => sum + line.quantity, 0) : 0;
}
function pending() {
    try { return JSON.parse(sessionStorage.getItem(pendingKey) || "null"); } catch (error) { return null; }
}
function steps(active) {
    return `<nav class="steps" aria-label="Checkout steps"><a href="#product">Product</a><span>›</span><a class="${active === "cart" ? "current" : ""}" href="#cart">Bag</a><span>›</span><span class="${active === "checkout" ? "current" : ""}">Checkout</span><span>›</span><span class="${active === "confirmation" ? "current" : ""}">Confirmation</span></nav>`;
}
function totals(value, action = "", paymentNote = "Pay the full total in cash when your order arrives. Australian delivery only.") {
    return `<aside class="summary-box"><h2>Order summary</h2>${value.lines.map(line => `<div><span>${escapeHtml(line.name)} × ${line.quantity}</span><strong> ${money(line.total, value.currency)}</strong></div>`).join("")}<dl><div><dt>Subtotal</dt><dd>${money(value.subtotal, value.currency)}</dd></div><div><dt>Shipping</dt><dd>${Number(value.shipping) === 0 ? "Free" : money(value.shipping, value.currency)}</dd></div><div class="total"><dt>Total · ${escapeHtml(value.currency)}</dt><dd>${money(value.total, value.currency)}</dd></div></dl>${action}<p class="shipping-note">${escapeHtml(paymentNote)}</p></aside>`;
}
function productPage() {
    const slug = location.hash.startsWith("#product/") ? decodeURIComponent(location.hash.slice(9)) : null;
    const product = slug ? catalog.find(item => item.slug === slug) : catalog[0];
    if (!product) { app.innerHTML = `<div class="empty"><h1>Product unavailable</h1><a class="primary" href="#product">Back to shop</a></div>`; return; }
    const isInitial = product.slug === "astaxanthin-200g";
    const productImage = isInitial ? `<img src="/assets/product.svg" alt="Illustration of an AstaVet 130g supplement jar">` : `<div class="empty"><h2>${escapeHtml(product.name)}</h2></div>`;
    app.innerHTML = `<p class="breadcrumb"><a href="#product">Shop</a><span>/</span>${escapeHtml(product.name)}</p>${catalog.length > 1 ? `<nav class="collection" aria-label="Products">${catalog.map(item => `<a href="#product/${encodeURIComponent(item.slug)}">${escapeHtml(item.name)}</a>`).join("")}</nav>` : ""}<section class="product-layout"><div class="product-art"><span class="art-note">DAILY CANINE CARE</span>${productImage}<div class="art-caption">CARE THAT BECOMES A HABIT</div></div><div class="product-copy"><p class="eyebrow">A LITTLE CARE. EVERY DAY.</p><h1>${escapeHtml(product.name)}</h1><p class="lead">${escapeHtml(product.description)}</p><p class="price">${money(product.price, product.currency)} <small>${escapeHtml(product.currency)}</small></p><p class="stock">${product.stock > 0 ? "Available · Ready for your best friend" : "Currently out of stock"}</p>${isInitial ? '<div class="benefits"><span>Natural astaxanthin</span><span>Beta-glucan</span><span>MOS prebiotics</span></div>' : ""}<form id="add-form" class="buy-row"><div class="quantity"><button type="button" id="minus" aria-label="Decrease quantity">−</button><input id="product-quantity" aria-label="Quantity" type="number" value="1" min="1" max="${Math.min(99, Math.max(1, product.stock))}" required><button type="button" id="plus" aria-label="Increase quantity">+</button></div><button class="primary" type="submit" ${product.stock === 0 ? "disabled" : ""}>${product.stock === 0 ? "Out of stock" : "Add to bag"}</button></form><p class="shipping-note">$7.95 delivery · Free on orders of $100 or more</p><div class="confidence"><span>◌ Cash on delivery</span><span>◌ Australian delivery</span></div></div></section><section id="details" class="details"><div><p class="eyebrow">GOOD CARE STARTS HERE</p><h2>A simple addition<br>to their daily routine.</h2></div><div><details open><summary>About this product</summary><p>${escapeHtml(product.description)}</p></details>${isInitial ? '<details><summary>Daily feeding guide</summary><p>Mix with your dog’s food. One teaspoon is approximately 3.653g. Check suitability and dosage with your veterinarian.</p><table class="dosage"><thead><tr><th>Dog weight</th><th>Daily amount</th></tr></thead><tbody><tr><td>0–10kg</td><td>¼ teaspoon</td></tr><tr><td>11–20kg</td><td>½ teaspoon</td></tr><tr><td>21–32kg</td><td>¾ teaspoon</td></tr><tr><td>Over 32kg</td><td>1 teaspoon</td></tr></tbody></table></details>' : ""}<details><summary>Storage &amp; precautions</summary><p>Store sealed in a cool, dry place. Do not use if the seal is broken. Safety during pregnancy or breeding has not been established. Stop use and consult a veterinarian if your animal’s condition worsens. Keep out of reach of children and animals.</p></details><details><summary>Delivery &amp; payment</summary><p>We deliver within Australia. Pay in cash on delivery. Shipping is $7.95, or free when your subtotal reaches $100. You’ll see the full total before placing your order.</p></details></div></section>`;
    const quantity = document.querySelector("#product-quantity");
    document.querySelector("#minus").onclick = () => { quantity.value = Math.max(1, Number(quantity.value) - 1); };
    document.querySelector("#plus").onclick = () => { quantity.value = Math.min(Number(quantity.max), Number(quantity.value) + 1); };
    document.querySelector("#add-form").onsubmit = async event => {
        event.preventDefault();
        const button = event.currentTarget.querySelector('[type="submit"]'); button.disabled = true;
        try {
            cart = await api("/cart");
            const current = cart.lines.find(line => line.productId === product.id)?.quantity || 0;
            cart = await api("/cart/lines", {method:"PUT", body:JSON.stringify({productId:product.id, quantity:current + Number(quantity.value)})});
            updateCount(); notify("Added to your bag."); location.hash = "cart";
        } catch (error) { notify(error.message); } finally { button.disabled = false; }
    };
    if (location.hash === "#details") document.querySelector("#details").scrollIntoView();
}
function cartPage() {
    if (!cart.lines.length) { app.innerHTML = `${steps("cart")}<section class="empty"><h1>Your bag is empty</h1><p class="muted">A little daily care is waiting.</p><a class="primary" href="#product">Explore the shop</a>${pending() ? '<p><a href="#checkout">Recover your pending checkout</a></p>' : ""}</section>`; return; }
    app.innerHTML = `${steps("cart")}<div class="page-heading"><h1>Your bag</h1><a class="text-button" href="#product">Continue shopping</a></div><div class="checkout-layout"><section aria-label="Bag items">${cart.lines.map(line => `<article class="bag-item"><img src="/assets/product.svg" alt="Supplement jar illustration"><div><h3>${escapeHtml(line.name)}</h3><small>${money(line.unitPrice, cart.currency)} each</small><div class="quantity"><button data-id="${line.productId}" data-quantity="${line.quantity - 1}" aria-label="Decrease ${escapeHtml(line.name)} quantity">−</button><input aria-label="${escapeHtml(line.name)} quantity" type="number" min="0" max="99" value="${line.quantity}" data-id="${line.productId}"><button data-id="${line.productId}" data-quantity="${line.quantity + 1}" aria-label="Increase ${escapeHtml(line.name)} quantity">+</button></div><button class="text-button" data-id="${line.productId}" data-quantity="0">Remove</button></div><strong>${money(line.total, cart.currency)}</strong></article>`).join("")}<p class="shipping-note">Availability and pricing are checked again when you place your order.</p></section>${totals(cart, '<a class="primary" href="#checkout">Continue to checkout →</a>')}</div>`;
    app.querySelectorAll("button[data-id]").forEach(button => { button.onclick = () => changeQuantity(button.dataset.id, Number(button.dataset.quantity)); });
    app.querySelectorAll("input[data-id]").forEach(input => { input.onchange = () => changeQuantity(input.dataset.id, Number(input.value)); });
}
async function changeQuantity(productId, quantity) {
    if (!Number.isInteger(quantity) || quantity < 0 || quantity > 99) { notify("Choose a quantity between 0 and 99."); return; }
    app.querySelectorAll("button,input").forEach(element => { element.disabled = true; });
    try { cart = await api("/cart/lines", {method:"PUT", body:JSON.stringify({productId, quantity})}); updateCount(); cartPage(); }
    catch (error) { notify(error.message); await render(); }
}
function checkoutPage() {
    const recovery = pending();
    if (recovery) {
        app.innerHTML = `${steps("checkout")}<section class="confirmation"><h1>Recover your checkout</h1><p>Your previous submission has not been confirmed on this device. Retry the same submission to safely retrieve its result.</p><p>Delivery to ${escapeHtml(recovery.name)}, ${escapeHtml(recovery.city)}.</p><button id="retry-checkout" class="primary">Retry saved checkout</button><p id="checkout-error" role="alert"></p></section>`;
        document.querySelector("#retry-checkout").onclick = () => submitOrder(recovery);
        return;
    }
    if (!cart.lines.length) { cartPage(); return; }
    app.innerHTML = `${steps("checkout")}<div class="page-heading"><h1>Checkout</h1><a class="text-button" href="#cart">Edit your bag</a></div><form id="checkout-form"><div class="checkout-layout"><section class="form-section"><h2>Contact &amp; delivery</h2><div class="form-grid"><label class="full">Full name<input name="name" autocomplete="name" maxlength="100" required></label><label>Email<input name="email" type="email" autocomplete="email" maxlength="254" required></label><label>Phone<input name="phone" type="tel" autocomplete="tel" pattern="[+0-9() .\-]{6,30}" maxlength="30" required></label><label class="full">Street address<input name="address" autocomplete="street-address" maxlength="300" required></label><label>Suburb / city<input name="city" autocomplete="address-level2" maxlength="100" required></label><label>State / territory<select name="state" autocomplete="address-level1" required><option value="">Choose a state</option>${["NSW","VIC","QLD","WA","SA","TAS","ACT","NT"].map(state => `<option>${state}</option>`).join("")}</select></label><label>Postcode<input name="postcode" autocomplete="postal-code" inputmode="numeric" pattern="[0-9]{4}" maxlength="4" required></label><label>Country<input value="Australia" disabled></label></div><div class="payment-box"><h3>◉ Cash on delivery</h3><p>No payment is collected now. Have the exact cash total ready when your parcel arrives.</p></div><p id="checkout-error" role="alert"></p></section>${totals(cart, '<button class="primary" id="place-order" type="submit">Place order · Pay on delivery</button>')}</div></form>`;
    document.querySelector("#checkout-form").onsubmit = event => {
        event.preventDefault();
        const body = Object.fromEntries(new FormData(event.currentTarget));
        body.idempotencyKey = crypto.randomUUID(); body.quoteFingerprint = cart.fingerprint;
        try { sessionStorage.setItem(pendingKey, JSON.stringify(body)); } catch (error) { notify("Enable session storage in your browser before placing an order."); return; }
        submitOrder(body);
    };
}
async function submitOrder(body) {
    if (submitting) return;
    submitting = true;
    app.querySelectorAll("input,select,button").forEach(element => { element.disabled = true; });
    try {
        const order = await api("/orders", {method:"POST", body:JSON.stringify(body)});
        sessionStorage.removeItem(pendingKey);
        cart = {lines:[]}; updateCount(); location.hash = `confirmation/${order.id}`;
    } catch (error) {
        if (error.status && error.status < 500 && error.status !== 429 && error.status !== 403) {
            sessionStorage.removeItem(pendingKey);
            await render(); notify(error.message);
        } else {
            checkoutPage();
            const target = document.querySelector("#checkout-error");
            if (target) { target.className = "error-message"; target.textContent = error.message; }
        }
    } finally { submitting = false; }
}
function confirmationPage(order) {
    const customer = order.customer;
    const cancelled = order.status === "CANCELLED";
    const delivered = order.status === "DELIVERED";
    const heading = cancelled ? "Your order was cancelled." : delivered ? "Delivered. Thank you." : "A little care is on its way.";
    const paymentNote = cancelled ? "This order was cancelled. No payment is due." : delivered ? "This order has been marked as delivered." : "Pay the full total in cash when your order arrives. Australian delivery only.";
    app.innerHTML = `${steps("confirmation")}<section class="confirmation"><div class="success-mark" aria-hidden="true">✓</div><p class="eyebrow">THANK YOU FOR YOUR ORDER</p><h1>${heading}</h1><p>We’ve received your order, ${escapeHtml(customer.name)}. Your order status is <strong>${escapeHtml(order.status.toLowerCase())}</strong>.</p><p class="order-id">Order ${escapeHtml(order.id)}</p>${totals(order, "", paymentNote)}<div class="order-details"><div><h3>Delivery address</h3><p>${escapeHtml(customer.name)}<br>${escapeHtml(customer.address)}<br>${escapeHtml(customer.city)}, ${escapeHtml(customer.state)} ${escapeHtml(customer.postcode)}<br>Australia</p></div><div><h3>Cash on delivery</h3><p>${cancelled ? "No payment is due." : delivered ? "Order delivered." : money(order.total, order.currency) + " payable on arrival."}<br>Contact: ${escapeHtml(customer.email)}<br>${escapeHtml(customer.phone)}</p></div></div><a class="secondary" href="#product">Back to the shop</a></section>`;
}
async function render() {
    const version = ++routeVersion;
    app.setAttribute("aria-busy", "true");
    try {
        const [products, bag] = await Promise.all([api("/products"), api("/cart")]);
        if (version !== routeVersion) return;
        catalog = products; cart = bag; updateCount();
        if (location.hash.startsWith("#confirmation/")) {
            const order = await api(`/orders/${encodeURIComponent(location.hash.slice(14))}`);
            if (version !== routeVersion) return;
            confirmationPage(order);
        } else if (location.hash === "#cart") cartPage();
        else if (location.hash === "#checkout") checkoutPage();
        else productPage();
    } catch (error) {
        if (version !== routeVersion) return;
        app.innerHTML = `<section class="empty"><h1>We couldn’t open this page</h1><p class="error-message">${escapeHtml(error.message)}</p><button id="retry-page" class="primary">Try again</button><p><a href="#product">Back to shop</a></p></section>`;
        document.querySelector("#retry-page").onclick = render;
    } finally { if (version === routeVersion) app.removeAttribute("aria-busy"); }
}
window.addEventListener("hashchange", () => { window.scrollTo(0, 0); render(); });
render();
