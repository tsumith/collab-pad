import { useState, useEffect } from 'react';
import { Plus, Search, FileText } from 'lucide-react';
import { documentService } from '../services/documentService';

export default function Dashboard({ workspaceId, onOpenDoc }) {
  const [documents, setDocuments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [newTitle, setNewTitle] = useState('');

  useEffect(() => {
    fetchDocuments();
  }, [workspaceId]);

  const fetchDocuments = async () => {
    try {
      setLoading(true);
      const data = await documentService.getDocumentsByWorkspace(workspaceId);
      setDocuments(data);
    } catch (err) {
      console.error("Failed to fetch documents", err);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    if (!newTitle.trim()) return;
    
    try {
      const data = await documentService.createDocument(
        newTitle,
        '',
        workspaceId
      );
      setDocuments([...documents, data]);
      setNewTitle('');
      onOpenDoc(data.id);
    } catch (err) {
      console.error("Failed to create document", err);
    }
  };

  return (
    <div style={{ maxWidth: '1000px', margin: '0 auto' }}>
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '40px' }}>
        <div>
          <h1 style={{ fontSize: '2rem', marginBottom: '8px' }}>Workspace Docs</h1>
          <p style={{ color: 'var(--text-secondary)' }}>View and manage your collaborative documents.</p>
        </div>
        
        <div style={{ position: 'relative', width: '300px' }}>
          <Search size={18} style={{ position: 'absolute', left: '12px', top: '11px', color: 'var(--text-secondary)' }} />
          <input type="text" className="minimal-input" placeholder="Search documents..." style={{ paddingLeft: '40px' }} />
        </div>
      </header>

      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '40px' }}>
        <div>
          <h3 style={{ marginBottom: '20px', fontSize: '1.1rem', color: 'var(--text-secondary)' }}>Recent Documents</h3>
          
          {loading ? (
            <p style={{ color: 'var(--text-secondary)' }}>Loading documents...</p>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              {documents.length === 0 ? (
                <div style={{ padding: '40px', textAlign: 'center', backgroundColor: 'var(--bg-sidebar)', borderRadius: '12px', border: '1px dashed var(--border-color)' }}>
                  <FileText size={32} style={{ color: 'var(--text-secondary)', margin: '0 auto 16px' }} />
                  <p style={{ color: 'var(--text-secondary)' }}>No documents found in this workspace.</p>
                </div>
              ) : (
                documents.map(doc => (
                  <div key={doc.id} className="document-card" onClick={() => onOpenDoc(doc.id)}>
                    <div style={{ display: 'flex', alignItems: 'flex-start', gap: '16px' }}>
                      <div style={{ padding: '10px', backgroundColor: 'var(--bg-sidebar)', borderRadius: '8px' }}>
                        <FileText size={24} color="var(--accent-color)" />
                      </div>
                      <div>
                        <h4 style={{ fontSize: '1.1rem', marginBottom: '4px' }}>{doc.title}</h4>
                        <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                          Last updated {new Date(doc.updatedAt).toLocaleDateString()} at {new Date(doc.updatedAt).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}
                        </p>
                      </div>
                    </div>
                  </div>
                ))
              )}
            </div>
          )}
        </div>

        <div>
          <div style={{ backgroundColor: 'var(--bg-sidebar)', padding: '24px', borderRadius: '12px', border: '1px solid var(--border-color)' }}>
            <h3 style={{ marginBottom: '16px', fontSize: '1.1rem' }}>Create New</h3>
            <form onSubmit={handleCreate} style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              <input 
                type="text" 
                className="minimal-input" 
                value={newTitle}
                onChange={(e) => setNewTitle(e.target.value)}
                placeholder="Document title" 
                required
              />
              <button type="submit" className="primary-button" style={{ justifyContent: 'center' }}>
                <Plus size={18} />
                Create Document
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}
