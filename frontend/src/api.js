import axios from 'axios';
import { User } from 'oidc-client-ts';

// We route traffic through the API Gateway (Port 8080) instead of the Backend (Port 8081)
const api = axios.create({
  baseURL: import.meta.env.VITE_API_GATEWAY_URL,
  headers: {
    'Content-Type': 'application/json',
  }
});

// Automatically inject the real JWT token from Keycloak!
api.interceptors.request.use((config) => {
  // Keycloak dynamically generates this storage key based on the authority and client_id
  const authority = import.meta.env.VITE_KEYCLOAK_AUTHORITY;
  const clientId = import.meta.env.VITE_KEYCLOAK_CLIENT_ID;
  const oidcStorage = sessionStorage.getItem(`oidc.user:${authority}:${clientId}`)
  
  if (oidcStorage) {
      const user = User.fromStorageString(oidcStorage);
      if (user && user.access_token) {
          config.headers['Authorization'] = `Bearer ${user.access_token}`;
      }
  }
  return config;
});

export default api;
