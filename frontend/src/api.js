import axios from 'axios';

const api = axios.create({
  baseURL: `${import.meta.env.VITE_API_GATEWAY_URL}/api/v1/`,
  headers: {
    'Content-Type': 'application/json',
  }
});

api.interceptors.request.use(async (config) => {
  if (window.Clerk && window.Clerk.session) {
    try {
      const token = await window.Clerk.session.getToken();
      if (token) {
        config.headers['Authorization'] = `Bearer ${token}`;
      }
    } catch (err) {
      console.error("Failed to retrieve Clerk session token", err);
    }
  }
  return config;
});

export default api;
