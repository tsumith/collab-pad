import { useState } from 'react'
import { useAuth } from "react-oidc-context";
import Sidebar from './components/Sidebar'
import Dashboard from './components/Dashboard'
import DocumentEditor from './components/DocumentEditor'

function App() {
  const auth = useAuth();
  const [currentView, setCurrentView] = useState('dashboard');
  const [selectedDocId, setSelectedDocId] = useState(null);
  const [activeWorkspaceId, setActiveWorkspaceId] = useState(null);
  
  if (auth.isLoading) {
    return <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>Loading Security Context...</div>;
  }

  if (auth.error) {
    return <div>Security Error: {auth.error.message}</div>;
  }

  // If the user isn't logged in, show the landing page
  if (!auth.isAuthenticated) {
    return (
      <div style={{ display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center', height: '100vh', backgroundColor: 'var(--bg-main)' }}>
        <h1 style={{ fontSize: '3rem', marginBottom: '20px' }}>KnowledgeHub</h1>
        <p style={{ color: 'var(--text-secondary)', marginBottom: '30px' }}>Enterprise AI Knowledge Management</p>
        <button className="primary-button" style={{ padding: '15px 30px', fontSize: '1.2rem' }} onClick={() => void auth.signinRedirect()}>
          Secure Login (Keycloak)
        </button>
      </div>
    );
  }

  const handleOpenDoc = (id) => {
    setSelectedDocId(id);
    setCurrentView('editor');
  };

  const handleGoHome = () => {
    setSelectedDocId(null);
    setCurrentView('dashboard');
  };

  return (
    <div className="app-container">
      <Sidebar 
        onGoHome={handleGoHome} 
        activeWorkspaceId={activeWorkspaceId} 
        onWorkspaceChange={setActiveWorkspaceId} 
        onOpenDoc={handleOpenDoc}
      />
      
      <main className="main-content">
        <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '20px' }}>
            <span style={{ marginRight: '15px', color: 'var(--text-secondary)', display: 'flex', alignItems: 'center' }}>
              Logged in as: {auth.user?.profile.preferred_username}
            </span>
            <button className="nav-button" style={{ width: 'auto', padding: '5px 10px', border: '1px solid var(--border-color)' }} onClick={() => void auth.signoutRedirect()}>
              Sign Out
            </button>
        </div>

        {currentView === 'dashboard' ? (
          activeWorkspaceId ? (
            <Dashboard workspaceId={activeWorkspaceId} onOpenDoc={handleOpenDoc} />
          ) : (
            <div style={{ display: 'flex', justifyContent: 'center', marginTop: '50px' }}>Loading workspace data...</div>
          )
        ) : (
          <DocumentEditor documentId={selectedDocId} onBack={handleGoHome} />
        )}
      </main>
    </div>
  )
}

export default App
