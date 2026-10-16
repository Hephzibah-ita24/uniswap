requireLogin();

const sb = supabase.createClient(SUPABASE_URL, SUPABASE_KEY);
const errorBox = document.getElementById("error");

document.getElementById("logoutLink").addEventListener("click", (e) => {
  e.preventDefault();
  logout();
});

// Fill a dropdown from the backend
async function loadSelect(path, selectId, placeholder) {
  const select = document.getElementById(selectId);
  try {
    const items = await apiFetch(path);
    select.innerHTML = `<option value="">${placeholder}</option>`;
    items.forEach((item) => {
      const opt = document.createElement("option");
      opt.value = item.id;
      opt.textContent = item.name;
      select.appendChild(opt);
    });
  } catch (e) {
    select.innerHTML = '<option value="">Could not load</option>';
  }
}
loadSelect("/categories", "category", "Select a category");
loadSelect("/faculties", "faculty", "Select a faculty");

// Upload one file to Supabase Storage, return its public URL
async function uploadPhoto(file) {
  const safeName = file.name.replace(/[^a-zA-Z0-9._-]/g, "_");
  const path = `${Date.now()}-${safeName}`;
  const { error } = await sb.storage.from("item-photos").upload(path, file);
  if (error) throw new Error("Photo upload failed: " + error.message);
  const { data } = sb.storage.from("item-photos").getPublicUrl(path);
  return data.publicUrl;
}

document.getElementById("submitBtn").addEventListener("click", async () => {
  const btn = document.getElementById("submitBtn");
  const title = document.getElementById("title").value.trim();
  const description = document.getElementById("description").value.trim();
  const price = parseFloat(document.getElementById("price").value);
  const categoryId = document.getElementById("category").value;
  const facultyId = document.getElementById("faculty").value;
  const files = Array.from(document.getElementById("photos").files).slice(0, 5);
  const itemCondition = document.getElementById("condition").value;

  errorBox.textContent = "";

  if (!title) { errorBox.textContent = "Please enter a title."; return; }
  if (!(price > 0)) { errorBox.textContent = "Price must be greater than 0."; return; }
  if (!categoryId || !facultyId) { errorBox.textContent = "Please choose a category and a faculty."; return; }
  if (!itemCondition) { errorBox.textContent = "Please choose the item condition."; return; }

  btn.disabled = true;
  btn.textContent = "Posting...";

  try {
    const imageUrls = [];
    for (const file of files) {
      imageUrls.push(await uploadPhoto(file));
    }

    await apiFetch("/listings", {
      method: "POST",
      body: JSON.stringify({
        title,
        description,
        price,
        categoryId: Number(categoryId),
        facultyId: Number(facultyId),
        itemCondition,
        imageUrls
      })
    });

    window.location.href = "browse.html";
  } catch (e) {
    errorBox.textContent = e.message;
    btn.disabled = false;
    btn.textContent = "Post listing";
  }
});