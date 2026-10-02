'use client';

import React, { useEffect, useState, useCallback } from 'react';
import { AdminLayout } from '@/components/AdminLayout';
import { useAuth } from '@/components/AuthProvider';
import { supabase } from '@/lib/supabase';
import { Profile, Union, UserRole } from '@/lib/types';
import { toBanglaNumber, formatBanglaDate } from '@/lib/bangla';
import { RoleBadge } from '@/components/RoleBadge';
import { LoadingSkeleton } from '@/components/LoadingSkeleton';
import { EmptyState } from '@/components/EmptyState';
import {
  CheckCircle,
  XCircle,
  AlertCircle,
  RefreshCw,
  UserPlus,
  KeyRound,
  Eye,
  EyeOff,
  X,
  Building,
  User,
  Mail,
  Phone,
  Lock,
} from 'lucide-react';

export default function UsersPage() {
  const { profile, union: currentUnion } = useAuth();
  const [users, setUsers] = useState<Profile[]>([]);
  const [unions, setUnions] = useState<Union[]>([]);
  const [loading, setLoading] = useState(true);
  const [feedback, setFeedback] = useState<{ type: 'success' | 'error'; text: string } | null>(null);
  const [updatingId, setUpdatingId] = useState<string | null>(null);

  // Create User Modal State
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [showPassword, setShowPassword] = useState(false);

  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [selectedRole, setSelectedRole] = useState<UserRole>('operator');
  const [selectedUnionId, setSelectedUnionId] = useState<string>('');

  // Reset Password Modal State
  const [resetTargetUser, setResetTargetUser] = useState<Profile | null>(null);
  const [newPassword, setNewPassword] = useState('');
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [isResetting, setIsResetting] = useState(false);
  const [resetError, setResetError] = useState<string | null>(null);

  const isSuperAdmin = profile?.role === 'super_admin';
  const isUnionAdmin = profile?.role === 'union_admin';

  // Fetch Users List
  const fetchUsers = useCallback(async () => {
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
  }, [profile]);

  // Fetch unions for Super Admin
  const fetchUnions = useCallback(async () => {
    if (profile?.role !== 'super_admin') return;
    try {
      const { data, error } = await supabase
        .from('unions')
        .select('*')
        .order('name_bn', { ascending: true });
      if (!error && data) {
        setUnions(data as Union[]);
      }
    } catch (err) {
      console.error('Fetch unions exception:', err);
    }
  }, [profile]);

  useEffect(() => {
    fetchUsers();
    fetchUnions();
  }, [fetchUsers, fetchUnions]);

  // Open Create Modal
  const handleOpenCreateModal = () => {
    setFullName('');
    setEmail('');
    setPhone('');
    setPassword('');
    setShowPassword(false);
    setFormError(null);
    if (isUnionAdmin) {
      setSelectedRole('operator');
      setSelectedUnionId(profile?.union_id || '');
    } else {
      setSelectedRole('operator');
      setSelectedUnionId(unions[0]?.id || '');
    }
    setIsCreateModalOpen(true);
  };

  // Submit Create User
  const handleCreateUserSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError(null);

    // Validation
    if (!fullName.trim() || fullName.trim().length < 2) {
      setFormError('ব্যবহারকারীর পূর্ণ নাম কমপক্ষে ২ অক্ষরের হতে হবে।');
      return;
    }
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!email.trim() || !emailRegex.test(email.trim())) {
      setFormError('একটি সঠিক ইমেইল ঠিকানা প্রদান করুন।');
      return;
    }
    if (!password || password.length < 8) {
      setFormError('পাসওয়ার্ড কমপক্ষে ৮ অক্ষরের হতে হবে।');
      return;
    }

    const targetUnionId = isUnionAdmin ? profile?.union_id : selectedUnionId || null;
    if (selectedRole !== 'super_admin' && !targetUnionId) {
      setFormError('অনুগ্রহ করে ইউনিয়ন পরিষদ নির্বাচন করুন।');
      return;
    }

    setIsSubmitting(true);

    try {
      const { data, error } = await supabase.functions.invoke('create-user', {
        body: {
          action: 'create',
          full_name: fullName.trim(),
          email: email.trim().toLowerCase(),
          phone: phone.trim() || null,
          password: password,
          role: selectedRole,
          union_id: targetUnionId,
        },
      });

      if (error) {
        let errMessage = 'নতুন ইউজার তৈরি করতে ব্যর্থ হয়েছে।';
        try {
          if ('context' in error && error.context && typeof (error.context as any).json === 'function') {
            const errJson = await (error.context as any).json();
            if (errJson?.error) errMessage = errJson.error;
          } else if (data?.error) {
            errMessage = data.error;
          } else if (error.message) {
            errMessage = error.message;
          }
        } catch {
          if (error.message) errMessage = error.message;
        }
        setFormError(errMessage);
        return;
      }

      if (data?.error) {
        setFormError(data.error);
        return;
      }

      setFeedback({
        type: 'success',
        text: data?.message || `${fullName}-এর অ্যাকাউন্ট সফলভাবে তৈরি করা হয়েছে।`,
      });
      setIsCreateModalOpen(false);
      await fetchUsers();
    } catch (err: any) {
      console.error('Create user exception:', err);
      setFormError(err?.message || 'সার্ভারে সংযোগ করতে সমস্যা হয়েছে।');
    } finally {
      setIsSubmitting(false);
    }
  };

  // Toggle active status via Edge Function (with DB fallback)
  const handleToggleActive = async (targetUser: Profile) => {
    if (!profile) return;
    setUpdatingId(targetUser.id);
    setFeedback(null);

    // Rule: Union admin can only toggle operators in own union
    if (profile.role === 'union_admin' && targetUser.role !== 'operator') {
      setFeedback({ type: 'error', text: 'ইউনিয়ন অ্যাডমিন শুধুমাত্র অপারেটরদের স্ট্যাটাস পরিবর্তন করতে পারবেন।' });
      setUpdatingId(null);
      return;
    }

    const nextState = !targetUser.is_active;

    try {
      const { data, error } = await supabase.functions.invoke('create-user', {
        body: {
          action: 'set-active',
          userId: targetUser.id,
          isActive: nextState,
        },
      });

      if (error) {
        // Fallback to direct supabase update
        const { error: directErr } = await supabase
          .from('profiles')
          .update({ is_active: nextState })
          .eq('id', targetUser.id);

        if (directErr) {
          console.error('Toggle active direct error:', directErr);
          setFeedback({
            type: 'error',
            text: 'স্ট্যাটাস পরিবর্তন করার অনুমতি নেই বা RLS পলিসিতে বাঁধা দেওয়া হয়েছে।',
          });
          return;
        }
      }

      setUsers((prev) =>
        prev.map((u) => (u.id === targetUser.id ? { ...u, is_active: nextState } : u))
      );
      setFeedback({
        type: 'success',
        text: data?.message || `${targetUser.full_name}-এর অ্যাকাউন্ট ${nextState ? 'সক্রিয়' : 'নিষ্ক্রিয়'} করা হয়েছে।`,
      });
    } catch (err) {
      console.error('Toggle active exception:', err);
      setFeedback({ type: 'error', text: 'সার্ভারে সমস্যা হয়েছে।' });
    } finally {
      setUpdatingId(null);
    }
  };

  // Open Reset Password Dialog
  const handleOpenResetPassword = (targetUser: Profile) => {
    setResetTargetUser(targetUser);
    setNewPassword('');
    setShowNewPassword(false);
    setResetError(null);
  };

  // Submit Password Reset via Edge Function
  const handleResetPasswordSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!resetTargetUser) return;
    setResetError(null);

    if (!newPassword || newPassword.length < 8) {
      setResetError('নতুন পাসওয়ার্ড কমপক্ষে ৮ অক্ষরের হতে হবে।');
      return;
    }

    setIsResetting(true);
    try {
      const { data, error } = await supabase.functions.invoke('create-user', {
        body: {
          action: 'reset-password',
          userId: resetTargetUser.id,
          newPassword: newPassword,
        },
      });

      if (error) {
        let errMessage = 'পাসওয়ার্ড পরিবর্তন করতে ব্যর্থ হয়েছে।';
        try {
          if ('context' in error && error.context && typeof (error.context as any).json === 'function') {
            const errJson = await (error.context as any).json();
            if (errJson?.error) errMessage = errJson.error;
          } else if (data?.error) {
            errMessage = data.error;
          } else if (error.message) {
            errMessage = error.message;
          }
        } catch {
          if (error.message) errMessage = error.message;
        }
        setResetError(errMessage);
        return;
      }

      if (data?.error) {
        setResetError(data.error);
        return;
      }

      setFeedback({
        type: 'success',
        text: data?.message || `${resetTargetUser.full_name}-এর পাসওয়ার্ড সফলভাবে পরিবর্তন করা হয়েছে।`,
      });
      setResetTargetUser(null);
    } catch (err: any) {
      console.error('Reset password exception:', err);
      setResetError(err?.message || 'সার্ভারে সমস্যা হয়েছে।');
    } finally {
      setIsResetting(false);
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

  const canCreateUser = isSuperAdmin || isUnionAdmin;

  return (
    <AdminLayout>
      {/* Title & Action Buttons */}
      <div className="mb-6 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-xl md:text-2xl font-bold text-slate-800">ইউজার ও কর্মকর্তা ব্যবস্থাপনা</h1>
          <p className="text-xs md:text-sm text-slate-500 mt-0.5">
            মোট ব্যবহারকারী: {toBanglaNumber(users.length)} জন
          </p>
        </div>

        <div className="flex items-center gap-2.5">
          {canCreateUser && (
            <button
              onClick={handleOpenCreateModal}
              className="flex items-center gap-1.5 px-4 py-2 bg-bdGreen-600 hover:bg-bdGreen-700 text-white rounded-xl text-xs font-bold shadow-sm transition"
            >
              <UserPlus className="w-4 h-4" />
              <span>নতুন ইউজার যোগ করুন</span>
            </button>
          )}

          <button
            onClick={fetchUsers}
            className="flex items-center gap-1.5 px-3.5 py-2 bg-white border border-slate-200 text-slate-700 hover:bg-slate-50 rounded-xl text-xs font-semibold shadow-xs transition"
          >
            <RefreshCw className="w-4 h-4 text-slate-500" />
            <span>রিফ্রেশ</span>
          </button>
        </div>
      </div>

      {/* Feedback Toast / Alert */}
      {feedback && (
        <div
          className={`mb-6 p-3.5 rounded-xl text-xs flex items-center justify-between gap-2 transition ${
            feedback.type === 'success'
              ? 'bg-emerald-50 text-emerald-800 border border-emerald-200'
              : 'bg-rose-50 text-rose-800 border border-rose-200'
          }`}
        >
          <div className="flex items-center gap-2">
            {feedback.type === 'success' ? (
              <CheckCircle className="w-4 h-4 shrink-0 text-emerald-600" />
            ) : (
              <AlertCircle className="w-4 h-4 shrink-0 text-rose-600" />
            )}
            <span>{feedback.text}</span>
          </div>
          <button onClick={() => setFeedback(null)} className="text-slate-400 hover:text-slate-600">
            <X className="w-3.5 h-3.5" />
          </button>
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
                  <th className="py-3.5 px-4 font-semibold text-right">অ্যাকশন</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {users.map((u) => {
                  const isUpdating = updatingId === u.id;
                  const canToggle = isSuperAdmin || (profile?.role === 'union_admin' && u.role === 'operator');
                  const canResetPassword = isSuperAdmin || (profile?.role === 'union_admin' && u.role === 'operator');

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
                      <td className="py-3.5 px-4 text-right">
                        {canResetPassword && (
                          <button
                            onClick={() => handleOpenResetPassword(u)}
                            className="inline-flex items-center gap-1 px-2.5 py-1 text-slate-600 hover:text-bdGreen-700 hover:bg-bdGreen-50 rounded-lg border border-slate-200 text-[11px] font-medium transition"
                            title="পাসওয়ার্ড পরিবর্তন করুন"
                          >
                            <KeyRound className="w-3.5 h-3.5" />
                            <span>পাসওয়ার্ড</span>
                          </button>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* CREATE USER MODAL */}
      {isCreateModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-xs animate-in fade-in duration-150">
          <div className="bg-white rounded-2xl border border-slate-200 shadow-xl max-w-lg w-full overflow-hidden">
            <div className="flex items-center justify-between p-4 md:p-5 border-b border-slate-100 bg-slate-50/50">
              <div className="flex items-center gap-2.5">
                <div className="w-9 h-9 rounded-xl bg-bdGreen-50 text-bdGreen-700 flex items-center justify-center">
                  <UserPlus className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="text-base font-bold text-slate-800">নতুন ইউজার তৈরি করুন</h3>
                  <p className="text-xs text-slate-500">
                    {isUnionAdmin
                      ? `${currentUnion?.name_bn || 'আপনার ইউনিয়ন'} এর জন্য নতুন অপারেটর তৈরি করুন`
                      : 'নতুন ইউজার বা অ্যাডমিন অ্যাকাউন্ট তৈরি করুন'}
                  </p>
                </div>
              </div>
              <button
                onClick={() => setIsCreateModalOpen(false)}
                className="text-slate-400 hover:text-slate-600 p-1.5 rounded-lg hover:bg-slate-100 transition"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleCreateUserSubmit} className="p-4 md:p-5 space-y-4">
              {formError && (
                <div className="p-3 bg-rose-50 border border-rose-200 text-rose-800 rounded-xl text-xs flex items-center gap-2">
                  <AlertCircle className="w-4 h-4 shrink-0 text-rose-600" />
                  <span>{formError}</span>
                </div>
              )}

              {/* Full Name */}
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  পূর্ণ নাম <span className="text-rose-500">*</span>
                </label>
                <div className="relative">
                  <User className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
                  <input
                    type="text"
                    required
                    value={fullName}
                    onChange={(e) => setFullName(e.target.value)}
                    placeholder="যেমন: মোঃ রফিকুল ইসলাম"
                    className="w-full text-xs pl-9 pr-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:bg-white focus:outline-none focus:ring-1 focus:ring-bdGreen-600"
                  />
                </div>
              </div>

              {/* Email */}
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  ইমেইল অ্যাড্রেস <span className="text-rose-500">*</span>
                </label>
                <div className="relative">
                  <Mail className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
                  <input
                    type="email"
                    required
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="name@example.com"
                    className="w-full text-xs pl-9 pr-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:bg-white focus:outline-none focus:ring-1 focus:ring-bdGreen-600"
                  />
                </div>
              </div>

              {/* Phone */}
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  মোবাইল নম্বর <span className="text-slate-400 font-normal">(ঐচ্ছিক)</span>
                </label>
                <div className="relative">
                  <Phone className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
                  <input
                    type="tel"
                    value={phone}
                    onChange={(e) => setPhone(e.target.value)}
                    placeholder="০১৭xxxxxxxx"
                    className="w-full text-xs pl-9 pr-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:bg-white focus:outline-none focus:ring-1 focus:ring-bdGreen-600"
                  />
                </div>
              </div>

              {/* Password */}
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  লগইন পাসওয়ার্ড <span className="text-rose-500">*</span>
                </label>
                <div className="relative">
                  <Lock className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
                  <input
                    type={showPassword ? 'text' : 'password'}
                    required
                    minLength={8}
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="কমপক্ষে ৮ অক্ষরের পাসওয়ার্ড"
                    className="w-full text-xs pl-9 pr-10 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:bg-white focus:outline-none focus:ring-1 focus:ring-bdGreen-600"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
                  >
                    {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                  </button>
                </div>
                <p className="text-[11px] text-slate-500 mt-1">
                  ইউজার এই ইমেইল ও পাসওয়ার্ড ব্যবহার করে সিস্টেমে প্রবেশ করবেন।
                </p>
              </div>

              {/* Role Selection */}
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  অ্যাকাউন্ট রোল (Role) <span className="text-rose-500">*</span>
                </label>
                {isUnionAdmin ? (
                  <div className="flex items-center gap-2 p-2.5 bg-slate-100 rounded-xl border border-slate-200 text-slate-700 text-xs font-medium">
                    <span className="font-semibold text-bdGreen-700">অপারেটর (Operator)</span>
                    <span className="text-[11px] text-slate-500">
                      (ইউনিয়ন অ্যাডমিন শুধুমাত্র অপারেটর নিয়োগ করতে পারেন)
                    </span>
                  </div>
                ) : (
                  <select
                    value={selectedRole}
                    onChange={(e) => setSelectedRole(e.target.value as UserRole)}
                    className="w-full text-xs py-2.5 px-3 bg-slate-50 border border-slate-200 rounded-xl focus:bg-white focus:outline-none focus:ring-1 focus:ring-bdGreen-600 font-medium"
                  >
                    <option value="operator">অপারেটর (Operator)</option>
                    <option value="union_admin">ইউনিয়ন অ্যাডমিন (Union Admin)</option>
                    <option value="super_admin">সুপার অ্যাডমিন (Super Admin)</option>
                  </select>
                )}
              </div>

              {/* Union Selection (for Super Admin) */}
              {isSuperAdmin && selectedRole !== 'super_admin' && (
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    ইউনিয়ন পরিষদ <span className="text-rose-500">*</span>
                  </label>
                  <div className="relative">
                    <Building className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
                    <select
                      required
                      value={selectedUnionId}
                      onChange={(e) => setSelectedUnionId(e.target.value)}
                      className="w-full text-xs pl-9 pr-3 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:bg-white focus:outline-none focus:ring-1 focus:ring-bdGreen-600 font-medium"
                    >
                      <option value="">-- ইউনিয়ন পরিষদ নির্বাচন করুন --</option>
                      {unions.map((u) => (
                        <option key={u.id} value={u.id}>
                          {u.name_bn} ({u.upazila}, {u.district})
                        </option>
                      ))}
                    </select>
                  </div>
                </div>
              )}

              {/* Footer Buttons */}
              <div className="pt-2 flex items-center justify-end gap-2.5 border-t border-slate-100">
                <button
                  type="button"
                  disabled={isSubmitting}
                  onClick={() => setIsCreateModalOpen(false)}
                  className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-xl text-xs font-semibold transition"
                >
                  বাতিল
                </button>
                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="flex items-center gap-1.5 px-5 py-2 bg-bdGreen-600 hover:bg-bdGreen-700 text-white rounded-xl text-xs font-bold shadow-sm transition disabled:opacity-50"
                >
                  {isSubmitting ? (
                    <>
                      <RefreshCw className="w-3.5 h-3.5 animate-spin" />
                      <span>তৈরি করা হচ্ছে...</span>
                    </>
                  ) : (
                    <>
                      <UserPlus className="w-3.5 h-3.5" />
                      <span>ইউজার তৈরি করুন</span>
                    </>
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* RESET PASSWORD MODAL */}
      {resetTargetUser && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-xs animate-in fade-in duration-150">
          <div className="bg-white rounded-2xl border border-slate-200 shadow-xl max-w-md w-full overflow-hidden">
            <div className="flex items-center justify-between p-4 md:p-5 border-b border-slate-100 bg-slate-50/50">
              <div className="flex items-center gap-2.5">
                <div className="w-9 h-9 rounded-xl bg-amber-50 text-amber-700 flex items-center justify-center">
                  <KeyRound className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="text-base font-bold text-slate-800">পাসওয়ার্ড পরিবর্তন করুন</h3>
                  <p className="text-xs text-slate-500">ইউজার: {resetTargetUser.full_name}</p>
                </div>
              </div>
              <button
                onClick={() => setResetTargetUser(null)}
                className="text-slate-400 hover:text-slate-600 p-1.5 rounded-lg hover:bg-slate-100 transition"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleResetPasswordSubmit} className="p-4 md:p-5 space-y-4">
              {resetError && (
                <div className="p-3 bg-rose-50 border border-rose-200 text-rose-800 rounded-xl text-xs flex items-center gap-2">
                  <AlertCircle className="w-4 h-4 shrink-0 text-rose-600" />
                  <span>{resetError}</span>
                </div>
              )}

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  নতুন পাসওয়ার্ড <span className="text-rose-500">*</span>
                </label>
                <div className="relative">
                  <Lock className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
                  <input
                    type={showNewPassword ? 'text' : 'password'}
                    required
                    minLength={8}
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    placeholder="কমপক্ষে ৮ অক্ষরের নতুন পাসওয়ার্ড"
                    className="w-full text-xs pl-9 pr-10 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:bg-white focus:outline-none focus:ring-1 focus:ring-bdGreen-600"
                  />
                  <button
                    type="button"
                    onClick={() => setShowNewPassword(!showNewPassword)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
                  >
                    {showNewPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                  </button>
                </div>
              </div>

              <div className="pt-2 flex items-center justify-end gap-2.5 border-t border-slate-100">
                <button
                  type="button"
                  disabled={isResetting}
                  onClick={() => setResetTargetUser(null)}
                  className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-xl text-xs font-semibold transition"
                >
                  বাতিল
                </button>
                <button
                  type="submit"
                  disabled={isResetting}
                  className="flex items-center gap-1.5 px-5 py-2 bg-amber-600 hover:bg-amber-700 text-white rounded-xl text-xs font-bold shadow-sm transition disabled:opacity-50"
                >
                  {isResetting ? (
                    <>
                      <RefreshCw className="w-3.5 h-3.5 animate-spin" />
                      <span>পরিবর্তন হচ্ছে...</span>
                    </>
                  ) : (
                    <>
                      <KeyRound className="w-3.5 h-3.5" />
                      <span>পাসওয়ার্ড সংরক্ষণ করুন</span>
                    </>
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </AdminLayout>
  );
}
