(function () {
  const links = [
    ["browse.html", "Browse"],
    ["create-listing.html", "Sell"],
    ["my-listings.html", "My Listings"],
    ["inbox.html", "Chats"]
  ];
  const current = location.pathname.split("/").pop() || "index.html";
  const items = links
    .map(([href, label]) => `<a href="${href}" class="${current === href ? "active" : ""}">${label}</a>`)
    .join("");

  const html = `
    <div class="navbar">
      <a class="logo" href="browse.html">Uniswap</a>
      <div>${items}<a href="#" id="logoutLink">Logout</a></div>
    </div>`;

  const host = document.getElementById("navbar");
  if (host) {
    host.innerHTML = html;
    document.getElementById("logoutLink").addEventListener("click", (e) => {
      e.preventDefault();
      logout();
    });
  }
})();