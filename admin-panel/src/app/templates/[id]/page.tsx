'use client';

import React, { useEffect, useState, useMemo } from 'react';
import { useParams } from 'next/navigation';
import Link from 'next/link';
import { AdminLayout } from '@/components/AdminLayout';
import { useAuth } from '@/components/AuthProvider';
import { supabase } from '@/lib/supabase';
import { CertificateType } from '@/lib/types';
import { LoadingSkeleton } from '@/components/LoadingSkeleton';
import {
  ArrowLeft,
  Save,
  AlertTriangle,
  Eye,
  CheckCircle2,
  Code2,
  Shield,
} from 'lucide-react';

const sampleData: Record<string, string> = {
  applicant_name: 'মোহাম্মদ আব্দুল করিম',
  father_name: 'মৃত মোঃ রফিকুল ইসলাম',
  mother_name: 'মোছাঃ জাহানারা বেগম',
  spouse_name: 'মোছাঃ রোকসানা আক্তার',
  village: 'চর ভবানীপুর',
  ward_no: '০৩',
  post_office: 'মাদবদী',
  upazila: 'পলাশ',
  district: 'নরসিংদী',
  nid_number: '১৯৮৮১২২৩৩৪৫৫০০১২৩',
  death_date: '১২ জুন, ২০২৪',
  annual_income: '১,৫০,০০০',
  religion: 'ইসলাম',
  occupation: 'কৃষি',
};

