import { useEffect, useState } from 'react';
import { Home, Server, Users, Settings, BookOpen, ChevronRight, ChevronDown, FileText } from 'lucide-react';
import api from '../api';

export default function Sidebar({ onGoHome, activeWorkspaceId, onWorkspaceChange, onOpenDoc }) {
  const [workspaces, setWorkspaces] = useState([]);
  const [documentTree, setDocumentTree] = useState([]);
  
  useEffect(() => {
    api.get('/workspaces')
      .then(res => {
        setWorkspaces(res.data);
        if (res.data.length > 0 && !activeWorkspaceId) {
          onWorkspaceChange(res.data[0].id);
        }
      })
      .catch(err => console.error("Failed to load workspaces", err));
  }, [activeWorkspaceId, onWorkspaceChange]);

  useEffect(() => {
    if (activeWorkspaceId) {
      api.get(`/documents/tree?workspaceId=${activeWorkspaceId}`)
        .then(res => {
          setDocumentTree(res.data);
        })
        .catch(err => console.error("Failed to load document tree", err));
    } else {
      setDocumentTree([]);
    }
  }, [activeWorkspaceId]);

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

      <div style={{ flex: 2, overflowY: 'auto', padding: '10px 0' }}>
        <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', padding: '0 10px', marginBottom: '10px' }}>Documents</p>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
          {documentTree.map(node => (
            <TreeItem key={node.id} node={node} level={0} onOpenDoc={onOpenDoc} />
          ))}
          {documentTree.length === 0 && (
            <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', padding: '0 10px' }}>No documents yet.</span>
          )}
        </div>
      </div>

      <div style={{ marginTop: 'auto' }}>
        <button className="nav-button">
          <Settings size={18} />
          <span>Settings</span>
        </button>
      </div>
    </aside>
  );
}

function TreeItem({ node, level, onOpenDoc }) {
  const [expanded, setExpanded] = useState(false);
  const hasChildren = node.children && node.children.length > 0;

  return (
    <div style={{ display: 'flex', flexDirection: 'column' }}>
      <div 
        className="nav-button" 
        style={{ 
          paddingLeft: `${(level * 12) + 10}px`, 
          display: 'flex', 
          alignItems: 'center', 
          gap: '5px',
          cursor: 'pointer'
        }}
      >
        <span 
          style={{ display: 'flex', alignItems: 'center', width: '16px', height: '16px', justifyContent: 'center' }}
          onClick={(e) => {
            e.stopPropagation();
            setExpanded(!expanded);
          }}
        >
          {hasChildren ? (
            expanded ? <ChevronDown size={14} /> : <ChevronRight size={14} />
          ) : (
            <span style={{ width: '14px' }}></span>
          )}
        </span>

        <div 
          style={{ display: 'flex', alignItems: 'center', gap: '5px', flex: 1 }}
          onClick={() => onOpenDoc && onOpenDoc(node.id)}
        >
          {node.icon ? (
            <span>{node.icon}</span>
          ) : (
            <FileText size={14} color="var(--text-secondary)" />
          )}
          <span style={{ fontSize: '0.9rem', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
            {node.title}
          </span>
        </div>
      </div>

      {expanded && hasChildren && (
        <div style={{ display: 'flex', flexDirection: 'column' }}>
          {node.children.map(child => (
            <TreeItem key={child.id} node={child} level={level + 1} onOpenDoc={onOpenDoc} />
          ))}
        </div>
      )}
    </div>
  );
}
