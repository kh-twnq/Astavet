"use strict";
async function prepareLogin() {
    const message = document.querySelector("#login-message");
    if (new URLSearchParams(location.search).has("error")) {
        message.className = "error-message";
        message.textContent = "The username or password was incorrect.";
    }
    try {
        const response = await fetch("/api/v1/csrf", {credentials:"same-origin"});
        if (!response.ok) throw new Error("Sign in is temporarily unavailable. Please refresh.");
        const csrf = await response.json();
        const input = document.querySelector("#csrf-input");
        input.name = "_csrf"; input.value = csrf.token;
        document.querySelector("#sign-in").disabled = false;
    } catch (error) { message.className = "error-message"; message.textContent = error.message; }
}
prepareLogin();
