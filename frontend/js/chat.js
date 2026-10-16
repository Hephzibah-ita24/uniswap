requireLogin();

const params = new URLSearchParams(window.location.search);
const conversationId = params.get("id");
const messagesBox = document.getElementById("messages");
const errorBox = document.getElementById("error");
let lastCount = 0;

document.getElementById("logoutLink").addEventListener("click", (e) => {
  e.preventDefault();
  logout();
});

if (!conversationId) {
  errorBox.textContent = "No conversation selected. Open a chat from a listing or your inbox.";
}

// Read the user id from the JWT so we can tell which messages are mine
function getMyId() {
  try {
    const payload = JSON.parse(atob(getToken().split(".")[1]));
    return payload.userId || payload.id || payload.sub;
  } catch (e) {
    return null;
  }
}
const myId = String(getMyId());

function renderMessages(messages) {
  messagesBox.innerHTML = "";
  messages.forEach((m) => {
    const div = document.createElement("div");
    const senderId = String(m.senderId ?? (m.sender && m.sender.id));
    div.className = "msg" + (senderId === myId ? " mine" : "");
    div.textContent = m.content;
    messagesBox.appendChild(div);
  });
  if (messages.length !== lastCount) {
    messagesBox.scrollTop = messagesBox.scrollHeight;
    lastCount = messages.length;
  }
}

async function loadMessages() {
  if (!conversationId) return;
  try {
    const messages = await apiFetch(`/conversations/${conversationId}/messages`);
    renderMessages(messages);
    errorBox.textContent = "";
  } catch (e) {
    errorBox.textContent = e.message;
  }
}

async function sendMessage() {
  const input = document.getElementById("messageInput");
  const content = input.value.trim();
  if (!content || !conversationId) return;
  try {
    await apiFetch(`/conversations/${conversationId}/messages`, {
      method: "POST",
      body: JSON.stringify({ content })
    });
    input.value = "";
    loadMessages();
  } catch (e) {
    errorBox.textContent = e.message;
  }
}

document.getElementById("sendBtn").addEventListener("click", sendMessage);
document.getElementById("messageInput").addEventListener("keydown", (e) => {
  if (e.key === "Enter") sendMessage();
});

loadMessages();
setInterval(loadMessages, 4000); // poll every 4 seconds