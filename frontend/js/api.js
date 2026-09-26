/**
 * ResourceHub Central API & Utility Library
 * Manages JWT tokens, authenticated requests, session states, UI toasts,
 * and dynamic Vercel / Cloud Backend URL resolution.
 */

// Dynamic API Base URL Detection for Local Development & Vercel Production
function resolveApiBase() {
  if (typeof window !== "undefined") {
    // 1. In-browser manual override stored in localStorage
    const saved = localStorage.getItem("resourcehub_api_base") || localStorage.getItem("toolshare_api_base");
    if (saved && saved.trim()) {
      return saved.trim().replace(/\/+$/, "");
    }

    // 2. Global window config (from js/config.js)
    const conf = window.__RESOURCEHUB_CONFIG__ || window.__TOOLSHARE_CONFIG__;
    if (conf && conf.BACKEND_URL && conf.BACKEND_URL.trim()) {
      return conf.BACKEND_URL.trim().replace(/\/+$/, "");
    }

    // 3. Localhost development
    const host = window.location.hostname;
    if (host === "localhost" || host === "127.0.0.1" || host === "0.0.0.0") {
      // If frontend is served directly by Spring Boot on port 8080
      if (window.location.port === "8080") {
        return "/api";
      }
      return "http://localhost:8080/api";
    }

    // 4. Direct file system access (file://)
    if (window.location.protocol === "file:") {
      return "http://localhost:8080/api";
    }
  }

  // 4. Default for Vercel production:
  // Proxied through Vercel's 'rewrites' rule defined in vercel.json
  return "/api";
}

const API_BASE = resolveApiBase();

