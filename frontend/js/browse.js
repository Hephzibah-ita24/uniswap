document.addEventListener("DOMContentLoaded", async () => {
  await loadFaculties();
  await loadListings();

  // Filter listings when a user selects a different faculty
  const facultyFilter = document.getElementById("facultyFilter");
  if (facultyFilter) {
    facultyFilter.addEventListener("change", (e) => {
      loadListings(e.target.value);
    });
  }
});

// 1. Fetch faculties from backend to populate dropdown
async function loadFaculties() {
  const select = document.getElementById("facultyFilter");
  if (!select) return;

  try {
    const faculties = await apiFetch("/faculties");
    faculties.forEach(faculty => {
      const option = document.createElement("option");
      option.value = faculty.id;
      option.textContent = faculty.name;
      select.appendChild(option);
    });
  } catch (e) {
    console.error("Failed to load faculties:", e.message);
  }
}

// 2. Fetch listings and render them in the grid
async function loadListings(facultyId = "") {
  const grid = document.getElementById("listingsGrid");
  if (!grid) return;

  grid.innerHTML = "<p>Loading listings...</p>";

  try {
    let url = "/listings";
    if (facultyId) {
      url += `?faculty=${facultyId}`;
    }
    
    const listings = await apiFetch(url);
    
    if (!listings || listings.length === 0) {
      grid.innerHTML = "<p>No listings found.</p>";
      return;
    }

    grid.innerHTML = "";
    listings.forEach(listing => {
      const card = document.createElement("div");
      card.className = "card";
      card.innerHTML = `
        <div style="height: 140px; background: #ddd; border-radius: 6px; margin-bottom: 10px; display: flex; align-items: center; justify-content: center; color: #666;">
          ${listing.imageUrls && listing.imageUrls.length > 0 
            ? `<img src="${listing.imageUrls[0]}" style="width:100%; height:100%; object-fit:cover; border-radius:6px;">` 
            : 'No Image'}
        </div>
        <h3 style="font-size: 16px; margin-bottom: 6px;">${escapeHtml(listing.title)}</h3>
        <p style="color: #2d3a8c; font-weight: bold; margin-bottom: 8px;">$${listing.price.toFixed(2)}</p>
        <p style="font-size: 13px; color: #666; margin-bottom: 12px;">By ${escapeHtml(listing.sellerName || "Unknown")}</p>
        <a href="listing.html?id=${listing.id}" class="btn" style="text-align: center; text-decoration: none; display: block; padding: 8px; font-size: 14px;">View Details</a>
      `;
      grid.appendChild(card);
    });
  } catch (e) {
    grid.innerHTML = `<p class="error">Failed to load listings: ${e.message}</p>`;
  }
}

// Security helper to escape HTML output
function escapeHtml(str) {
  if (!str) return "";
  return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}