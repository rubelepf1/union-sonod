'use client';

import React, { useEffect, useState } from 'react';
import { AdminLayout } from '@/components/AdminLayout';
import { useAuth } from '@/components/AuthProvider';
import { supabase } from '@/lib/supabase';
import { Profile, UserRole } from '@/lib/types';
import { toBanglaNumber, formatBanglaDate, getRoleMeta } from '@/lib/bangla';
import { RoleBadge } from '@/components/RoleBadge';
import { LoadingSkeleton } from '@/components/LoadingSkeleton';
import { EmptyState } from '@/components/EmptyState';
import {
  Users,
  Info,
  ShieldCheck,
  CheckCircle,
  XCircle,
  AlertCircle,
  RefreshCw,
} from 'lucide-react';

export default function UsersPage() {
  const { profile } = useAuth();
  const [users, setUsers] = useState<Profile[]>([]);
  const [loading, setLoading] = useState(true);
  const [feedback, setFeedback] = useState<{ type: 'success' | 'error'; text: string } | null>(null);
  const [updatingId, setUpdatingId] = useState<string | null>(null);

  const fetchUsers = async () => {
    if (!profile) return;
    setLoading(true);
    try {
      let query = supabase.from('profiles').select('*, unions(*)').order('created_at', { ascending: false });
      if (profile.role !== 'super_admin' && profile.union_id) {
        query = query.eq('union_id', profile.union_id);
      }
      const { data, error } = await query;
      if (error) {
        console.error('Fetch users error:', error);
        setFeedback({ type: 'error', text: 'ইউজার তালিকা লোড করতে ব্যর্থ হয়েছে।' });
      } else if (data) {
        setUsers(data as Profile[]);
      }
    } catch (err) {
      console.error('Users exception:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, [profile]);

  // Toggle active status
  const handleToggleActive = async (targetUser: Profile) => {
    if (!profile) return;
    setUpdatingId(targetUser.id);
    setFeedback(null);

    // Rule: Union admin can only toggle operators
    if (profile.role === 'union_admin' && targetUser.role !== 'operator') {
      setFeedback({ type: 'error', text: 'ইউনিয়ন অ্যাডমিন শুধুমাত্র অপারেটরদের স্ট্যাটাস পরিবর্তন করতে পারবেন।' });
      setUpdatingId(null);
      return;
    }

    const nextState = !targetUser.is_active;

    try {
      const { error } = await supabase
        .from('profiles')
        .update({ is_active: nextState })
        .eq('id', targetUser.id);

      if (error) {
        console.error('Update is_active error:', error);
        setFeedback({
          type: 'error',
          text: 'স্ট্যাটাস পরিবর্তন করার অনুমতি নেই বা RLS পলিসিতে বাঁধা দেওয়া হয়েছে।',
        });
      } else {
        setUsers((prev) =>
          prev.map((u) => (u.id === targetUser.id ? { ...u, is_active: nextState } : u))
        );
        setFeedback({
          type: 'success',
          text: `${targetUser.full_name}-এর অ্যাকাউন্ট ${nextState ? 'সক্রিয়' : 'নিষ্ক্রিয়'} করা হয়েছে।`,
        });
      }
    } catch (err) {
      console.error('Toggle active exception:', err);
      setFeedback({ type: 'error', text: 'সার্ভারে সমস্যা হয়েছে।' });
    } finally {
      setUpdatingId(null);
    }
  };

  // Change role (super_admin only)
  const handleChangeRole = async (targetUser: Profile, newRole: UserRole) => {
    if (!profile || profile.role !== 'super_admin') {
      setFeedback({ type: 'error', text: 'রোল পরিবর্তন করার ক্ষমতা শুধুমাত্র সুপার অ্যাডমিনের রয়েছে।' });
      return;
    }

    setUpdatingId(targetUser.id);
    setFeedback(null);

    try {
      const { error } = await supabase
        .from('profiles')
        .update({ role: newRole })
        .eq('id', targetUser.id);

      if (error) {
        console.error('Update role error:', error);
        setFeedback({ type: 'error', text: 'রোল পরিবর্তনে ত্রুটি হয়েছে।' });
      } else {
        setUsers((prev) =>
          prev.map((u) => (u.id === targetUser.id ? { ...u, role: newRole } : u))
        );
        setFeedback({ type: 'success', text: 'রোল সফলভাবে পরিবর্তন করা হয়েছে।' });
      }
    } catch (err) {
      console.error('Change role exception:', err);
    } finally {
      setUpdatingId(null);
    }
  };

  const isSuperAdmin = profile?.role === 'super_admin';

  return (
    <AdminLayout>
      {/* Title & Refresh */}
      <div className="mb-6 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h1 className="text-xl md:text-2xl font-bold text-slate-800">ইউজার ও কর্মকর্তা ব্যবস্থাপনা</h1>
          <p className="text-xs md:text-sm text-slate-500 mt-0.5">
            মোট ব্যবহারকারী: {toBanglaNumber(users.length)} জন
          </p>
        </div>

        <button
          onClick={fetchUsers}
          className="flex items-center gap-1.5 px-3.5 py-2 bg-white border border-slate-200 text-slate-700 hover:bg-slate-50 rounded-xl text-xs font-semibold shadow-xs transition w-fit"
        >
          <RefreshCw className="w-4 h-4 text-slate-500" />
          <span>রিফ্রেশ</span>
        </button>
      </div>

      {/* Mandatory Notice as requested by User prompt */}
      <div className="mb-6 p-4 bg-emerald-50 border border-emerald-200 rounded-2xl flex items-start gap-3 text-emerald-900 text-xs">
        <Info className="w-5 h-5 text-emerald-700 shrink-0 mt-0.5" />
        <div className="leading-relaxed">
          <strong className="block text-emerald-800 font-bold mb-0.5">নতুন ইউজার যোগ করার নিয়ম:</strong>
          <span>নতুন ইউজার যোগ করতে Supabase ড্যাশবোর্ডের Authentication &gt; Users ব্যবহার করুন, এবং এখানে এসে চালু করুন।</span>
        </div>
      </div>

      {/* Feedback Toast / Alert */}
      {feedback && (
        <div
          className={`mb-6 p-3.5 rounded-xl text-xs flex items-center gap-2 ${
            feedback.type === 'success'
              ? 'bg-emerald-50 text-emerald-800 border border-emerald-200'
              : 'bg-rose-50 text-rose-800 border border-rose-200'
          }`}
        >
          {feedback.type === 'success' ? (
            <CheckCircle className="w-4 h-4 shrink-0 text-emerald-600" />
          ) : (
            <AlertCircle className="w-4 h-4 shrink-0 text-rose-600" />
          )}
          <span>{feedback.text}</span>
        </div>
      )}

      {loading ? (
        <LoadingSkeleton rows={5} />
      ) : users.length === 0 ? (
        <EmptyState
          title="কোনো ইউজার পাওয়া যায়নি"
          description="আপনার ইউনিয়নে বর্তমানে কোনো সক্রিয় ইউজার প্রোফাইল নেই।"
        />
      ) : (
        <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 text-slate-600 border-b border-slate-200">
                <tr>
                  <th className="py-3.5 px-4 font-semibold">নাম</th>
                  <th className="py-3.5 px-4 font-semibold">ফোন নম্বর</th>
                  <th className="py-3.5 px-4 font-semibold">রোল (Role)</th>
                  {isSuperAdmin && <th className="py-3.5 px-4 font-semibold">ইউনিয়ন</th>}
                  <th className="py-3.5 px-4 font-semibold">যোগদানের তারিখ</th>
                  <th className="py-3.5 px-4 font-semibold text-center">অ্যাকাউন্ট স্ট্যাটাস</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {users.map((u) => {
                  const isUpdating = updatingId === u.id;
                  const canToggle = isSuperAdmin || (profile?.role === 'union_admin' && u.role === 'operator');

                  return (
                    <tr key={u.id} className="hover:bg-slate-50/70 transition">
                      <td className="py-3.5 px-4 font-bold text-slate-800">
                        {u.full_name}
                      </td>
                      <td className="py-3.5 px-4 text-slate-600 font-mono text-[11px]">
                        {u.phone || '—'}
                      </td>
                      <td className="py-3.5 px-4">
                        {isSuperAdmin ? (
                          <select
                            value={u.role}
                            disabled={isUpdating}
                            onChange={(e) => handleChangeRole(u, e.target.value as UserRole)}
                            className="text-xs font-semibold py-1 px-2.5 bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-1 focus:ring-bdGreen-600"
                          >
                            <option value="operator">অপারেটর</option>
                            <option value="union_admin">ইউনিয়ন অ্যাডমিন</option>
                            <option value="super_admin">সুপার অ্যাডমিন</option>
                          </select>
                        ) : (
                          <RoleBadge role={u.role} />
                        )}
                      </td>
                      {isSuperAdmin && (
                        <td className="py-3.5 px-4 text-slate-700">
                          {u.unions?.name_bn || '—'}
                        </td>
                      )}
                      <td className="py-3.5 px-4 text-slate-500 whitespace-nowrap">
                        {formatBanglaDate(u.created_at)}
                      </td>
                      <td className="py-3.5 px-4 text-center">
                        <button
                          onClick={() => handleToggleActive(u)}
                          disabled={!canToggle || isUpdating}
                          className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold transition shadow-2xs ${
                            u.is_active
                              ? 'bg-emerald-50 text-emerald-700 hover:bg-emerald-100 border border-emerald-200'
                              : 'bg-rose-50 text-rose-700 hover:bg-rose-100 border border-rose-200'
                          } disabled:opacity-40 disabled:cursor-not-allowed`}
                        >
                          {u.is_active ? (
                            <>
                              <CheckCircle className="w-3.5 h-3.5" />
                              <span>সক্রিয় (Active)</span>
                            </>
                          ) : (
                            <>
                              <XCircle className="w-3.5 h-3.5" />
                              <span>নিষ্ক্রিয় (Disabled)</span>
                            </>
                          )}
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </AdminLayout>
  );
}
