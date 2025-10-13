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

    // Close menu when clicking outside
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
          </div>
        )}
      </div>
    </div>
    </header>
  );
}


