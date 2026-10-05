"use strict";
const assert = require("assert");
const fs = require("fs");
const vm = require("vm");
const source = fs.readFileSync("src/main/resources/static/assets/shop.js", "utf8");
function browser(hash, saved, handler) {
    const elements = new Map();
    const storage = new Map(saved ? [["astavet-pending-checkout", JSON.stringify(saved)]] : []);
    const element = id => {
        if (!elements.has(id)) elements.set(id, {innerHTML:"",textContent:"",hidden:true,setAttribute(){},removeAttribute(){},querySelectorAll(){return [];}});
        return elements.get(id);
    };
    const calls = [];
    const context = vm.createContext({
        console, Intl, URLSearchParams, setTimeout(){}, location:{hash},
        document:{querySelector:element}, window:{addEventListener(){},scrollTo(){}},
        sessionStorage:{getItem:key => storage.get(key) || null,setItem:(key,value) => storage.set(key,value),removeItem:key => storage.delete(key)},
        crypto:{randomUUID:() => "11111111-1111-4111-8111-111111111111"},
        fetch:async (url, options = {}) => {
            calls.push({url,options});
            await new Promise(resolve => setImmediate(resolve));
            if (url === "/api/v1/csrf") return {ok:true,json:async () => ({token:"session-token",headerName:"X-CSRF-TOKEN"})};
            if (url === "/api/v1/products") return {ok:true,json:async () => []};
            if (url === "/api/v1/cart") return {ok:true,json:async () => ({lines:[]})};
            return handler(url, options);
        }
    });
    vm.runInContext(source,context);
    return {context,calls,elements,storage};
}
async function settle() { for (let i = 0; i < 10; i++) await new Promise(resolve => setImmediate(resolve)); }
async function firstVisitSharesOneCsrfSession() {
    const tab = browser("#product", null, () => { throw new Error("Unexpected request"); });
    await settle();
    assert.strictEqual(tab.calls.filter(call => call.url === "/api/v1/csrf").length, 1);
    assert.strictEqual(tab.calls.length, 3);
    for (const call of tab.calls.filter(call => call.url !== "/api/v1/csrf")) {
        assert.strictEqual(call.options.headers["X-CSRF-TOKEN"], "session-token");
    }
}
async function uncertainCheckoutReusesExactSavedSubmission() {
    const saved = {idempotencyKey:"11111111-1111-4111-8111-111111111111",quoteFingerprint:"a".repeat(64),name:"Alex",city:"Brisbane"};
    let attempts = 0;
    const tab = browser("#checkout", saved, (url, options) => {
        assert.strictEqual(url, "/api/v1/orders");
        assert.deepStrictEqual(JSON.parse(options.body), saved);
        if (++attempts === 1) throw new Error("Connection dropped after submission");
        return {ok:true,json:async () => ({id:"22222222-2222-4222-8222-222222222222"})};
    });
    await settle();
    assert.ok(tab.elements.get("#app").innerHTML.includes("Recover your checkout"));
    await tab.elements.get("#retry-checkout").onclick();
    assert.ok(tab.storage.has("astavet-pending-checkout"));
    await tab.elements.get("#retry-checkout").onclick();
    assert.strictEqual(attempts, 2);
    assert.strictEqual(tab.storage.has("astavet-pending-checkout"), false);
    assert.strictEqual(tab.context.location.hash, "confirmation/22222222-2222-4222-8222-222222222222");
}
(async () => {
    await firstVisitSharesOneCsrfSession();
    await uncertainCheckoutReusesExactSavedSubmission();
    console.log("2 storefront regressions passed");
})().catch(error => { console.error(error); process.exitCode = 1; });
