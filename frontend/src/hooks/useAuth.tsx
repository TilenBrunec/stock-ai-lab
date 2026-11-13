import { createContext, useContext, useEffect, useState, type ReactNode } from 'react';
import type { Session } from '@supabase/supabase-js';
import { supabase } from '../config/supabase';
import { api } from '../api/client';
import type { Me } from '../types/types';

interface AuthState {
  session: Session | null;
  loading: boolean;
  ime: string | null;
  slika: string | null;
  me: Me | null;
  signIn: () => Promise<void>;
  signOut: () => Promise<void>;
}

const AuthContext = createContext<AuthState | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<Session | null>(null);
  const [loading, setLoading] = useState(true);
  const [me, setMe] = useState<Me | null>(null);

  // seja iz Supabase (ob nalaganju in ob vsaki prijavi/odjavi)
  useEffect(() => {
    supabase.auth.getSession().then(({ data }) => {
      setSession(data.session);
      setLoading(false);
    });
    const { data: { subscription } } =
      supabase.auth.onAuthStateChange((_event, s) => setSession(s));
    return () => subscription.unsubscribe();
  }, []);

  // po prijavi pokliči backend: ustvari uporabnika in vrne vlogo
  useEffect(() => {
    if (!session) {
      setMe(null);
      return;
    }
    api.me().then(setMe).catch(() => setMe(null));
  }, [session]);

  const meta = session?.user.user_metadata ?? {};

  const value: AuthState = {
    session,
    loading,
    ime: meta.full_name ?? meta.name ?? session?.user.email ?? null,
    slika: meta.avatar_url ?? meta.picture ?? null,
    me,
    signIn: async () => {
      await supabase.auth.signInWithOAuth({
        provider: 'google',
        options: { redirectTo: window.location.origin },
      });
    },
    signOut: async () => {
      await supabase.auth.signOut();
    },
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth mora biti znotraj <AuthProvider>');
  return ctx;
}