import { useState } from 'react'
import { useAuth, useUser, SignIn, UserButton } from "@clerk/react";
import Sidebar from './components/Sidebar'
import Dashboard from './components/Dashboard'
import DocumentEditor from './components/DocumentEditor'
import OnboardingWizard from './components/OnboardingWizard'

function App() {
  const { isLoaded, isSignedIn } = useAuth();
  const { user } = useUser();
  const [currentView, setCurrentView] = useState('dashboard');
  const [selectedDocId, setSelectedDocId] = useState(null);
  const [activeWorkspaceId, setActiveWorkspaceId] = useState(null);
  const [needsOnboarding, setNeedsOnboarding] = useState(false);

  if (!isLoaded) {
    return <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>Loading Identity...</div>;
  }

  if (!isSignedIn) {
    return (
      <div style={{ display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', backgroundColor: 'var(--bg-main)', padding: '20px' }}>
        <h1 style={{ fontSize: '3rem', marginBottom: '8px' }}>Collab Pad</h1>
        <p style={{ color: 'var(--text-secondary)', marginBottom: '32px' }}>AI Knowledge Management</p>
        <SignIn />
      </div>
    );
  }

  if (needsOnboarding) {
    const userProfile = {
      preferred_username: user?.username || user?.firstName || user?.primaryEmailAddress?.emailAddress,
      email: user?.primaryEmailAddress?.emailAddress,
      name: user?.fullName || user?.firstName
    };
    return <OnboardingWizard userProfile={userProfile} onComplete={() => {
      setNeedsOnboarding(false);
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
        <div style={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'center', gap: '15px', marginBottom: '20px' }}>
          <span style={{ color: 'var(--text-secondary)' }}>
            Logged in as: {user?.primaryEmailAddress?.emailAddress || user?.fullName}
          </span>
          <UserButton />
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
