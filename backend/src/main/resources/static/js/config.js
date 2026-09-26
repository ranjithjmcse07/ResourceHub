/**
 * ResourceHub Global Environment & Backend Configuration
 * 
 * Deployment options for Vercel:
 * 1. Default (Recommended): Leave BACKEND_URL empty ("").
 *    Vercel will proxy all requests to /api using the 'rewrites' rule defined in vercel.json.
 * 
 * 2. Direct Cross-Origin API: Set BACKEND_URL to your deployed backend URL.
 *    e.g. BACKEND_URL: "https://resourcehub-api.onrender.com/api"
 * 
 * 3. Local Development: Automatically detects localhost and connects to http://localhost:8080/api.
 */
const config = {
  // Set your production backend API URL here if not using vercel.json rewrites:
  BACKEND_URL: ""
};

window.__RESOURCEHUB_CONFIG__ = config;
window.__TOOLSHARE_CONFIG__ = config;