const Api = {
  // Base URL inspection & overrides
  getBaseUrl() {
    return API_BASE;
  },

  setBaseUrl(newUrl) {
    if (newUrl && newUrl.trim()) {
      let clean = newUrl.trim().replace(/\/+$/, "");
      if (!clean.endsWith("/api") && !clean.includes("/api")) {
        clean += "/api";
      }
      localStorage.setItem("toolshare_api_base", clean);
    } else {
      localStorage.removeItem("toolshare_api_base");
    }
    window.location.reload();
  },

  resetBaseUrl() {
    localStorage.removeItem("toolshare_api_base");
    localStorage.removeItem("resourcehub_api_base");
    window.location.reload();
  },

  async pingBackend() {
    try {
      const res = await fetch(`${API_BASE}/health`, { method: "GET" });
      if (res.ok) {
        const data = await res.json();
        return { ok: true, data };
      }
      const resTools = await fetch(`${API_BASE}/tools`, { method: "GET" });
      if (resTools.ok) {
        return { ok: true, data: { status: "UP", endpoint: "tools" } };
      }
      return { ok: false, status: res.status };
    } catch (e) {
      return { ok: false, error: e.message };
    }
  },

  // Token & User Auth Management
  getToken() {
    return localStorage.getItem("toolshare_token");
  },

  setAuth(authResponse) {
    localStorage.setItem("toolshare_token", authResponse.token);
    const userObj = {
      id: authResponse.id,
      username: authResponse.username,
      fullName: authResponse.fullName,
      email: authResponse.email,
      role: authResponse.role,
      mobile: authResponse.mobile || "",
      address: authResponse.address || "",
      companyName: authResponse.companyName || ""
    };
    localStorage.setItem("toolshare_user", JSON.stringify(userObj));
  },

  getUser() {
    const userStr = localStorage.getItem("toolshare_user");
    return userStr ? JSON.parse(userStr) : null;
  },

  isLoggedIn() {
    return !!this.getToken();
  },

  logout() {
    localStorage.removeItem("toolshare_token");
    localStorage.removeItem("toolshare_user");
    window.location.href = "login.html";
  },

  // City & Multi-City Support
  POPULAR_CITIES: [
    { name: "All Cities", label: "All Cities", tag: "Pan-India", icon: "🇮🇳" },
    { name: "Bengaluru", label: "Bengaluru", tag: "Karnataka", icon: "🏙️" },
    { name: "Mumbai", label: "Mumbai", tag: "Maharashtra", icon: "🏙️" },
    { name: "Delhi NCR", label: "Delhi NCR", tag: "Capital Region", icon: "🏛️" },
    { name: "Hyderabad", label: "Hyderabad", tag: "Telangana", icon: "🏰" },
    { name: "Chennai", label: "Chennai", tag: "Tamil Nadu", icon: "🌊" },
    { name: "Pune", label: "Pune", tag: "Maharashtra", icon: "⚙️" },
    { name: "Kolkata", label: "Kolkata", tag: "West Bengal", icon: "🌉" },
    { name: "Ahmedabad", label: "Ahmedabad", tag: "Gujarat", icon: "🏭" },
    { name: "Jaipur", label: "Jaipur", tag: "Rajasthan", icon: "👑" },
    { name: "Chandigarh", label: "Chandigarh", tag: "Punjab/Haryana", icon: "🌳" },
    { name: "Lucknow", label: "Lucknow", tag: "Uttar Pradesh", icon: "🕌" },
    { name: "Kochi", label: "Kochi", tag: "Kerala", icon: "🌴" },
    { name: "Coimbatore", label: "Coimbatore", tag: "Tamil Nadu", icon: "🧵" },
    { name: "Indore", label: "Indore", tag: "Madhya Pradesh", icon: "✨" }
  ],

  getSelectedCity() {
    return localStorage.getItem("toolshare_city") || "All Cities";
  },

  setSelectedCity(city) {
    if (!city || city.trim() === "" || city.toLowerCase().includes("all")) {
      localStorage.setItem("toolshare_city", "All Cities");
    } else {
      localStorage.setItem("toolshare_city", city.trim());
    }
  },

  // HTTP Request Helper
  async request(endpoint, options = {}) {
    const url = `${API_BASE}${endpoint}`;
    const headers = {
      "Content-Type": "application/json",
      ...(options.headers || {})
    };

    const token = this.getToken();
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }

    try {
      const response = await fetch(url, { ...options, headers });
      
      let data;
      const contentType = response.headers.get("content-type") || "";
      if (contentType.includes("application/json")) {
        data = await response.json();
      } else {
        const text = await response.text();
        try {
          data = JSON.parse(text);
        } catch {
          data = { message: text || `HTTP ${response.status} ${response.statusText}` };
        }
      }

      if (!response.ok) {
        if (response.status === 401 && !endpoint.includes("/auth/")) {
          this.logout();
        }
        let errorMsg = data.message || `Request failed with status ${response.status}`;
        if (data.data && typeof data.data === "object" && !Array.isArray(data.data)) {
          const fieldMsgs = Object.values(data.data).filter(Boolean);
          if (fieldMsgs.length > 0) {
            errorMsg = fieldMsgs.join(" • ");
          }
        }
        throw new Error(errorMsg);
      }

      return data;
    } catch (err) {
      console.error(`API Error on ${endpoint}:`, err);

      // Enhance fetch failure message for deployment clarity
      if (err.name === "TypeError" && err.message.toLowerCase().includes("fetch")) {
        const msg = `Unable to connect to backend at ${API_BASE}. If deployed on Vercel, verify your backend URL in API Settings.`;
        showToast(msg, "error");
        throw new Error(msg);
      }

      throw err;
    }
  },

  // Auth Endpoints
  login(usernameOrEmail, password) {
    return this.request("/auth/login", {
      method: "POST",
      body: JSON.stringify({ usernameOrEmail, password })
    });
  },

  register(userData) {
    return this.request("/auth/register", {
      method: "POST",
      body: JSON.stringify(userData)
    });
  },

  // User & Profile Endpoints
  async getCurrentUser() {
    try {
      const res = await this.request("/users/me");
      if (res && res.data) {
        const u = res.data;
        const current = this.getUser() || {};
        const merged = {
          ...current,
          id: u.id,
          username: u.username,
          fullName: u.fullName,
          email: u.email,
          role: u.role,
          mobile: u.mobile || "",
          address: u.address || "",
          companyName: u.companyName || ""
        };
        localStorage.setItem("toolshare_user", JSON.stringify(merged));
        return merged;
      }
    } catch (e) {
      console.warn("Could not refresh current user:", e);
    }
    return this.getUser();
  },

  async updateProfile(userData) {
    const res = await this.request("/users/me", {
      method: "PUT",
      body: JSON.stringify(userData)
    });
    if (res && res.data) {
      const u = res.data;
      const current = this.getUser() || {};
      const merged = {
        ...current,
        fullName: u.fullName || current.fullName,
        mobile: u.mobile || current.mobile,
        address: u.address || current.address,
        companyName: u.companyName || current.companyName
      };
      localStorage.setItem("toolshare_user", JSON.stringify(merged));
      return merged;
    }
    return res;
  },

  getUserById(id) {
    return this.request(`/users/${id}`);
  },

  // Tools Endpoints
  async getTools(params = {}) {
    const query = new URLSearchParams(params).toString();
    try {
      const res = await this.request(`/tools${query ? `?${query}` : ""}`);
      return res;
    } catch (err) {
      if (typeof window !== "undefined" && window.__FALLBACK_TOOLS__ && Array.isArray(window.__FALLBACK_TOOLS__)) {
        console.warn("Backend offline or unreachable, using cloud fallback dataset:", err.message);
        let list = [...window.__FALLBACK_TOOLS__];
        if (params.category && params.category !== "All") {
          list = list.filter(t => (t.category || "").toLowerCase() === params.category.toLowerCase());
        }
        if (params.city && params.city !== "All Cities") {
          list = list.filter(t => (t.location || "").toLowerCase().includes(params.city.toLowerCase()));
        }
        if (params.locality) {
          list = list.filter(t => (t.location || "").toLowerCase().includes(params.locality.toLowerCase()));
        }
        if (params.condition && params.condition !== "All") {
          list = list.filter(t => (t.toolCondition || "").toLowerCase() === params.condition.toLowerCase());
        }
        if (params.maxRate) {
          const max = parseFloat(params.maxRate);
          if (!isNaN(max)) {
            list = list.filter(t => Number(t.dailyRate) <= max);
          }
        }
        if (params.search || params.q) {
          const q = (params.search || params.q).toLowerCase();
          list = list.filter(t => (t.toolName || "").toLowerCase().includes(q) || (t.description || "").toLowerCase().includes(q));
        }
        return {
          success: true,
          message: "Tools retrieved (Cloud Standalone)",
          isFallback: true,
          data: list
        };
      }
      throw err;
    }
  },

  async getToolById(id) {
    try {
      return await this.request(`/tools/${id}`);
    } catch (err) {
      if (typeof window !== "undefined" && window.__FALLBACK_TOOLS__) {
        const found = window.__FALLBACK_TOOLS__.find(t => String(t.id) === String(id));
        if (found) {
          return { success: true, isFallback: true, data: found };
        }
      }
      throw err;
    }
  },

  createTool(toolData) {
    return this.request("/tools", {
      method: "POST",
      body: JSON.stringify(toolData)
    });
  },

  updateTool(id, toolData) {
    return this.request(`/tools/${id}`, {
      method: "PUT",
      body: JSON.stringify(toolData)
    });
  },

  deleteTool(id) {
    return this.request(`/tools/${id}`, {
      method: "DELETE"
    });
  },

  async getCategories() {
    try {
      return await this.request("/tools/categories");
    } catch (err) {
      if (typeof window !== "undefined" && window.__FALLBACK_TOOLS__) {
        const cats = [...new Set(window.__FALLBACK_TOOLS__.map(t => t.category).filter(Boolean))].sort();
        return { success: true, data: cats };
      }
      throw err;
    }
  },

  searchTools(query, category) {
    const params = new URLSearchParams();
    if (query) params.append("q", query);
    if (category && category !== "All") params.append("category", category);
    return this.getTools({ search: query, category });
  },

  // Borrow Request Endpoints
  createBorrowRequest(requestData) {
    return this.request("/borrow-requests", {
      method: "POST",
      body: JSON.stringify(requestData)
    });
  },

  getMyRequests() {
    return this.request("/borrow-requests/my");
  },

  getBorrowerRequests() {
    return this.request("/borrow-requests/my");
  },

  getLenderRequests() {
    return this.request("/borrow-requests/lender");
  },

  approveRequest(id) {
    return this.request(`/borrow-requests/${id}/approve`, {
      method: "PATCH"
    });
  },

  rejectRequest(id) {
    return this.request(`/borrow-requests/${id}/reject`, {
      method: "PATCH"
    });
  },

  cancelRequest(id) {
    return this.request(`/borrow-requests/${id}/cancel`, {
      method: "PATCH"
    });
  },

  returnTool(id) {
    return this.request(`/borrow-requests/${id}/return`, {
      method: "PATCH"
    });
  },

  confirmReturn(id) {
    return this.request(`/borrow-requests/${id}/return`, {
      method: "PATCH"
    });
  },

  // Review Endpoints
  createReview(reviewData) {
    return this.request("/reviews", {
      method: "POST",
      body: JSON.stringify(reviewData)
    });
  },

  getToolReviews(toolId) {
    return this.request(`/reviews/tool/${toolId}`);
  },

  // Dashboard Stats
  getLenderStats() {
    return this.request("/dashboard/lender/stats");
  },

  getBorrowerStats() {
    return this.request("/dashboard/borrower/stats");
  },

  getAdminStats() {
    return this.request("/dashboard/admin/stats");
  },

  // Suggestion Endpoints
  createSuggestion(suggestionData) {
    return this.request("/suggestions", {
      method: "POST",
      body: JSON.stringify(suggestionData)
    });
  },

  getMySuggestions() {
    return this.request("/suggestions/my");
  },

  // Admin Endpoints
  getAdminUsers() {
    return this.request("/admin/users");
  },

  toggleUserStatus(id, active) {
    return this.request(`/admin/users/${id}/status?active=${active}`, {
      method: "PATCH"
    });
  },

  getAdminTools() {
    return this.request("/admin/tools");
  },

  getAdminTransactions() {
    return this.request("/admin/transactions");
  },

  getAdminSuggestions() {
    return this.request("/admin/suggestions");
  },

  updateSuggestionStatus(id, status, adminResponse) {
    return this.request(`/admin/suggestions/${id}`, {
      method: "PATCH",
      body: JSON.stringify({ status, adminResponse })
    });
  }
};

