'use client';

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { supabase } from '@/lib/supabase';
import { useAuth } from '@/components/AuthProvider';
import { Lock, Mail, ShieldAlert, ArrowRight } from 'lucide-react';

export default function LoginPage() {
  const router = useRouter();
  const { user, profile, isLoading: authLoading } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    const storedError = sessionStorage.getItem('auth_error');
    if (storedError) {
      setErrorMsg(storedError);
      sessionStorage.removeItem('auth_error');
    }

    if (!authLoading && user && profile && profile.is_active && profile.role !== 'operator') {
      router.replace('/dashboard');
    }
  }, [user, profile, authLoading, router]);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg(null);

    if (!email.trim() || !password.trim()) {
      setErrorMsg('অনুগ্রহ করে ইমেইল এবং পাসওয়ার্ড উভয়ই প্রদান করুন।');
      return;
    }

    setIsSubmitting(true);
    try {
      const { data, error } = await supabase.auth.signInWithPassword({
        email: email.trim(),
        password: password.trim(),
      });

      if (error) {
        console.error('Supabase login error:', error);
        setErrorMsg('ইমেইল বা পাসওয়ার্ড সঠিক নয়। অনুগ্রহ করে পুনরায় চেষ্টা করুন।');
        setIsSubmitting(false);
        return;
      }

      if (data.user) {
        // Fetch profile to verify admin role
        const { data: profileData, error: profErr } = await supabase
          .from('profiles')
          .select('*, unions(*)')
          .eq('id', data.user.id)
          .single();

        if (profErr || !profileData) {
          console.error('Profile read error:', profErr);
          await supabase.auth.signOut();
          setErrorMsg('ব্যবহারকারীর প্রোফাইল পাওয়া যায়নি।');
          setIsSubmitting(false);
          return;
        }

        // Strict role verification:
        if (!profileData.is_active || profileData.role === 'operator') {
          await supabase.auth.signOut();
          setErrorMsg('এই প্যানেলে প্রবেশের অনুমতি নেই (শুধুমাত্র ইউনিয়ন ও সুপার অ্যাডমিনদের জন্য)।');
          setIsSubmitting(false);
          return;
        }

        router.push('/dashboard');
      }
    } catch (err: any) {
      console.error('Unexpected login exception:', err);
      setErrorMsg('সার্ভারে যোগাযোগ করতে সমস্যা হচ্ছে। কিছুক্ষণ পর আবার চেষ্টা করুন।');
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-emerald-50 via-slate-50 to-emerald-100 flex items-center justify-center p-4 font-bangla">
      <div className="max-w-md w-full bg-white rounded-3xl shadow-xl border border-slate-100 overflow-hidden">
        {/* Header Banner */}
        <div className="bg-bdGreen-600 px-8 py-10 text-white text-center relative overflow-hidden">
          <div className="absolute -right-8 -bottom-8 w-32 h-32 bg-white/10 rounded-full blur-xl pointer-events-none" />
          <div className="w-16 h-16 mx-auto bg-white text-bdGreen-700 rounded-2xl flex items-center justify-center font-extrabold text-2xl shadow-lg mb-4">
            ইউপি
          </div>
          <h1 className="text-2xl font-bold tracking-tight">ইউপি সনদ অ্যাডমিন প্যানেল</h1>
          <p className="text-emerald-100 text-sm mt-1">
            ডিজিটাল ইউনিয়ন পরিষদ সনদপত্র ও প্রত্যয়ন ব্যবস্থাপনা
          </p>
        </div>

        {/* Form Body */}
        <div className="p-8">
          {errorMsg && (
            <div className="mb-6 p-4 bg-bdRed-50 border border-bdRed-100 rounded-xl flex items-start gap-3 text-bdRed-600">
              <ShieldAlert className="w-5 h-5 shrink-0 mt-0.5" />
              <p className="text-xs font-semibold leading-relaxed">{errorMsg}</p>
            </div>
          )}

          <form onSubmit={handleLogin} className="space-y-5">
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-2">
                অ্যাডমিন ইমেইল
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                  <Mail className="w-5 h-5" />
                </div>
                <input
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="admin@example.com"
                  className="w-full pl-11 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-bdGreen-600 focus:bg-white transition"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-2">
                গোপন পাসওয়ার্ড
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                  <Lock className="w-5 h-5" />
                </div>
                <input
                  type="password"
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  className="w-full pl-11 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-bdGreen-600 focus:bg-white transition"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={isSubmitting}
              className="w-full mt-2 py-3.5 px-4 bg-bdGreen-600 hover:bg-bdGreen-700 active:scale-[0.99] text-white rounded-xl font-bold text-sm shadow-md hover:shadow-lg transition flex items-center justify-center gap-2 disabled:opacity-60 cursor-pointer"
            >
              {isSubmitting ? (
                <>
                  <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                  <span>যাচাই করা হচ্ছে...</span>
                </>
              ) : (
                <>
                  <span>লগইন করুন</span>
                  <ArrowRight className="w-4 h-4" />
                </>
              )}
            </button>
          </form>

          {/* Security Notice */}
          <div className="mt-8 pt-6 border-t border-slate-100 text-center">
            <p className="text-xs text-slate-500 leading-relaxed">
              শুধুমাত্র ইউনিয়ন পরিষদ অ্যাডমিন এবং অনুমোদিত কর্মকর্তাগণ এখানে প্রবেশ করতে পারবেন। সাধারণ
              অপারেটরদের মোবাইল অ্যাপ ব্যবহারের অনুরোধ করা হচ্ছে।
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
