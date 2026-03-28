const BASE = "/api/v1";

function getToken() {
  return localStorage.getItem("token");
}

function headers(withBody = false) {
  const h = { Authorization: `Bearer ${getToken()}` };
  if (withBody) h["Content-Type"] = "application/json";
  return h;
}

async function request(method, path, body) {
  const res = await fetch(`${BASE}${path}`, {
    method,
    headers: headers(!!body),
    body: body ? JSON.stringify(body) : undefined,
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({ error: "Unknown error" }));
    throw new Error(err.error || `HTTP ${res.status}`);
  }
  if (res.status === 204) return null;
  return res.json();
}

// ── Auth ───────────────────────────────────────────────────────────────────
export const api = {
  auth: {
    register: (data) => request("POST", "/auth/register", data),
    login: (data) => request("POST", "/auth/login", data),
    me: () => request("GET", "/auth/me"),
  },

  // ── Characters ─────────────────────────────────────────────────────────
  characters: {
    create: (data) => request("POST", "/characters", data),
    list: () => request("GET", "/characters"),
    get: (id) => request("GET", `/characters/${id}`),
    update: (id, d) => request("PATCH", `/characters/${id}`, d),
    updateHp: (id, d) => request("PATCH", `/characters/${id}/hp`, d),
    delete: (id) => request("DELETE", `/characters/${id}`),
  },

  // ── Campaigns ──────────────────────────────────────────────────────────
  campaigns: {
    create: (data) => request("POST", "/campaigns", data),
    list: () => request("GET", "/campaigns"),
    get: (id) => request("GET", `/campaigns/${id}`),
    archive: (id) => request("POST", `/campaigns/${id}/archive`),
    addMember: (id, data) => request("POST", `/campaigns/${id}/members`, data),
    getMembers: (id) => request("GET", `/campaigns/${id}/members`),
    removeMember: (id, userId) =>
      request("DELETE", `/campaigns/${id}/members/${userId}`),
    getSessions: (id) => request("GET", `/campaigns/${id}/sessions`),
    createSession: (id, data) =>
      request("POST", `/campaigns/${id}/sessions`, data),
    updateStatus: (cid, sid, d) =>
      request("PATCH", `/campaigns/${cid}/sessions/${sid}/status`, d),
    updateNotes: (cid, sid, d) =>
      request("PATCH", `/campaigns/${cid}/sessions/${sid}/notes`, d),
  },

  // ── Dice ───────────────────────────────────────────────────────────────
  dice: {
    roll: (data) => request("POST", "/dice/roll", data),
  },
};