// UI Notification Toasts
function showToast(message, type = "success") {
  let container = document.getElementById("toast-container");
  if (!container) {
    container = document.createElement("div");
    container.id = "toast-container";
    document.body.appendChild(container);
  }

  const toast = document.createElement("div");
  toast.className = `toast ${type}`;
  toast.innerHTML = `
    <span>${message}</span>
    <button onclick="this.parentElement.remove()" style="background:none;border:none;color:#fff;font-size:16px;cursor:pointer;margin-left:12px;">&times;</button>
  `;

  container.appendChild(toast);
  setTimeout(() => {
    toast.remove();
  }, 5000);
}

// Render dynamic user state in top navbar
function updateNavUser() {
  const user = Api.getUser();
  const navAccount = document.getElementById("nav-account");
  if (!navAccount) return;

  if (user) {
    let dashboardLink = "borrower-dashboard.html";
    if (user.role === "LENDER") dashboardLink = "lender-dashboard.html";
    if (user.role === "ADMIN") dashboardLink = "admin-dashboard.html";

    navAccount.innerHTML = `
      <a href="${dashboardLink}" class="nav-link-btn">
        <span class="subtext">Hello, ${(user.fullName || user.username || 'User').split(' ')[0]} <span class="role-pill">${user.role}</span></span>
        <strong>Dashboard & Account</strong>
      </a>
      <a href="javascript:void(0)" onclick="Api.logout()" class="nav-link-btn" title="Sign Out">
        <span class="subtext">Exit</span>
        <strong>Sign Out</strong>
      </a>
    `;
  } else {
    navAccount.innerHTML = `
      <a href="login.html" class="nav-link-btn">
        <span class="subtext">Hello, Sign in</span>
        <strong>Account & Lists</strong>
      </a>
    `;
  }
}

