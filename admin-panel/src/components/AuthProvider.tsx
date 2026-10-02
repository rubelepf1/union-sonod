'use client';

import React, { createContext, useContext, useEffect, useState } from 'react';
import { User, Session } from '@supabase/supabase-js';
import { useRouter, usePathname } from 'next/navigation';
import { supabase } from '@/lib/supabase';
import { Profile, Union } from '@/lib/types';

interface AuthContextType {
  user: User | null;
  profile: Profile | null;
  union: Union | null;
  session: Session | null;
  isLoading: boolean;
  signOut: () => Promise<void>;
  refreshProfile: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType>({
  user: null,
  profile: null,
  union: null,
  session: null,
  isLoading: true,
  signOut: async () => {},
  refreshProfile: async () => {},
});

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [session, setSession] = useState<Session | null>(null);
  const [profile, setProfile] = useState<Profile | null>(null);
  const [union, setUnion] = useState<Union | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const router = useRouter();
  const pathname = usePathname();

  const fetchProfileAndUnion = async (userId: string): Promise<boolean> => {
    try {
      const { data: profileData, error: profileErr } = await supabase
        .from('profiles')
        .select('*, unions(*)')
        .eq('id', userId)
        .single();

      if (profileErr || !profileData) {
        console.error('Profile fetch failed:', profileErr);
        await supabase.auth.signOut();
        setUser(null);
        setSession(null);
        setProfile(null);
        setUnion(null);
        return false;
      }

      // Check access rules:
      // If is_active is false OR role is operator, sign out and show error
      if (!profileData.is_active || profileData.role === 'operator') {
        await supabase.auth.signOut();
        setUser(null);
        setSession(null);
        setProfile(null);
        setUnion(null);
        sessionStorage.setItem('auth_error', 'এই প্যানেলে প্রবেশের অনুমতি নেই (শুধুমাত্র অ্যাডমিনগণ প্রবেশ করতে পারবেন)');
        router.push('/login');
        return false;
      }

      setProfile(profileData);
      if (profileData.unions) {
        setUnion(profileData.unions);
      } else if (profileData.union_id) {
        const { data: uData } = await supabase
          .from('unions')
          .select('*')
          .eq('id', profileData.union_id)
          .single();
        if (uData) setUnion(uData);
      }
      return true;
    } catch (err) {
      console.error('Error fetching user profile:', err);
      return false;
    }
  };

  const refreshProfile = async () => {
    if (user?.id) {
      await fetchProfileAndUnion(user.id);
    }
  };

  useEffect(() => {
    let mounted = true;

    async function checkInitialSession() {
      try {
        const { data: { session: currentSession } } = await supabase.auth.getSession();
        if (!mounted) return;

        if (currentSession?.user) {
          setSession(currentSession);
          setUser(currentSession.user);
          const hasAccess = await fetchProfileAndUnion(currentSession.user.id);
          if (!hasAccess && pathname !== '/login') {
            router.push('/login');
          }
        } else {
          setUser(null);
          setSession(null);
          setProfile(null);
          setUnion(null);
          if (pathname !== '/login') {
            router.push('/login');
          }
        }
      } catch (err) {
        console.error('Session check error:', err);
      } finally {
        if (mounted) setIsLoading(false);
      }
    }

    checkInitialSession();

    const { data: { subscription } } = supabase.auth.onAuthStateChange(async (event, newSession) => {
      if (!mounted) return;
      setSession(newSession);
      setUser(newSession?.user ?? null);

      if (newSession?.user) {
        const ok = await fetchProfileAndUnion(newSession.user.id);
        if (ok && pathname === '/login') {
          router.push('/dashboard');
        }
      } else {
        setProfile(null);
        setUnion(null);
        if (pathname !== '/login') {
          router.push('/login');
        }
      }
    });

    return () => {
      mounted = false;
      subscription.unsubscribe();
    };
  }, [pathname, router]);

  const signOut = async () => {
    await supabase.auth.signOut();
    setUser(null);
    setSession(null);
    setProfile(null);
    setUnion(null);
    router.push('/login');
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        profile,
        union,
        session,
        isLoading,
        signOut,
        refreshProfile,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
