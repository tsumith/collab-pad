import { useState } from 'react'
import { useAuth } from "react-oidc-context";
import Sidebar from './components/Sidebar'
import Dashboard from './components/Dashboard'
import DocumentEditor from './components/DocumentEditor'
import OnboardingWizard from './components/OnboardingWizard'

function App() {
  const auth = useAuth();
  const [currentView, setCurrentView] = useState('dashboard');
  const [selectedDocId, setSelectedDocId] = useState(null);
  const [activeWorkspaceId, setActiveWorkspaceId] = useState(null);
  const [needsOnboarding, setNeedsOnboarding] = useState(false);

  if (auth.isLoading) {
    return <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>Loading Context...</div>;
  }

  if (auth.error) {
    return (
      <div style={{ display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center', height: '100vh', backgroundColor: 'var(--bg-main)' }}>
        <h2>Security Error</h2>
        <p style={{ color: 'red' }}>{auth.error.message}</p>
        <p style={{ color: 'var(--text-secondary)', marginTop: '10px', maxWidth: '500px', textAlign: 'center' }}>
          This usually happens if you refreshed the page during login, or if your browser tab lost its session storage.
        </p>
        <button
          className="primary-button"
          style={{ marginTop: '20px', padding: '10px 20px' }}
          onClick={() => {
            window.history.replaceState({}, document.title, window.location.pathname);
            auth.removeUser();
            window.location.reload();
          }}
        >
          Clear Error & Try Again
        </button>
      </div>
    );
  }

  // If the user isn't logged in, show the landing page
  if (!auth.isAuthenticated) {
    return (
      <div style={{ display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center', height: '100vh', backgroundColor: 'var(--bg-main)' }}>
        <h1 style={{ fontSize: '3rem', marginBottom: '20px' }}>KnowledgeHub</h1>
        <p style={{ color: 'var(--text-secondary)', marginBottom: '30px' }}>Enterprise AI Knowledge Management</p>
        <button className="primary-button" style={{ padding: '15px 30px', fontSize: '1.2rem' }} onClick={() => void auth.signinRedirect()}>
          Login (Keycloak)
        </button>
      </div>
    );
  }

  if (needsOnboarding) {
    return <OnboardingWizard userProfile={auth.user?.profile} onComplete={() => {
      setNeedsOnboarding(false);
      // force reload to trigger sidebar fetch again
      window.location.reload();
    }} />;
  }

  const handleOpenDoc = (id) => {
    setSelectedDocId(id);
    setCurrentView('editor');
  };

  const handleGoHome = () => {
    setSelectedDocId(null);
    setCurrentView('dashboard');
  };

  const handleGoTeam = () => {
    setSelectedDocId(null);
    setCurrentView('team');
  };

  return (
    <div className="app-container">
      <Sidebar
        currentView={currentView}
        onGoHome={handleGoHome}
        onGoTeam={handleGoTeam}
        activeWorkspaceId={activeWorkspaceId}
        onWorkspaceChange={setActiveWorkspaceId}
        onOpenDoc={handleOpenDoc}
        onNeedsOnboarding={() => setNeedsOnboarding(true)}
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

        {currentView === 'team' ? (
          <div style={{ padding: '20px' }}>
            <h2 style={{ fontSize: '2rem', marginBottom: '8px' }}>Team Directory</h2>
            <p style={{ color: 'var(--text-secondary)' }}>Coming soon...</p>
          </div>
        ) : currentView === 'dashboard' ? (
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
