'use client';

import React, { useEffect, useState } from 'react';
import { AdminLayout } from '@/components/AdminLayout';
import { useAuth } from '@/components/AuthProvider';
import { supabase } from '@/lib/supabase';
import { Union } from '@/lib/types';
import { LoadingSkeleton } from '@/components/LoadingSkeleton';
import {
  Building2,
  Save,
  CheckCircle2,
  AlertCircle,
  Phone,
  Mail,
  MapPin,
  UserCheck,
} from 'lucide-react';

export default function UnionPage() {
  const { profile, union: currentUnion, refreshProfile } = useAuth();
  const [union, setUnion] = useState<Union | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [feedback, setFeedback] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  // Editable fields
  const [nameBn, setNameBn] = useState('');
  const [upazila, setUpazila] = useState('');
  const [district, setDistrict] = useState('');
  const [chairmanName, setChairmanName] = useState('');
  const [phone, setPhone] = useState('');
  const [email, setEmail] = useState('');

  const canEdit = profile?.role === 'super_admin' || profile?.role === 'union_admin';

  useEffect(() => {
    async function loadUnionData() {
      if (!profile) return;
      setLoading(true);
      try {
        let unionId = profile.union_id;
        // If super_admin has no union_id, get the first union
        if (!unionId) {
          const { data: firstUnion } = await supabase.from('unions').select('*').limit(1).single();
          if (firstUnion) unionId = firstUnion.id;
        }

        if (unionId) {
          const { data, error } = await supabase.from('unions').select('*').eq('id', unionId).single();
          if (error) {
            console.error('Fetch union error:', error);
          } else if (data) {
            setUnion(data as Union);
            setNameBn(data.name_bn || '');
            setUpazila(data.upazila || '');
            setDistrict(data.district || '');
            setChairmanName(data.chairman_name || '');
            setPhone(data.phone || '');
            setEmail(data.email || '');
          }
        }
      } catch (err) {
        console.error('Union exception:', err);
      } finally {
        setLoading(false);
      }
    }

    loadUnionData();
  }, [profile]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!union || !canEdit) return;

    setSaving(true);
    setFeedback(null);

    try {
      const { error } = await supabase
        .from('unions')
        .update({
          name_bn: nameBn.trim(),
          upazila: upazila.trim(),
          district: district.trim(),
          chairman_name: chairmanName.trim(),
          phone: phone.trim() || null,
          email: email.trim() || null,
        })
        .eq('id', union.id);

      if (error) {
        console.error('Save union error:', error);
        setFeedback({ type: 'error', text: 'তথ্য সংরক্ষণ ব্যর্থ হয়েছে বা অনুমতি নেই।' });
      } else {
        setFeedback({ type: 'success', text: 'ইউনিয়ন পরিষদের তথ্যাবলী সফলভাবে আপডেট করা হয়েছে।' });
        await refreshProfile();
      }
    } catch (err) {
      console.error('Union save exception:', err);
      setFeedback({ type: 'error', text: 'সার্ভারে সমস্যা হয়েছে।' });
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <AdminLayout>
        <LoadingSkeleton rows={5} />
      </AdminLayout>
    );
  }

  return (
    <AdminLayout>
      {/* Title */}
      <div className="mb-6">
        <h1 className="text-xl md:text-2xl font-bold text-slate-800">ইউনিয়ন পরিষদ পরিচিতি ও তথ্য</h1>
        <p className="text-xs md:text-sm text-slate-500 mt-0.5">
          সনদের হেডার ও অফিসিয়াল নথিতে প্রদর্শনের জন্য ইউনিয়ন তথ্য
        </p>
      </div>

      {feedback && (
        <div
          className={`mb-6 p-4 rounded-xl text-xs flex items-center gap-2 ${
            feedback.type === 'success'
              ? 'bg-emerald-50 text-emerald-800 border border-emerald-200'
              : 'bg-rose-50 text-rose-800 border border-rose-200'
          }`}
        >
          {feedback.type === 'success' ? (
            <CheckCircle2 className="w-5 h-5 shrink-0 text-emerald-600" />
          ) : (
            <AlertCircle className="w-5 h-5 shrink-0 text-rose-600" />
          )}
          <span>{feedback.text}</span>
        </div>
      )}

      <div className="max-w-2xl bg-white p-6 md:p-8 rounded-2xl border border-slate-200 shadow-xs">
        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase mb-2">
              ইউনিয়ন পরিষদের নাম (বাংলায়)
            </label>
            <div className="relative">
              <Building2 className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
              <input
                type="text"
                disabled={!canEdit}
                value={nameBn}
                onChange={(e) => setNameBn(e.target.value)}
                required
                placeholder="যেমন: ১নং পলাশ ইউনিয়ন পরিষদ"
                className="w-full pl-10 pr-4 py-2.5 text-xs md:text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-bdGreen-600 disabled:opacity-60 font-semibold"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase mb-2">উপজেলা</label>
              <div className="relative">
                <MapPin className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
                <input
                  type="text"
                  disabled={!canEdit}
                  value={upazila}
                  onChange={(e) => setUpazila(e.target.value)}
                  required
                  placeholder="উপজেলা"
                  className="w-full pl-10 pr-4 py-2.5 text-xs md:text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-bdGreen-600 disabled:opacity-60"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase mb-2">জেলা</label>
              <div className="relative">
                <MapPin className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
                <input
                  type="text"
                  disabled={!canEdit}
                  value={district}
                  onChange={(e) => setDistrict(e.target.value)}
                  required
                  placeholder="জেলা"
                  className="w-full pl-10 pr-4 py-2.5 text-xs md:text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-bdGreen-600 disabled:opacity-60"
                />
              </div>
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase mb-2">
              বর্তমান চেয়ারম্যানের নাম
            </label>
            <div className="relative">
              <UserCheck className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
              <input
                type="text"
                disabled={!canEdit}
                value={chairmanName}
                onChange={(e) => setChairmanName(e.target.value)}
                required
                placeholder="চেয়ারম্যানের পূর্ণ নাম"
                className="w-full pl-10 pr-4 py-2.5 text-xs md:text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-bdGreen-600 disabled:opacity-60"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase mb-2">
                যোগাযোগের মোবাইল নম্বর
              </label>
              <div className="relative">
                <Phone className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
                <input
                  type="text"
                  disabled={!canEdit}
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  placeholder="০১৭xxxxxxxx"
                  className="w-full pl-10 pr-4 py-2.5 text-xs md:text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-bdGreen-600 disabled:opacity-60 font-mono"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase mb-2">
                অফিসিয়াল ইমেইল
              </label>
              <div className="relative">
                <Mail className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
                <input
                  type="email"
                  disabled={!canEdit}
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="union@gov.bd"
                  className="w-full pl-10 pr-4 py-2.5 text-xs md:text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-bdGreen-600 disabled:opacity-60 font-mono"
                />
              </div>
            </div>
          </div>

          {canEdit && (
            <div className="pt-4 border-t border-slate-100">
              <button
                type="submit"
                disabled={saving}
                className="w-full py-3 bg-bdGreen-600 hover:bg-bdGreen-700 text-white rounded-xl font-bold text-xs md:text-sm shadow-md transition flex items-center justify-center gap-2 cursor-pointer disabled:opacity-60"
              >
                <Save className="w-4 h-4" />
                <span>{saving ? 'সংরক্ষণ করা হচ্ছে...' : 'তথ্য সংরক্ষণ করুন'}</span>
              </button>
            </div>
          )}
        </form>
      </div>
    </AdminLayout>
  );
}
