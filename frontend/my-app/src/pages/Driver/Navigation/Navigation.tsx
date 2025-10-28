import React from 'react';
import GraphhopperNavigation from './GraphhopperNavigation';

interface NavigationProps {
  navigate: (path: string) => void;
}

const Navigation: React.FC<NavigationProps> = ({ navigate }) => {
  return <GraphhopperNavigation navigate={navigate} />;
};

export default Navigation;