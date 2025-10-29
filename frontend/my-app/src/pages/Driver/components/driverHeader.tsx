import { useState, useEffect, useRef } from 'react';
import logo from '../../../assets/logo.png';
import { authService } from '../../../services/authService';




export default function DriverHeader({ navigate }: { navigate: (path: string) => void }) {
    const [isLoggingOut, setIsLoggingOut] = useState(false);
    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const menuRef = useRef<HTMLDivElement>(null);

    const handleLogout = async () => {
      setIsLoggingOut(true);
      try {
        const token = authService.getToken();
        if (token) {
          await authService.logout(token);
        }
      } catch (error) {
        console.error('Logout error:', error);
      } finally {
        authService.removeToken();
        navigate('/');
      }
    };

    const handleBack = () => {
      window.history.back();
      setIsMenuOpen(false);
    };

    const toggleMenu = () => {
      setIsMenuOpen(!isMenuOpen);
    };

    useEffect(() => {
      const handleClickOutside = (event: MouseEvent) => {
        if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
          setIsMenuOpen(false);
        }
      };

      if (isMenuOpen) {
        document.addEventListener('mousedown', handleClickOutside);
      }

      return () => {
        document.removeEventListener('mousedown', handleClickOutside);
      };
    }, [isMenuOpen]);

    const handleBackToDashboard = () => {
      navigate('/driver/dashboard');
    };
    
  return (
    <header className="dashboard-header" style={{ backgroundColor: '#96D6A1' , borderRadius: '0px'}}>
    <div className="header-left">
      <div className="logo">
        <img src={logo} alt="Driver GO" style={{width: '50px', height: '50px', objectFit: 'contain', padding: '0px', zIndex: '1000'}}/>
        <span className="logo-text">Driver GO</span>
      </div>
    </div>
    <h1 className="header-title">Home</h1>
    <div className="header-right">
      <div className="hamburger-menu" ref={menuRef}>
        <button 
          className="hamburger-btn" 
          onClick={toggleMenu}
          disabled={isLoggingOut}
        >
          <div className="hamburger-icon">
            <span></span>
            <span></span>
            <span></span>
          </div>
        </button>
        
        {isMenuOpen && (
          <div className="dropdown-menu">
            <button 
              className="dropdown-item" 
              onClick={handleBack}
            >
              <div className="menu-icon">←</div>
              <span>Back</span>
            </button>
            <button 
              className="dropdown-item" 
              onClick={handleLogout}
              disabled={isLoggingOut}
            >
              <div className="menu-icon">↪</div>
              <span>Logout</span>
            </button>
            <button 
              className="dropdown-item" 
              onClick={handleBackToDashboard}
            >
              <div className="menu-icon" style={{ fontSize: '1.2em', lineHeight: '1' }} aria-label="Home">
                <svg width="20" height="20" viewBox="0 0 20 20" fill="none" style={{display: "block"}} xmlns="http://www.w3.org/2000/svg">
                  <path d="M2 10L10 3L18 10" stroke="#555" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                  <path d="M4 10V17C4 17.5523 4.44772 18 5 18H8C8.55228 18 9 17.5523 9 17V13H11V17C11 17.5523 11.4477 18 12 18H15C15.5523 18 16 17.5523 16 17V10" stroke="#555" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
              </div>
              <span>Home</span>
            </button>
          </div>
        )}
      </div>
    </div>
    </header>
  );
}


