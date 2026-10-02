'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/components/AuthProvider';

export default function HomePage() {
  const router = useRouter();
  const { user, profile, isLoading } = useAuth();

  useEffect(() => {
    if (!isLoading) {
      if (user && profile && profile.is_active && profile.role !== 'operator') {
        router.replace('/dashboard');
      } else {
        router.replace('/login');
      }
    }
  }, [user, profile, isLoading, router]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50 font-bangla">
      <div className="flex flex-col items-center gap-3">
        <div className="w-12 h-12 border-4 border-bdGreen-600 border-t-transparent rounded-full animate-spin"></div>
        <p className="text-slate-600 font-medium">লোড হচ্ছে, অপেক্ষা করুন...</p>
      </div>
    </div>
  );
}
