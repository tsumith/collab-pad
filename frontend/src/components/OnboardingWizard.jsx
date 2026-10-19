import { useState } from 'react';
import { onboardingService } from '../services/onboardingService';

export default function OnboardingWizard({ userProfile, onComplete }) {
  const [companyName, setCompanyName] = useState('');
  const [departments, setDepartments] = useState({
    Engineering: true,
    Product: true,
    HR: false,
    Marketing: false,
    Legal: false,
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const toggleDept = (dept) => {
    setDepartments(prev => ({...prev, [dept]: !prev[dept]}));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!companyName.trim()) return;
    
    setLoading(true);
    setError('');
    
    const selected = Object.keys(departments).filter(k => departments[k]);
    
    try {
      await onboardingService.enrollCompany(companyName, selected);
      onComplete(); // callback to refresh the app
    } catch (err) {
      setError(err.response?.data || "Failed to enroll. Ensure backend is running.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
      backgroundColor: 'rgba(0,0,0,0.8)',
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      zIndex: 1000
    }}>
      <div style={{
        backgroundColor: 'var(--bg-main)',
        padding: '40px',
        borderRadius: '12px',
        width: '500px',
        maxWidth: '90%'
      }}>
        <h2 style={{marginBottom: '10px'}}>Welcome to KnowledgeHub!</h2>
        <p style={{color: 'var(--text-secondary)', marginBottom: '30px'}}>
          Let's set up your company workspace.
        </p>

        {error && <div style={{color: 'red', marginBottom: '20px'}}>{error}</div>}

        <form onSubmit={handleSubmit} style={{display: 'flex', flexDirection: 'column', gap: '20px'}}>
          <div>
            <label style={{display: 'block', marginBottom: '8px', color: 'var(--text-secondary)'}}>Your Name</label>
            <input type="text" className="minimal-input" value={userProfile?.name || userProfile?.preferred_username || ''} disabled style={{opacity: 0.7}} />
          </div>
          
          <div>
            <label style={{display: 'block', marginBottom: '8px', color: 'var(--text-secondary)'}}>Your Email</label>
            <input type="email" className="minimal-input" value={userProfile?.email || ''} disabled style={{opacity: 0.7}} />
          </div>

          <div>
            <label style={{display: 'block', marginBottom: '8px', color: 'var(--text-secondary)'}}>Company Name</label>
            <input 
              type="text" 
              className="minimal-input" 
              value={companyName} 
              onChange={e => setCompanyName(e.target.value)} 
              placeholder="e.g. Acme Corp"
              required 
            />
          </div>

          <div>
            <label style={{display: 'block', marginBottom: '8px', color: 'var(--text-secondary)'}}>Bootstrap Departments</label>
            <div style={{display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px'}}>
              {Object.keys(departments).map(dept => (
                <label key={dept} style={{display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer'}}>
                  <input 
                    type="checkbox" 
                    checked={departments[dept]} 
                    onChange={() => toggleDept(dept)} 
                  />
                  {dept}
                </label>
              ))}
            </div>
          </div>

          <button type="submit" className="primary-button" style={{marginTop: '10px', justifyContent: 'center'}} disabled={loading}>
            {loading ? 'Setting up...' : 'Create Company Workspace'}
          </button>
        </form>
      </div>
    </div>
  );
}
