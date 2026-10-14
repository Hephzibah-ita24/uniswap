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

// Call at the top of any page that needs a logged-in user
function requireLogin() {
  if (!isLoggedIn()) {
    window.location.href = "login.html";
  }
}