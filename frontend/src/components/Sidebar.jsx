import { useEffect, useState } from 'react';
import { Home, Users, Settings, BookOpen, ChevronRight, ChevronDown, FileText, X } from 'lucide-react';
import { workspaceService } from '../services/workspaceService';
import { documentService } from '../services/documentService';
export default function Sidebar({ currentView, onGoHome, onGoTeam, activeWorkspaceId, onWorkspaceChange, onOpenDoc, onNeedsOnboarding, isOpen, onClose }) {
  const [workspaces, setWorkspaces] = useState([]);
  const [documentTree, setDocumentTree] = useState([]);

  const handleHome = () => {
    onGoHome();
    if (onClose) onClose();
  };

  const handleTeam = () => {
    onGoTeam();
    if (onClose) onClose();
  };

  const handleDoc = (id) => {
    onOpenDoc(id);
    if (onClose) onClose();
  };

  useEffect(() => {
    workspaceService.getWorkspaces()
      .then(data => {
        setWorkspaces(data);
        if (data.length > 0) {
          if (!activeWorkspaceId) {
            onWorkspaceChange(data[0].id);
          }
        } else {
          if (onNeedsOnboarding) onNeedsOnboarding();
        }
      })
      .catch(err => {
        console.error("Failed to load workspaces", err);
        if (err.response?.status === 500 || err.response?.status === 401 || err.response?.status === 403) {
          if (onNeedsOnboarding) onNeedsOnboarding();
        }
      });
  }, [activeWorkspaceId, onWorkspaceChange, onNeedsOnboarding]);

  useEffect(() => {
    if (activeWorkspaceId) {
      documentService.getDocumentTree(activeWorkspaceId)
        .then(data => {
          setDocumentTree(data);
        })
        .catch(err => console.error("Failed to load document tree", err));
    } else {
      setDocumentTree([]);
    }
  }, [activeWorkspaceId]);

  return (
    <aside className={`sidebar ${isOpen ? 'open' : ''}`}>
      <div className="title-logo" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '24px' }}>
        <span style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <BookOpen size={24} color="var(--accent-color)" />
          CollabPad
        </span>
        {isOpen && onClose && (
          <button
            className="icon-button mobile-close-button"
            onClick={onClose}
            aria-label="Close Sidebar"
          >
            <X size={20} />
          </button>
        )}
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
        <button className={`nav-button ${currentView === 'dashboard' ? 'active' : ''}`} onClick={handleHome}>
          <Home size={18} />
          <span>Dashboard</span>
        </button>

        <button className={`nav-button ${currentView === 'team' ? 'active' : ''}`} onClick={handleTeam}>
          <Users size={18} />
          <span>Team Directory</span>
        </button>
      </nav>

      <div style={{ flex: 2, overflowY: 'auto', padding: '10px 0' }}>
        <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', padding: '0 10px', marginBottom: '10px' }}>Documents</p>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
          {documentTree.map(node => (
            <TreeItem key={node.id} node={node} level={0} onOpenDoc={handleDoc} />
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