// Global API Settings Modal (Allows setting live backend URL easily on Vercel)
function initApiSettingsWidget() {
  if (document.getElementById("toolshare-api-settings-btn")) return;

  // Small floating status badge in bottom-right corner
  const btn = document.createElement("button");
  btn.id = "toolshare-api-settings-btn";
  btn.title = "Configure ResourceHub Backend API URL";
  btn.style.cssText = `
    position: fixed;
    bottom: 18px;
    right: 18px;
    z-index: 9999;
    background: #131921;
    color: #f3a847;
    border: 1px solid #3a4553;
    border-radius: 20px;
    padding: 6px 14px;
    font-size: 0.75rem;
    font-weight: 600;
    cursor: pointer;
    box-shadow: 0 4px 12px rgba(0,0,0,0.25);
    display: flex;
    align-items: center;
    gap: 6px;
    transition: transform 0.2s, background 0.2s;
  `;
  btn.innerHTML = `<span style="display:inline-block;width:8px;height:8px;border-radius:50%;background:#00e676;"></span> API Settings`;
  btn.onmouseover = () => { btn.style.background = "#232f3e"; };
  btn.onmouseout = () => { btn.style.background = "#131921"; };
  btn.onclick = openApiSettingsModal;
  document.body.appendChild(btn);

  // Modal Container
  const modal = document.createElement("div");
  modal.id = "toolshare-api-modal";
  modal.style.cssText = `
    display: none;
    position: fixed;
    top: 0; left: 0; right: 0; bottom: 0;
    background: rgba(0,0,0,0.65);
    backdrop-filter: blur(4px);
    z-index: 10000;
    justify-content: center;
    align-items: center;
  `;
  modal.innerHTML = `
    <div style="background:#fff;border-radius:12px;width:90%;max-width:480px;padding:24px;box-shadow:0 12px 36px rgba(0,0,0,0.3);position:relative;font-family:sans-serif;">
      <button onclick="closeApiSettingsModal()" style="position:absolute;top:16px;right:16px;background:none;border:none;font-size:22px;cursor:pointer;color:#666;">&times;</button>
      <h3 style="margin:0 0 8px 0;font-size:1.2rem;color:#131921;display:flex;align-items:center;gap:8px;">
        ⚙️ Backend API Configuration
      </h3>
      <p style="margin:0 0 16px 0;font-size:0.85rem;color:#555;line-height:1.4;">
        Connect your ResourceHub frontend to your deployed Java Spring Boot backend (Render, Railway, or Localhost).
      </p>

      <div style="background:#f4f6f8;border-radius:8px;padding:12px;margin-bottom:16px;font-size:0.82rem;">
        <div style="color:#777;margin-bottom:4px;">Current Active API Endpoint:</div>
        <code style="color:#007185;font-weight:700;word-break:break-all;" id="toolshare-current-api-display">${Api.getBaseUrl()}</code>
      </div>

      <div style="margin-bottom:16px;">
        <label style="display:block;font-size:0.85rem;font-weight:600;margin-bottom:6px;color:#222;">Custom Backend URL</label>
        <input type="url" id="toolshare-custom-api-input" placeholder="https://your-backend.onrender.com/api" 
               style="width:100%;box-sizing:border-box;padding:10px 12px;border:1px solid #ccc;border-radius:6px;font-size:0.9rem;" />
        <small style="display:block;color:#777;margin-top:4px;font-size:0.75rem;">
          Example: <code>https://resourcehub.onrender.com/api</code> or leave empty to use default.
        </small>
      </div>

      <div id="toolshare-ping-status" style="margin-bottom:16px;font-size:0.82rem;display:none;padding:10px;border-radius:6px;"></div>

      <div style="display:flex;gap:8px;justify-content:flex-end;">
        <button onclick="testApiConnection()" style="background:#e7f4f5;color:#007185;border:1px solid #007185;padding:8px 14px;border-radius:6px;cursor:pointer;font-weight:600;font-size:0.85rem;">
          Ping Backend
        </button>
        <button onclick="resetApiUrl()" style="background:#f5f5f5;color:#333;border:1px solid #ccc;padding:8px 14px;border-radius:6px;cursor:pointer;font-size:0.85rem;">
          Reset Default
        </button>
        <button onclick="saveApiUrl()" style="background:#ff9900;color:#111;border:none;padding:8px 18px;border-radius:6px;cursor:pointer;font-weight:700;font-size:0.85rem;">
          Save & Reload
        </button>
      </div>
    </div>
  `;
  document.body.appendChild(modal);
}

