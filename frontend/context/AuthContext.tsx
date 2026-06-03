"use client";

import React, { createContext, useContext, useEffect, useState } from 'react';
import Cookies from 'js-cookie';
import { useRouter } from 'next/navigation';

export interface User {
  email: string;
  name: string;
  role: 'ADMIN' | 'SELLER' | 'BUYER';
}

interface AuthContextType {
  user: User | null;
  login: (token: string, user: User) => void;
  logout: () => void;
  isLoading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {
    const token = Cookies.get('token');
    const storedUser = Cookies.get('user');
    
    if (token && storedUser) {
      try {
        setUser(JSON.parse(storedUser));
      } catch (e) {
        Cookies.remove('token');
        Cookies.remove('user');
      }
    }
    setIsLoading(false);
  }, []);

  const login = (token: string, userData: User) => {
    Cookies.set('token', token, { expires: 1 });
    Cookies.set('user', JSON.stringify(userData), { expires: 1 });
    setUser(userData);
    
    if (userData.role === 'ADMIN') {
      router.push('/admin/orders');
    } else {
      router.push('/products');
    }
  };

  const logout = () => {
    Cookies.remove('token');
    Cookies.remove('user');
    setUser(null);
    router.push('/login');
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, isLoading }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
