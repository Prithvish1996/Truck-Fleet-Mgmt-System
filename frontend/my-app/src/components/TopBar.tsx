import React from "react";
import "./TopBar.css";
import logoSmall from "../assets/logo.png";
import { authService } from "../services/authService";
import { useNavigate } from "react-router-dom";

interface TopBarProps {
  title: string;
}

const TopBar: React.FC<TopBarProps> = ({ title }) => {
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      const token = authService.getToken();
      if (token) {
        await authService.logout(token);
      }
    } catch (e) {
      console.warn("Logout API error:", e);
    }

    authService.removeToken();
    navigate("/");
  };

  return (
    <header className="topbar">
      {/* LEFT SIDE */}
      <div className="topbar-left">
        <img src={logoSmall} className="topbar-logo" alt="EcoFlow Small Logo" />
        <h1>{title}</h1>
      </div>

      {/* RIGHT SIDE */}
      <div className="topbar-right">

        <button className="logout-btn" onClick={handleLogout}>
          Logout
        </button>

      </div>
    </header>
  );
};

export default TopBar;