export default function TemplateEditPage() {
  const params = useParams();
  const id = params?.id as string;
  const { profile } = useAuth();

  const [typeItem, setTypeItem] = useState<CertificateType | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [feedback, setFeedback] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  // Form states
  const [titleBn, setTitleBn] = useState('');
  const [category, setCategory] = useState('');
  const [templateBn, setTemplateBn] = useState('');
  const [isActive, setIsActive] = useState(true);

  const isSuperAdmin = profile?.role === 'super_admin';

  useEffect(() => {
    async function fetchType() {
      if (!id) return;
      setLoading(true);
      try {
        const { data, error } = await supabase
          .from('certificate_types')
          .select('*')
          .eq('id', id)
          .single();

        if (error || !data) {
          console.error('Fetch type error:', error);
        } else {
          setTypeItem(data as CertificateType);
          setTitleBn(data.title_bn || '');
          setCategory(data.category || 'নাগরিক সেবা');
          setTemplateBn(data.template_bn || '');
          setIsActive(data.is_active ?? true);
        }
      } catch (err) {
        console.error('Exception fetching type:', err);
      } finally {
        setLoading(false);
      }
    }
    fetchType();
  }, [id]);

  // Extract all {placeholders} from template_bn
  const detectedPlaceholders = useMemo(() => {
    const regex = /\{([a-zA-Z0-9_]+)\}/g;
    const matches = new Set<string>();
    let match;
    while ((match = regex.exec(templateBn)) !== null) {
      matches.add(match[1]);
    }
    return Array.from(matches);
  }, [templateBn]);

  // Live preview replacement
  const livePreviewText = useMemo(() => {
    let result = templateBn;
    detectedPlaceholders.forEach((key) => {
      const sample = sampleData[key] || `[${key}]`;
      result = result.replace(new RegExp(`\\{${key}\\}`, 'g'), sample);
    });
    return result;
  }, [templateBn, detectedPlaceholders]);

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!isSuperAdmin) {
      alert('শুধুমাত্র সুপার অ্যাডমিন টেমপ্লেট পরিবর্তন করতে পারবেন।');
      return;
    }

    const confirmed = window.confirm(
      'সতর্কতা: টেমপ্লেট বদলালে নতুন তৈরি করা সকল সনদে প্রভাব পড়বে। আপনি কি সংরক্ষণ করতে চান?'
    );
    if (!confirmed) return;

    setSaving(true);
    setFeedback(null);

    try {
      const { error } = await supabase
        .from('certificate_types')
        .update({
          title_bn: titleBn.trim(),
          category: category.trim(),
          template_bn: templateBn.trim(),
          is_active: isActive,
        })
        .eq('id', id);

      if (error) {
        console.error('Save template error:', error);
        setFeedback({ type: 'error', text: 'টেমপ্লেট সংরক্ষণ ব্যর্থ হয়েছে।' });
      } else {
        setFeedback({ type: 'success', text: 'টেমপ্লেট সফলভাবে সংরক্ষিত হয়েছে।' });
      }
    } catch (err) {
      console.error('Save exception:', err);
      setFeedback({ type: 'error', text: 'সার্ভার ত্রুটি।' });
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <AdminLayout>
        <LoadingSkeleton rows={6} />
      </AdminLayout>
    );
  }

  if (!typeItem) {
    return (
      <AdminLayout>
        <div className="bg-white p-8 rounded-2xl text-center border border-slate-200">
          <p className="text-slate-500 mb-4">টেমপ্লেট খুঁজে পাওয়া যায়নি।</p>
          <Link
            href="/templates"
            className="px-4 py-2 bg-bdGreen-600 text-white rounded-xl text-xs font-semibold"
          >
            টেমপ্লেট তালিকায় ফিরে যান
          </Link>
        </div>
      </AdminLayout>
    );
  }

  return (
    <AdminLayout>
      {/* Back button */}
      <div className="mb-6 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <Link
          href="/templates"
          className="inline-flex items-center gap-2 text-xs font-bold text-slate-600 hover:text-bdGreen-700 bg-white border border-slate-200 px-3 py-2 rounded-xl shadow-xs transition w-fit"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>টেমপ্লেট তালিকায় ফিরে যান</span>
        </Link>

        {!isSuperAdmin && (
          <div className="flex items-center gap-1.5 px-3 py-1.5 bg-amber-50 text-amber-800 border border-amber-200 rounded-xl text-xs font-semibold">
            <Shield className="w-4 h-4 text-amber-600" />
            <span>শুধু রিড-অনলি মোড</span>
          </div>
        )}
      </div>

      {/* Warning banner */}
      <div className="mb-6 p-4 bg-amber-50 border border-amber-200 rounded-2xl flex items-center gap-3 text-amber-900 text-xs">
        <AlertTriangle className="w-5 h-5 text-amber-600 shrink-0" />
        <span className="font-semibold">
          সতর্কতা: টেমপ্লেট বদলালে নতুন সনদে প্রভাব পড়বে। পূর্ববর্তী ইস্যুকৃত সনদের বডি অপরিবর্তিত থাকবে।
        </span>
      </div>

      {feedback && (
        <div
          className={`mb-6 p-3.5 rounded-xl text-xs flex items-center gap-2 ${
            feedback.type === 'success'
              ? 'bg-emerald-50 text-emerald-800 border border-emerald-200'
              : 'bg-rose-50 text-rose-800 border border-rose-200'
          }`}
        >
          <CheckCircle2 className="w-4 h-4 shrink-0 text-emerald-600" />
          <span>{feedback.text}</span>
        </div>
      )}

      {/* Edit and Preview Columns */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Left Column: Form Editor */}
        <form onSubmit={handleSave} className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs space-y-4">
          <div className="flex items-center justify-between border-b border-slate-100 pb-3 mb-2">
            <h2 className="font-bold text-base text-slate-800">টেমপ্লেট কনফিগারেশন</h2>
            <span className="text-xs font-mono text-slate-400">ID: {typeItem.id}</span>
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
              সনদের শিরোনাম (বাংলা)
            </label>
            <input
              type="text"
              disabled={!isSuperAdmin}
              value={titleBn}
              onChange={(e) => setTitleBn(e.target.value)}
              required
              className="w-full py-2.5 px-3 text-xs md:text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-bdGreen-600 disabled:opacity-60 font-semibold"
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                ক্যাটাগরি
              </label>
              <input
                type="text"
                disabled={!isSuperAdmin}
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                className="w-full py-2 px-3 text-xs bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-bdGreen-600 disabled:opacity-60"
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                স্ট্যাটাস
              </label>
              <label className="flex items-center gap-2 mt-2 cursor-pointer text-xs font-bold text-slate-700">
                <input
                  type="checkbox"
                  disabled={!isSuperAdmin}
                  checked={isActive}
                  onChange={(e) => setIsActive(e.target.checked)}
                  className="rounded text-bdGreen-600 focus:ring-bdGreen-500 w-4 h-4 disabled:opacity-60"
                />
                <span>{isActive ? 'টেমপ্লেট সক্রিয় (Active)' : 'নিষ্ক্রিয় (Disabled)'}</span>
              </label>
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
              সনদের মূল বয়ান (টেমপ্লেট টেক্সট)
            </label>
            <textarea
              rows={9}
              disabled={!isSuperAdmin}
              value={templateBn}
              onChange={(e) => setTemplateBn(e.target.value)}
              required
              className="w-full p-3 text-xs md:text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-bdGreen-600 font-serif leading-relaxed disabled:opacity-60"
            />
          </div>

          {/* Placeholders list */}
          <div>
            <div className="flex items-center gap-1.5 text-xs font-bold text-slate-700 mb-2">
              <Code2 className="w-4 h-4 text-bdGreen-600" />
              <span>শনাক্তকৃত প্লেসহোল্ডারসমূহ ({detectedPlaceholders.length} টি):</span>
            </div>
            <div className="flex flex-wrap gap-1.5 p-3 bg-slate-50 rounded-xl border border-slate-100">
              {detectedPlaceholders.length > 0 ? (
                detectedPlaceholders.map((ph) => (
                  <span
                    key={ph}
                    className="px-2 py-1 bg-white border border-slate-200 rounded-md font-mono text-[11px] font-bold text-bdGreen-700"
                  >
                    {`{${ph}}`}
                  </span>
                ))
              ) : (
                <span className="text-xs text-slate-400">কোনো {`{প্লেসহোল্ডার}`} পাওয়া যায়নি</span>
              )}
            </div>
          </div>

          {isSuperAdmin && (
            <button
              type="submit"
              disabled={saving}
              className="w-full py-3 bg-bdGreen-600 hover:bg-bdGreen-700 text-white rounded-xl font-bold text-xs shadow-md transition flex items-center justify-center gap-2 cursor-pointer disabled:opacity-60"
            >
              <Save className="w-4 h-4" />
              <span>{saving ? 'সংরক্ষণ করা হচ্ছে...' : 'টেমপ্লেট সংরক্ষণ করুন'}</span>
            </button>
          )}
        </form>

        {/* Right Column: Live Preview */}
        <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs flex flex-col">
          <div className="flex items-center gap-2 border-b border-slate-100 pb-3 mb-4">
            <Eye className="w-5 h-5 text-bdGreen-600" />
            <h2 className="font-bold text-base text-slate-800">লাইভ প্রিভিউ (নমুনা ডেটা সহ)</h2>
          </div>

          <div className="flex-1 bg-amber-50/30 p-6 rounded-2xl border border-amber-100/60 font-serif flex flex-col justify-between">
            <div>
              <div className="text-center mb-6 border-b border-slate-200 pb-4">
                <h3 className="font-bold text-lg text-slate-900">{titleBn || 'সনদের নাম'}</h3>
                <p className="text-xs text-slate-500 font-sans mt-0.5">ইউনিয়ন পরিষদ কার্যালয়</p>
              </div>

              <div className="text-slate-800 text-sm md:text-base leading-loose whitespace-pre-line text-justify">
                {livePreviewText}
              </div>
            </div>

            <div className="mt-10 pt-6 border-t border-dashed border-slate-300 flex justify-between items-end text-xs font-sans text-slate-500">
              <div>
                <p>স্মারক: ইউপি/২০২৬/০০১২</p>
                <p>তারিখ: ০২ অক্টোবর, ২০২৬</p>
              </div>
              <div className="text-center">
                <div className="w-24 border-b border-slate-400 mb-1" />
                <p className="font-bold text-slate-800">চেয়ারম্যান</p>
                <p>ইউনিয়ন পরিষদ</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </AdminLayout>
  );
}
