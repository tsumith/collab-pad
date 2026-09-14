import api from '../api';

export const documentService = {
  getDocumentsByWorkspace: async (workspaceId) => {
    const response = await api.get(`/documents?workspaceId=${workspaceId}`);
    return response.data;
  },
  
  getDocumentTree: async (workspaceId) => {
    const response = await api.get(`/documents/tree?workspaceId=${workspaceId}`);
    return response.data;
  },
  
  getDocumentById: async (documentId) => {
    const response = await api.get(`/documents/${documentId}`);
    return response.data;
  },
  
  createDocument: async (title, content, workspaceId) => {
    const response = await api.post('/documents', { title, content, workspaceId });
    return response.data;
  },
  
  updateDocument: async (documentId, title, content, workspaceId) => {
    const response = await api.put(`/documents/${documentId}`, { title, content, workspaceId });
    return response.data;
  }
};
