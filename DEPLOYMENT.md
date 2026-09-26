# ResourceHub — Complete Vercel & Cloud Deployment Guide

This guide walks you through deploying the **ToolShare** system to **Vercel** (Frontend) and connecting it to your **Spring Boot Backend & MySQL Database**.

---

## 🏗️ 1. Understanding the Architecture

| Component | Technology | Recommended Host | Why? |
| :--- | :--- | :--- | :--- |
| **Frontend** | HTML5, CSS3, Vanilla JS | **Vercel** | Lightning-fast Edge CDN, global caching, automated SSL, zero maintenance. |
| **Backend** | Spring Boot 3.3.4 (Java 17) | **Render** / **Railway** | Vercel runs Serverless Node/Python/Go and **does not host persistent Java JVM processes**. Render & Railway provide dedicated container environments. |
| **Database** | MySQL 9.2 (JDBC) | **Aiven** / **Railway** / **Clever Cloud** | Free, fully-managed cloud MySQL instances accessible worldwide. |

---

## ⚡ 2. Deploying the Frontend to Vercel

The project is already pre-configured with `vercel.json` and `.vercelignore`.

### Method A: Deploy via GitHub (Recommended)
1. Push your project to a GitHub repository:
   ```bash
   git init
   git add .
   git commit -m "ToolShare Vercel Ready"
   git branch -M main
   git remote add origin https://github.com/<your-username>/toolshare.git
   git push -u origin main
   ```
