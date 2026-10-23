function saveToken(token) {
  localStorage.setItem("token", token);
}

function getToken() {
  return localStorage.getItem("token");
}

function isLoggedIn() {
  return !!getToken();
}

function logout() {
  localStorage.removeItem("token");
  window.location.href = "login.html";
}

function requireLogin() {
  if (!isLoggedIn()) {
    window.location.href = "login.html";
  }
}

// Id of the logged-in user, read from the JWT
function currentUserId() {
  try {
    const payload = JSON.parse(atob(getToken().split(".")[1]));
    return String(payload.userId || payload.id || payload.sub);
  } catch (e) {
    return null;
  }
}

function escapeHtml(str) {
  if (str === null || str === undefined) return "";
  return String(str).replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}

function formatPrice(p) {
  const n = Number(p);
  if (isNaN(n)) return escapeHtml(p);
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: typeof CURRENCY !== "undefined" ? CURRENCY : "USD",
    maximumFractionDigits: 0
  }).format(n);
}

function conditionLabel(c) {
  return { NEW: "New", GOOD: "Good", USED: "Used" }[c] || "";
}

// Returns a list of image URL strings, whatever shape the backend sends
function listingImages(l) {
  const raw = l.imageUrls || l.images || [];
  return raw
    .map((x) => (typeof x === "string" ? x : x && (x.imageUrl || x.url)))
    .filter(Boolean);
}