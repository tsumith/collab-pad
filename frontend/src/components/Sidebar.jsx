import { useEffect, useState } from 'react';
import { Home, Server, Users, Settings, BookOpen } from 'lucide-react';
import api from '../api';

export default function Sidebar({ onGoHome, activeWorkspaceId, onWorkspaceChange }) {
  const [workspaces, setWorkspaces] = useState([]);
  
  useEffect(() => {
    // Fetch user's workspaces from the API
    api.get('/workspaces')
      .then(res => {
        setWorkspaces(res.data);
        if (res.data.length > 0 && !activeWorkspaceId) {
          onWorkspaceChange(res.data[0].id); // Auto-select the first one
        }
      })
      .catch(err => console.error("Failed to load workspaces", err));
  }, [activeWorkspaceId, onWorkspaceChange]);

  return (
    <aside className="sidebar">
      <div className="title-logo">
        <span style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <BookOpen size={24} color="var(--accent-color)" />
          KnowledgeHub
        </span>
      </div>

      <div style={{ padding: '0 10px', marginBottom: '20px' }}>
        <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '5px' }}>Current Workspace</p>
        <select 
          style={{ width: '100%', padding: '8px', borderRadius: '4px', backgroundColor: 'var(--bg-elevated)', color: 'var(--text-primary)', border: '1px solid var(--border-color)' }}
          value={activeWorkspaceId || ''}
          onChange={(e) => onWorkspaceChange(Number(e.target.value))}
        >
          {workspaces.map(ws => (
            <option key={ws.id} value={ws.id}>{ws.name}</option>
          ))}
        </select>
      </div>

      <nav style={{ display: 'flex', flexDirection: 'column', gap: '4px', flex: 1 }}>
        <button className="nav-button active" onClick={onGoHome}>
          <Home size={18} />
          <span>Dashboard</span>
        </button>
        
        <button className="nav-button">
          <Users size={18} />
          <span>Team Directory</span>
        </button>
      </nav>

      <div style={{ marginTop: 'auto' }}>
        <button className="nav-button">
          <Settings size={18} />
          <span>Settings</span>
        </button>
      </div>
    </aside>
  );
}
