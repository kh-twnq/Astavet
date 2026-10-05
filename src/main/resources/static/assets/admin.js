"use strict";
const escapeHtml = value => String(value).replace(/[&<>"']/g, char => ({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[char]));
const money = (value, currency) => new Intl.NumberFormat("en-AU", {style:"currency",currency}).format(value);
let csrf;
let page = 0;
let loading = false;
let orders = [];
const transitions = {PLACED:["CONFIRMED","CANCELLED"],CONFIRMED:["SHIPPED","CANCELLED"],SHIPPED:["DELIVERED"],DELIVERED:[],CANCELLED:[]};
const labels = {CONFIRMED:"Confirm order",SHIPPED:"Mark shipped",DELIVERED:"Mark delivered",CANCELLED:"Cancel & restore stock"};
async function request(path, options = {}) {
    if (!csrf) csrf = await fetch("/api/v1/csrf").then(response => response.json());
    const response = await fetch(path, {...options, credentials:"same-origin", headers:{"Content-Type":"application/json",[csrf.headerName]:csrf.token}});
    if (response.status === 401) { location.href = "/login.html"; throw new Error("Please sign in again."); }
    if (!response.ok) {
        let message = "Unable to process the request. Refresh and try again.";
        try { message = (await response.json()).message || message; } catch (error) { message = response.status === 403 ? "Your session expired. Sign in again." : message; }
        throw new Error(message);
    }
    return response.json();
}
function draw() {
    document.querySelector("#orders").innerHTML = orders.length ? orders.map(order => {
        const c = order.customer;
        return `<article class="admin-order"><div class="order-top"><div><h3>Order <span class="order-id">${escapeHtml(order.id)}</span></h3><small>${new Date(order.createdAt).toLocaleString("en-AU")} · COD · ${money(order.total, order.currency)}</small></div><span class="status ${order.status === "CANCELLED" ? "cancelled" : ""}">${escapeHtml(order.status)}</span></div><div class="order-details"><div><h3>Customer &amp; delivery</h3><p>${escapeHtml(c.name)}<br>${escapeHtml(c.email)} · ${escapeHtml(c.phone)}<br>${escapeHtml(c.address)}<br>${escapeHtml(c.city)}, ${escapeHtml(c.state)} ${escapeHtml(c.postcode)}</p></div><div><h3>Items</h3>${order.lines.map(line => `<p>${escapeHtml(line.name)} × ${line.quantity} · ${money(line.total, order.currency)}</p>`).join("")}<p>Shipping ${money(order.shipping, order.currency)}<br><strong>Collect ${money(order.total, order.currency)} in cash on delivery</strong></p></div></div><div class="admin-actions">${transitions[order.status].map(status => `<button class="${status === "CANCELLED" ? "secondary" : "primary"}" data-id="${order.id}" data-status="${status}">${labels[status]}</button>`).join("")}</div></article>`;
    }).join("") : '<div class="empty"><h2>No orders on this page</h2><p>New customer orders will appear here.</p></div>';
    document.querySelector("#page-label").textContent = `Page ${page + 1}`;
    document.querySelector("#previous").disabled = page === 0;
    document.querySelector("#next").disabled = orders.length < 25;
    document.querySelectorAll("button[data-id]").forEach(button => {
        button.onclick = async () => {
            const order = orders.find(item => item.id === button.dataset.id);
            if (button.dataset.status === "CANCELLED" && !window.confirm("Cancel this order and restore its stock?")) return;
            document.querySelectorAll("button[data-id]").forEach(action => { action.disabled = true; });
            try {
                await request(`/api/v1/admin/orders/${order.id}/status`, {method:"PUT",body:JSON.stringify({expectedStatus:order.status,status:button.dataset.status})});
                await load();
            } catch (error) { document.querySelector("#admin-notice").textContent = error.message; draw(); }
        };
    });
}
async function load() {
    if (loading) return;
    loading = true;
    try { orders = await request(`/api/v1/admin/orders?page=${page}`); draw(); document.querySelector("#admin-notice").textContent = ""; }
    catch (error) { document.querySelector("#admin-notice").textContent = error.message; }
    finally { loading = false; }
}
document.querySelector("#refresh").onclick = load;
document.querySelector("#previous").onclick = () => { if (!loading && page > 0) { page--; load(); } };
document.querySelector("#next").onclick = () => { if (!loading) { page++; load(); } };
document.querySelector("#logout").onclick = async () => {
    if (!csrf) csrf = await fetch("/api/v1/csrf").then(response => response.json());
    const response = await fetch("/logout", {method:"POST",headers:{[csrf.headerName]:csrf.token}});
    if (response.ok) location.href = "/";
    else document.querySelector("#admin-notice").textContent = "Sign out failed. Refresh and try again.";
};
load();
