import axios from 'axios';
import { User } from 'oidc-client-ts';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_GATEWAY_URL,
  headers: {
    'Content-Type': 'application/json',
  }
});

api.interceptors.request.use((config) => {
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
