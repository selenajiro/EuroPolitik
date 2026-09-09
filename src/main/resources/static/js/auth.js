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
        el.innerHTML = `<a href="/account" style="color:#ede7da; opacity:0.75; text-decoration:none;">Account</a> <a href="#" onclick="clearAuth(); window.location.href='/map'; return false;" style="color:#ede7da; opacity:0.5; text-decoration:none;">Log out</a>`;
    } else {
        el.innerHTML = `<a href="/login" style="color:#ede7da; opacity:0.75; text-decoration:none;">Log in</a>`;
    }
}

function highlightActiveNav() {
    const path = window.location.pathname;
    const isMapActive = path === '/map' || path === '/';

    document.querySelectorAll('nav > a').forEach(link => {
        const href = link.getAttribute('href');
        const active = (href === '/map' && isMapActive) || (href !== '/map' && href === path);
        link.style.color = active ? '#b8945f' : '#ede7da';
        link.style.opacity = active ? '1' : '0.75';
    });
}

function showFlash(message) {
    const flash = document.createElement('div');
    flash.textContent = message;
    flash.style.cssText = 'position: fixed; bottom: 24px; right: 24px; background: #b8945f; color: #121b24; padding: 10px 18px; border-radius: 4px; font-size: 13px; font-weight: 500; z-index: 3000; box-shadow: 0 4px 20px rgba(0,0,0,0.35); transition: opacity 0.3s;';
    document.body.appendChild(flash);
    setTimeout(() => { flash.style.opacity = '0'; }, 1400);
    setTimeout(() => flash.remove(), 1800);
}

document.addEventListener('DOMContentLoaded', () => {
    renderAuthNav();
    highlightActiveNav();
});