import { useState, useEffect } from 'react';
import { ChevronLeft, Cloud, Check } from 'lucide-react';
import { documentService } from '../services/documentService';

export default function DocumentEditor({ documentId, onBack }) {
  const [doc, setDoc] = useState({ title: '', content: '' });
  const [saving, setSaving] = useState(false);
  const [lastSaved, setLastSaved] = useState(null);

  useEffect(() => {
    fetchDoc();
  }, [documentId]);

  const fetchDoc = async () => {
    try {
      const data = await documentService.getDocumentById(documentId);
      setDoc(data);
    } catch (err) {
      console.error("Failed to load document", err);
    }
  };

  const handleSave = async () => {
    setSaving(true);
    try {
      const data = await documentService.updateDocument(
        documentId,
        doc.title,
        doc.content,
        doc.workspaceId
      );
      setDoc(data);
      setLastSaved(new Date());
    } catch (err) {
      console.error("Failed to save", err);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto', height: '100%', display: 'flex', flexDirection: 'column' }}>
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '32px' }}>
        <button onClick={onBack} className="icon-button" style={{ marginLeft: '-8px' }}>
          <ChevronLeft size={24} />
          <span style={{ marginLeft: '4px', fontSize: '0.95rem' }}>Back</span>
        </button>
        
        <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
          {lastSaved && (
            <span style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', display: 'flex', alignItems: 'center', gap: '6px' }}>
              <Check size={14} />
              Saved {lastSaved.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}
            </span>
          )}
          <button onClick={handleSave} className="primary-button" disabled={saving}>
            <Cloud size={16} />
            {saving ? 'Saving...' : 'Save'}
          </button>
        </div>
      </header>

      <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
        <input 
          type="text" 
          value={doc.title || ''}
          onChange={(e) => setDoc({...doc, title: e.target.value})}
          className="editor-title-input"
          placeholder="Untitled Document"
        />
        
        <textarea
          value={doc.content || ''}
          onChange={(e) => setDoc({...doc, content: e.target.value})}
          className="editor-textarea"
          placeholder="Start writing..."
        />
      </div>
    </div>
  );
}
