const TOKEN_KEY = 'europolitik_token';
const USERNAME_KEY = 'europolitik_username';

function getToken() {
    return localStorage.getItem(TOKEN_KEY);
}

function getUsername() {
    return localStorage.getItem(USERNAME_KEY);
}

function setAuth(token, username) {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USERNAME_KEY, username);
}

function clearAuth() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USERNAME_KEY);
}

function authFetch(url, options = {}) {
    const token = getToken();
    const headers = Object.assign({}, options.headers || {}, token ? { 'Authorization': `Bearer ${token}` } : {});
    return fetch(url, Object.assign({}, options, { headers }));
}

function renderAuthNav() {
    const el = document.getElementById('nav-auth');
    if (!el) return;
    const username = getUsername();
    if (username) {
        el.innerHTML = `<a href="/account" style="color:#ede7da; opacity:0.75; text-decoration:none;">${username}</a> <a href="#" onclick="clearAuth(); window.location.href='/map'; return false;" style="color:#ede7da; opacity:0.5; text-decoration:none;">Log out</a>`;
    } else {
        el.innerHTML = `<a href="/login" style="color:#ede7da; opacity:0.75; text-decoration:none;">Log in</a>`;
    }
}

document.addEventListener('DOMContentLoaded', renderAuthNav);