2. Open [Vercel Dashboard](https://vercel.com/new).
3. Click **Import** next to your GitHub repository.
4. In the Project Configuration:
   - **Framework Preset**: `Other`
   - **Root Directory**: Leave as `./` (the root `vercel.json` will automatically route to the `frontend/` directory).
   - *(Optional)*: If you prefer, you can select `frontend` as Root Directory. A `frontend/vercel.json` is also provided.
5. Click **Deploy**.
6. Within seconds, your site will be live at `https://your-project.vercel.app`!

---

### Method B: Deploy via Vercel CLI
If you prefer deploying directly from your computer without GitHub:
```bash
npx vercel
```
- Set up and deploy: **y**
- Which scope: *(Select your personal account)*
- Link to existing project: **n**
- Project name: `toolshare`
- In which directory is your code located: `./`
- Want to modify settings: **n**

---

## 🔌 3. Connecting Vercel to Your Live Backend

Once your backend is hosted (see Section 4 below), connect your Vercel frontend using **any of these 3 methods**:

### Option 1: Vercel Rewrites (Best Practice — No CORS / Mixed Content Issues)
Open `vercel.json` in the root folder, and replace `YOUR-BACKEND-URL` with your live backend domain:
```json
"rewrites": [
  {
    "source": "/api/:path*",
    "destination": "https://toolshare-api.onrender.com/api/:path*"
  }
]
```
Commit and push. Vercel will reverse-proxy all `/api/...` calls directly to your backend!

### Option 2: Edit `frontend/js/config.js`
Open [frontend/js/config.js](file:///c:/Users/Hp/OneDrive/Desktop/Java/frontend/js/config.js) and set `BACKEND_URL`:
```javascript
window.__TOOLSHARE_CONFIG__ = {
  BACKEND_URL: "https://toolshare-api.onrender.com/api"
};
```

### Option 3: In-Browser GUI (Instant Testing)
1. Visit your deployed site: `https://your-project.vercel.app`.
2. Look at the bottom-right corner and click the **`⚙️ API Settings`** button.
3. Paste your backend URL (e.g. `https://toolshare-api.onrender.com/api`).
4. Click **Ping Backend** to test the connection, then click **Save & Reload**!

---

## ☁️ 4. Deploying the Java Backend (Free on Render)

We have created a production multi-stage [`backend/Dockerfile`](file:///c:/Users/Hp/OneDrive/Desktop/Java/backend/Dockerfile) ready for 1-click cloud deployment.

### Step 1: Create a Free MySQL Database
1. Go to [Aiven.io](https://aiven.io) or [Railway.app](https://railway.app).
2. Create a free **MySQL** database service.
3. Copy the database connection details:
   - **Host** (e.g. `mysql-xyz.aivencloud.com`)
   - **Port** (e.g. `12345`)
   - **User** (e.g. `avnadmin`)
   - **Password**
   - **Database Name** (e.g. `defaultdb` or `toolshare`)
4. In MySQL Workbench / DBeaver / CLI, run the schema scripts:
   - Run [`database/schema.sql`](file:///c:/Users/Hp/OneDrive/Desktop/Java/database/schema.sql)
   - Run [`database/sample_data.sql`](file:///c:/Users/Hp/OneDrive/Desktop/Java/database/sample_data.sql)

### Step 2: Deploy Spring Boot on Render
1. Open [Render.com](https://dashboard.render.com).
2. Click **New +** $\rightarrow$ **Web Service**.
3. Connect your GitHub repository.
4. Configure the Web Service:
   - **Name**: `toolshare-backend`
   - **Region**: Closest to you (e.g., Singapore, Frankfurt, Oregon)
   - **Root Directory**: `backend`
   - **Runtime**: `Docker` (Render will automatically detect [`backend/Dockerfile`](file:///c:/Users/Hp/OneDrive/Desktop/Java/backend/Dockerfile))
   - **Instance Type**: `Free`
5. Under **Environment Variables**, add:
   | Key | Value |
   | :--- | :--- |
   | `SPRING_DATASOURCE_URL` | `jdbc:mysql://<HOST>:<PORT>/<DATABASE>?useSSL=true&serverTimezone=UTC` |
   | `SPRING_DATASOURCE_USERNAME` | `<YOUR_DB_USER>` |
   | `SPRING_DATASOURCE_PASSWORD` | `<YOUR_DB_PASSWORD>` |
   | `APP_JWT_SECRET` | `404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970` |
6. Click **Create Web Service**.
7. Render will build the Docker container and provide a live URL (e.g., `https://toolshare-backend.onrender.com`).
8. Verify it by visiting:
   ```
   https://toolshare-backend.onrender.com/api/health
   ```
   You should see: `{"status": "UP", "service": "ToolShare Spring Boot Backend", "version": "1.0.0"}`.

---

## 🛠️ 5. Summary of Files Configured for Vercel

- [`vercel.json`](file:///c:/Users/Hp/OneDrive/Desktop/Java/vercel.json): Root configuration specifying `outputDirectory: "frontend"`, `/api` rewrites, and asset cache headers.
- [`frontend/vercel.json`](file:///c:/Users/Hp/OneDrive/Desktop/Java/frontend/vercel.json): Subdirectory configuration if deploying with root set to `frontend`.
- [`.vercelignore`](file:///c:/Users/Hp/OneDrive/Desktop/Java/.vercelignore): Excludes Maven binaries, `.jar` artifacts, and heavy assets to keep deployment uploads fast (< 5MB).
- [`frontend/js/config.js`](file:///c:/Users/Hp/OneDrive/Desktop/Java/frontend/js/config.js): Global configuration for defining production backend URLs.
- [`frontend/js/api.js`](file:///c:/Users/Hp/OneDrive/Desktop/Java/frontend/js/api.js): Smart API handler with automatic localhost vs. cloud detection, health pinging, and an in-browser configuration modal.
- [`backend/Dockerfile`](file:///c:/Users/Hp/OneDrive/Desktop/Java/backend/Dockerfile): Multi-stage container build for Java 17 + Spring Boot 3.3.4.
- [`backend/src/main/java/com/toolshare/controller/HealthController.java`](file:///c:/Users/Hp/OneDrive/Desktop/Java/backend/src/main/java/com/toolshare/controller/HealthController.java): Public `/api/health` endpoint for uptime testing.
- [`backend/src/main/resources/application.properties`](file:///c:/Users/Hp/OneDrive/Desktop/Java/backend/src/main/resources/application.properties): Updated with environment variable fallbacks for dynamic cloud database injection.
