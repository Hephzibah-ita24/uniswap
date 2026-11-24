requireLogin();

const grid = document.getElementById("listings");
const errorBox = document.getElementById("error");
const notice = document.getElementById("notice");
const editBox = document.getElementById("editBox");
const editError = document.getElementById("editError");
const saveBtn = document.getElementById("saveBtn");
let editing = null;
let busy = false;

function field(id) { return document.getElementById(id); }
function element(tag, text, className) {
  const el = document.createElement(tag);
  if (text != null) el.textContent = text;
  if (className) el.className = className;
  return el;
}
function button(text, action, className = "btn-light") {
  const b = element("button", text, className);
  b.type = "button";
  b.addEventListener("click", action);
  return b;
}
function setBusy(value) {
  busy = value;
  grid.querySelectorAll("button").forEach(b => b.disabled = value);
  saveBtn.disabled = value;
  field("cancelBtn").disabled = value;
}
function payload(l, status = l.status) {
  return { title: l.title, description: l.description || "", price: Number(l.price),
    categoryId: l.categoryId ?? null, itemCondition: l.itemCondition, status };
}
async function loadListings() {
  errorBox.textContent = "";
  field("count").textContent = "Loading listings...";
  grid.replaceChildren();
  try {
    const listings = await apiFetch("/listings/mine");
    if (!Array.isArray(listings)) throw new Error("Could not retrieve listings.");
    field("count").textContent = `${listings.length} listing(s)`;
    if (!listings.length) grid.appendChild(element("p", "You have no listings yet."));
    listings.forEach(renderListing);
  } catch (e) {
    field("count").textContent = "";
    errorBox.textContent = e.message;
  }
}
function renderListing(l) {
  const card = element("article", null, "card");
  const url = l.primaryImageUrl || listingImages(l)[0];
  const placeholder = () => element("div", "No photo", "noimg");
  if (url && /^https?:\/\//i.test(url)) {
    const img = element("img", null, "thumb");
    img.alt = l.title;
    img.addEventListener("error", () => img.replaceWith(placeholder()));
    img.src = url;
    card.appendChild(img);
  } else card.appendChild(placeholder());
  card.append(element("h3", l.title), element("p", formatPrice(l.price), "price"),
    element("p", `${conditionLabel(l.itemCondition)} · ${l.categoryName || "Uncategorized"}`, "status"),
    element("p", `Status: ${l.status === "SOLD" ? "Sold" : l.status === "AVAILABLE" ? "Available" : l.status || "Unknown"}`, "status"));
  const actions = element("div", null, "actions");
  const view = element("a", "View", "btn-light");
  view.href = `listing.html?id=${encodeURIComponent(l.id)}`;
  actions.append(view, button("Edit", () => openEdit(l)),
    button(l.status === "SOLD" ? "Relist" : "Mark as sold", () => changeStatus(l)),
    button("Delete", () => deleteListing(l), "btn-light delete"));
  card.appendChild(actions);
  grid.appendChild(card);
}
async function openEdit(l) {
  if (busy) return;
  setBusy(true);
  errorBox.textContent = "";
  editError.textContent = "";
  try {
    const categories = await apiFetch("/categories");
    const select = field("category");
    select.replaceChildren();
    if (l.categoryId == null) select.add(new Option("Uncategorized", ""));
    categories.forEach(c => select.add(new Option(c.name, c.id)));
    if (l.categoryId != null && !categories.some(c => String(c.id) === String(l.categoryId))) {
      select.add(new Option(l.categoryName || "Current category", l.categoryId));
    }
    select.value = l.categoryId ?? "";
    editing = l;
    field("title").value = l.title;
    field("description").value = l.description || "";
    field("price").value = l.price;
    field("condition").value = l.itemCondition;
    editBox.hidden = false;
    editBox.scrollIntoView({behavior: "smooth"});
    field("title").focus();
  } catch (e) { errorBox.textContent = e.message; }
  finally { setBusy(false); }
}
function closeEdit() { editing = null; editBox.hidden = true; editError.textContent = ""; }
field("cancelBtn").addEventListener("click", closeEdit);
field("editForm").addEventListener("submit", async e => {
  e.preventDefault();
  if (!editing || busy) return;
  const title = field("title").value.trim();
  if (!title) { editError.textContent = "Please enter a title."; return; }
  const data = { ...payload(editing), title, description: field("description").value,
    price: Number(field("price").value), categoryId: field("category").value ? Number(field("category").value) : null,
    itemCondition: field("condition").value };
  setBusy(true);
  editError.textContent = "";
  notice.textContent = "";
  try {
    await apiFetch(`/listings/${editing.id}`, {method:"PUT", body:JSON.stringify(data)});
    closeEdit();
    notice.textContent = "Listing updated.";
    await loadListings();
  } catch (e) { editError.textContent = e.message; }
  finally { setBusy(false); }
});
async function changeStatus(l) {
  if (busy) return;
  const status = l.status === "SOLD" ? "AVAILABLE" : "SOLD";
  if (!confirm(status === "SOLD" ? "Mark this item as sold?" : "Make this item available again?")) return;
  await mutate(l, {method:"PUT", body:JSON.stringify(payload(l, status))}, status === "SOLD" ? "Listing marked as sold." : "Listing is available again.");
}
async function deleteListing(l) {
  if (busy || !confirm(`Delete "${l.title}" permanently?`)) return;
  await mutate(l, {method:"DELETE"}, "Listing deleted.");
}
async function mutate(l, options, message) {
  setBusy(true);
  errorBox.textContent = "";
  notice.textContent = "";
  try {
    await apiFetch(`/listings/${l.id}`, options);
    closeEdit();
    notice.textContent = message;
    await loadListings();
  } catch (e) { errorBox.textContent = e.message; }
  finally { setBusy(false); }
}
if (isLoggedIn()) loadListings();