function openApiSettingsModal() {
  const modal = document.getElementById("toolshare-api-modal");
  if (!modal) return;
  const input = document.getElementById("toolshare-custom-api-input");
  input.value = localStorage.getItem("toolshare_api_base") || "";
  document.getElementById("toolshare-current-api-display").textContent = Api.getBaseUrl();
  modal.style.display = "flex";
}

function closeApiSettingsModal() {
  const modal = document.getElementById("toolshare-api-modal");
  if (modal) modal.style.display = "none";
}

async function testApiConnection() {
  const statusBox = document.getElementById("toolshare-ping-status");
  const input = document.getElementById("toolshare-custom-api-input");
  const testUrl = (input.value.trim() || Api.getBaseUrl()).replace(/\/+$/, "");
  
  statusBox.style.display = "block";
  statusBox.style.background = "#fff3cd";
  statusBox.style.color = "#856404";
  statusBox.innerHTML = `Testing connection to <strong>${testUrl}</strong>...`;

  try {
    const res = await fetch(`${testUrl}/health`, { method: "GET" });
    if (res.ok) {
      statusBox.style.background = "#d4edda";
      statusBox.style.color = "#155724";
      statusBox.innerHTML = `✅ Backend connected successfully! Status: 200 OK`;
      return;
    }
  } catch (e) {
    // try fallback ping on tools
    try {
      const res2 = await fetch(`${testUrl}/tools`, { method: "GET" });
      if (res2.ok) {
        statusBox.style.background = "#d4edda";
        statusBox.style.color = "#155724";
        statusBox.innerHTML = `✅ Backend reachable! Tools API active.`;
        return;
      }
    } catch (err2) {
      statusBox.style.background = "#f8d7da";
      statusBox.style.color = "#721c24";
      statusBox.innerHTML = `❌ Connection failed: ${err2.message}. Make sure backend server is running and CORS is enabled.`;
    }
  }
}

function saveApiUrl() {
  const input = document.getElementById("toolshare-custom-api-input");
  Api.setBaseUrl(input.value);
}

function resetApiUrl() {
  Api.resetBaseUrl();
}

document.addEventListener("DOMContentLoaded", () => {
  updateNavUser();
  initApiSettingsWidget();
});
