'use client';

import React, { useEffect, useState } from 'react';
import Link from 'next/link';
import { AdminLayout } from '@/components/AdminLayout';
import { useAuth } from '@/components/AuthProvider';
import { supabase } from '@/lib/supabase';
import { CertificateType } from '@/lib/types';
import { toBanglaNumber } from '@/lib/bangla';
import { LoadingSkeleton } from '@/components/LoadingSkeleton';
import { EmptyState } from '@/components/EmptyState';
import {
  Edit3,
  Eye,
  Shield,
} from 'lucide-react';

export default function TemplatesPage() {
  const { profile } = useAuth();
  const [types, setTypes] = useState<CertificateType[]>([]);
  const [loading, setLoading] = useState(true);
  const [updatingId, setUpdatingId] = useState<string | null>(null);

  const isSuperAdmin = profile?.role === 'super_admin';

  const fetchTypes = async () => {
    setLoading(true);
    try {
      const { data, error } = await supabase
        .from('certificate_types')
        .select('*')
        .order('category')
        .order('title_bn');

      if (error) {
        console.error('Fetch types error:', error);
      } else if (data) {
        setTypes(data as CertificateType[]);
      }
    } catch (err) {
      console.error('Types exception:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTypes();
  }, []);

  const handleToggleActive = async (typeItem: CertificateType) => {
    if (!isSuperAdmin) {
      alert('শুধুমাত্র সুপার অ্যাডমিন টেমপ্লেট সক্রিয়/নিষ্ক্রিয় করতে পারবেন।');
      return;
    }

    setUpdatingId(typeItem.id);
    const nextState = !typeItem.is_active;

    try {
      const { error } = await supabase
        .from('certificate_types')
        .update({ is_active: nextState })
        .eq('id', typeItem.id);

      if (error) {
        alert('আপডেট করতে ব্যর্থ হয়েছে।');
      } else {
        setTypes((prev) =>
          prev.map((t) => (t.id === typeItem.id ? { ...t, is_active: nextState } : t))
        );
      }
    } catch (err) {
      console.error('Toggle error:', err);
    } finally {
      setUpdatingId(null);
    }
  };

  return (
    <AdminLayout>
      {/* Title */}
      <div className="mb-6 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h1 className="text-xl md:text-2xl font-bold text-slate-800">সনদপত্র ও প্রত্যয়ন টেমপ্লেট</h1>
          <p className="text-xs md:text-sm text-slate-500 mt-0.5">
            মোট টেমপ্লেট সংখ্যা: {toBanglaNumber(types.length)} টি
          </p>
        </div>

        {!isSuperAdmin && (
          <div className="flex items-center gap-1.5 px-3 py-1.5 bg-amber-50 text-amber-800 border border-amber-200 rounded-xl text-xs font-semibold">
            <Shield className="w-4 h-4 text-amber-600" />
            <span>শুধু রিড-অনলি মোড (এডিটের জন্য সুপার অ্যাডমিন প্রয়োজন)</span>
          </div>
        )}
      </div>

      {loading ? (
        <LoadingSkeleton rows={5} />
      ) : types.length === 0 ? (
        <EmptyState
          title="কোনো সনদ টেমপ্লেট পাওয়া যায়নি"
          description="ডেটাবেসের certificate_types টেবিলে কোনো রেকর্ড বিদ্যমান নেই।"
        />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {types.map((typeItem) => {
            const isUpdating = updatingId === typeItem.id;
            return (
              <div
                key={typeItem.id}
                className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs flex flex-col justify-between hover:border-slate-300 transition"
              >
                <div>
                  <div className="flex items-start justify-between gap-2 mb-2">
                    <span className="text-[11px] font-semibold px-2 py-0.5 rounded-md bg-slate-100 text-slate-600">
                      {typeItem.category || 'নাগরিক সেবা'}
                    </span>
                    <button
                      onClick={() => handleToggleActive(typeItem)}
                      disabled={!isSuperAdmin || isUpdating}
                      className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-bold border ${
                        typeItem.is_active
                          ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                          : 'bg-rose-50 text-rose-700 border-rose-200'
                      } ${!isSuperAdmin ? 'opacity-80 cursor-default' : 'hover:opacity-90'}`}
                      title={isSuperAdmin ? 'ক্লিক করে চালু বা বন্ধ করুন' : 'শুধুমাত্র রিড-অনলি'}
                    >
                      {typeItem.is_active ? 'সক্রিয়' : 'বন্ধ'}
                    </button>
                  </div>

                  <h3 className="font-bold text-base text-slate-800 mb-1">{typeItem.title_bn}</h3>
                  {typeItem.english_name && (
                    <p className="text-xs text-slate-400 font-mono mb-3">{typeItem.english_name}</p>
                  )}

                  <p className="text-xs text-slate-500 line-clamp-3 bg-slate-50 p-2.5 rounded-lg border border-slate-100 font-serif leading-relaxed mb-4">
                    {typeItem.template_bn}
                  </p>
                </div>

                <div className="pt-3 border-t border-slate-100 flex items-center justify-between">
                  <span className="text-[11px] text-slate-400 font-mono">ID: {typeItem.id}</span>
                  <Link
                    href={`/templates/${typeItem.id}`}
                    className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-bold text-bdGreen-700 bg-bdGreen-50 hover:bg-bdGreen-100 rounded-lg transition"
                  >
                    {isSuperAdmin ? (
                      <>
                        <Edit3 className="w-3.5 h-3.5" />
                        <span>সম্পাদনা ও প্রিভিউ</span>
                      </>
                    ) : (
                      <>
                        <Eye className="w-3.5 h-3.5" />
                        <span>টেমপ্লেট প্রিভিউ</span>
                      </>
                    )}
                  </Link>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </AdminLayout>
  );
}
