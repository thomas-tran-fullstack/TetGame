import { ReactNode } from 'react';
import Header from './Header';
import './MainLayout.css';

interface MainLayoutProps {
  children: ReactNode;
  showHeader?: boolean;
}

export default function MainLayout({ children, showHeader = true }: MainLayoutProps) {
  return (
    <div className="main-layout">
      {showHeader && <Header />}
      <main className="main-content">
        {children}
      </main>
    </div>
  );
}
