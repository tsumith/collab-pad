import axios from 'axios';

const BASE_URL = import.meta.env.API_GATEWAY_URL || '';

const api = axios.create({
  baseURL: `${BASE_URL.replace(/\/+$/, '')}/api/v1/`,
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
