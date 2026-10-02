'use client';

import React, { useEffect, useState, useCallback } from 'react';
import { useParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import { AdminLayout } from '@/components/AdminLayout';
import { supabase } from '@/lib/supabase';
import { Certificate, CertificateStatus } from '@/lib/types';
import { toBanglaNumber, formatBanglaDateTime } from '@/lib/bangla';
import { StatusBadge } from '@/components/StatusBadge';
import { LoadingSkeleton } from '@/components/LoadingSkeleton';
import {
  ArrowLeft,
  Trash2,
  RefreshCw,
  CheckCircle2,
  AlertTriangle,
  Calendar,
  User,
  Hash,
  FileText,
} from 'lucide-react';

export default function CertificateDetailPage() {
  const params = useParams();
  const router = useRouter();
  const id = params?.id as string;

  const [cert, setCert] = useState<Certificate | null>(null);
  const [loading, setLoading] = useState(true);
  const [updatingStatus, setUpdatingStatus] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);
  const [feedbackMsg, setFeedbackMsg] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  const fetchCertificateDetail = useCallback(async () => {
    if (!id) return;
    setLoading(true);
    try {
      const { data, error } = await supabase
        .from('certificates')
        .select('*, certificate_types(*), profiles(*), unions(*)')
        .eq('id', id)
        .single();

      if (error || !data) {
        console.error('Fetch cert detail error:', error);
      } else {
        setCert(data as Certificate);
      }
    } catch (err) {
      console.error('Exception fetching certificate:', err);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchCertificateDetail();
  }, [fetchCertificateDetail]);

  const handleStatusChange = async (newStatus: CertificateStatus) => {
    if (!cert) return;
    setUpdatingStatus(true);
    setFeedbackMsg(null);
    try {
      const { error } = await supabase
        .from('certificates')
        .update({ status: newStatus, updated_at: new Date().toISOString() })
        .eq('id', cert.id);

      if (error) {
        console.error('Status update failed:', error);
        setFeedbackMsg({ type: 'error', text: 'স্ট্যাটাস পরিবর্তন ব্যর্থ হয়েছে।' });
      } else {
        setCert({ ...cert, status: newStatus });
        setFeedbackMsg({ type: 'success', text: 'স্ট্যাটাস সফলভাবে আপডেট করা হয়েছে।' });
      }
    } catch (err) {
      console.error('Status exception:', err);
      setFeedbackMsg({ type: 'error', text: 'সার্ভারে সমস্যা হয়েছে।' });
    } finally {
      setUpdatingStatus(false);
    }
  };

  const handleSoftDelete = async () => {
    if (!cert) return;
    setActionLoading(true);
    try {
      const { error } = await supabase
        .from('certificates')
        .update({ deleted_at: new Date().toISOString() })
        .eq('id', cert.id);

      if (error) {
        alert('মুছে ফেলতে সমস্যা হয়েছে।');
      } else {
        setShowDeleteModal(false);
        router.push('/certificates');
      }
    } catch (err) {
      console.error('Delete error:', err);
    } finally {
      setActionLoading(false);
    }
  };

  const handleRestore = async () => {
    if (!cert) return;
    setActionLoading(true);
    try {
      const { error } = await supabase
        .from('certificates')
        .update({ deleted_at: null })
        .eq('id', cert.id);

      if (error) {
        alert('পুনরুদ্ধার করতে সমস্যা হয়েছে।');
      } else {
        await fetchCertificateDetail();
        setFeedbackMsg({ type: 'success', text: 'সনদটি সফলভাবে পুনরুদ্ধার করা হয়েছে।' });
      }
    } catch (err) {
      console.error('Restore error:', err);
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) {
    return (
      <AdminLayout>
        <LoadingSkeleton rows={6} />
      </AdminLayout>
    );
  }

  if (!cert) {
    return (
      <AdminLayout>
        <div className="bg-white p-8 rounded-2xl text-center border border-slate-200">
          <p className="text-slate-500 mb-4">সনদপত্রটি খুঁজে পাওয়া যায়নি।</p>
          <Link
            href="/certificates"
            className="px-4 py-2 bg-bdGreen-600 text-white rounded-xl text-xs font-semibold"
          >
            তালিকায় ফিরে যান
          </Link>
        </div>
      </AdminLayout>
    );
  }

  const isDeleted = cert.deleted_at !== null;
  const dataFields = cert.data || {};

  return (
    <AdminLayout>
      {/* Top Navigation */}
      <div className="mb-6 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <Link
          href="/certificates"
          className="inline-flex items-center gap-2 text-xs font-bold text-slate-600 hover:text-bdGreen-700 bg-white border border-slate-200 px-3 py-2 rounded-xl shadow-xs transition w-fit"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>সনদ তালিকায় ফিরে যান</span>
        </Link>

        {/* Action Buttons: Delete / Restore */}
        <div className="flex items-center gap-2">
          {isDeleted ? (
            <button
              onClick={handleRestore}
              disabled={actionLoading}
              className="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-bold shadow-xs flex items-center gap-2"
            >
              <RefreshCw className="w-4 h-4" />
              <span>পুনরুদ্ধার করুন</span>
            </button>
          ) : (
            <button
              onClick={() => setShowDeleteModal(true)}
              className="px-4 py-2 bg-bdRed-50 text-bdRed-600 hover:bg-bdRed-100 rounded-xl text-xs font-bold flex items-center gap-2 border border-bdRed-100"
            >
              <Trash2 className="w-4 h-4" />
              <span>মুছুন (Soft Delete)</span>
            </button>
          )}
        </div>
      </div>

      {/* Deleted Warning Alert */}
      {isDeleted && (
        <div className="mb-6 p-4 bg-rose-50 border border-rose-200 rounded-2xl flex items-center gap-3 text-rose-800 text-xs">
          <AlertTriangle className="w-5 h-5 shrink-0" />
          <span>
            এই সনদটি সফট-ডিলিট অবস্থায় রয়েছে ({formatBanglaDateTime(cert.deleted_at)} তারিখে মুছে ফেলা হয়েছিল)।
          </span>
        </div>
      )}

      {/* Feedback Alert */}
      {feedbackMsg && (
        <div
          className={`mb-6 p-3.5 rounded-xl text-xs flex items-center gap-2 ${
            feedbackMsg.type === 'success'
              ? 'bg-emerald-50 text-emerald-800 border border-emerald-200'
              : 'bg-rose-50 text-rose-800 border border-rose-200'
          }`}
        >
          <CheckCircle2 className="w-4 h-4 shrink-0" />
          <span>{feedbackMsg.text}</span>
        </div>
      )}

      {/* Main Details Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column (2 spans): Certificate Data JSONB Fields */}
        <div className="lg:col-span-2 space-y-6">
          <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs">
            <div className="flex items-center justify-between pb-4 border-b border-slate-100 mb-6">
              <div>
                <span className="text-xs font-mono font-bold text-slate-500">
                  স্মারক: {cert.serial_no || 'অনির্ধারিত'}
                </span>
                <h2 className="text-xl font-bold text-slate-800 mt-1">
                  {cert.certificate_types?.title_bn || cert.type_id}
                </h2>
              </div>
              <StatusBadge status={cert.status} />
            </div>

            {/* Render all entries from jsonb data */}
            <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-4">
              আবেদনকারী ও সনদের তথ্যাবলী
            </h3>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {Object.entries(dataFields).map(([key, val]) => {
                if (key === 'heirs' && Array.isArray(val)) {
                  return (
                    <div key={key} className="col-span-full mt-2 p-4 bg-slate-50 rounded-xl border border-slate-200">
                      <h4 className="text-xs font-bold text-slate-700 mb-2">ওয়ারিশগণের তালিকা:</h4>
                      <div className="divide-y divide-slate-200 text-xs">
                        {val.map((heir: any, hIdx: number) => (
                          <div key={hIdx} className="py-2 flex items-center justify-between">
                            <span className="font-semibold text-slate-800">
                              {toBanglaNumber(hIdx + 1)}. {heir.name || heir.heirName}
                            </span>
                            <span className="text-slate-500">সম্পর্ক: {heir.relation}</span>
                            <span className="text-slate-500">বয়স: {toBanglaNumber(heir.age)}</span>
                          </div>
                        ))}
                      </div>
                    </div>
                  );
                }

                const displayValue = typeof val === 'object' ? JSON.stringify(val) : String(val);
                return (
                  <div key={key} className="p-3.5 bg-slate-50 rounded-xl border border-slate-100">
                    <p className="text-[11px] font-semibold text-slate-400 mb-1 capitalize">
                      {key.replace(/_/g, ' ')}
                    </p>
                    <p className="text-sm font-bold text-slate-800 break-words">{displayValue || '—'}</p>
                  </div>
                );
              })}
            </div>
          </div>
        </div>

        {/* Right Column: Metadata & Status Controls */}
        <div className="space-y-6">
          {/* Status Change Card */}
          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
            <h3 className="font-bold text-sm text-slate-800 mb-3">সনদের স্ট্যাটাস পরিবর্তন</h3>
            <div className="space-y-3">
              <select
                value={cert.status}
                disabled={updatingStatus}
                onChange={(e) => handleStatusChange(e.target.value as CertificateStatus)}
                className="w-full py-2.5 px-3 bg-slate-50 border border-slate-200 rounded-xl text-xs font-bold text-slate-800 focus:outline-none focus:ring-2 focus:ring-bdGreen-600"
              >
                <option value="draft">খসড়া (Draft)</option>
                <option value="generated">ইস্যুকৃত (Generated)</option>
                <option value="printed">প্রিন্টকৃত (Printed)</option>
                <option value="signed">স্বাক্ষরিত (Signed)</option>
                <option value="rejected">বাতিলকৃত (Rejected)</option>
              </select>
              <p className="text-[11px] text-slate-400 leading-normal">
                স্বাক্ষরিত বা প্রিন্ট হওয়ার পর স্ট্যাটাস হালনাগাদ করে রাখুন যাতে স্বচ্ছতা নিশ্চিত থাকে।
              </p>
            </div>
          </div>

          {/* Audit / Metadata Info Card */}
          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs space-y-4 text-xs">
            <h3 className="font-bold text-sm text-slate-800 border-b border-slate-100 pb-2">
              সিস্টেম মেটাডাটা
            </h3>

            <div className="flex items-center gap-3 text-slate-600">
              <Hash className="w-4 h-4 text-slate-400 shrink-0" />
              <div>
                <p className="text-[11px] text-slate-400">সনদ আইডি</p>
                <p className="font-mono font-bold text-[11px] text-slate-700 truncate">{cert.id}</p>
              </div>
            </div>

            <div className="flex items-center gap-3 text-slate-600">
              <User className="w-4 h-4 text-slate-400 shrink-0" />
              <div>
                <p className="text-[11px] text-slate-400">প্রস্তুতকারী কর্মকর্তা</p>
                <p className="font-semibold text-slate-800">{cert.profiles?.full_name || 'ইউজার'}</p>
              </div>
            </div>

            <div className="flex items-center gap-3 text-slate-600">
              <Calendar className="w-4 h-4 text-slate-400 shrink-0" />
              <div>
                <p className="text-[11px] text-slate-400">ইস্যুর তারিখ ও সময়</p>
                <p className="font-semibold text-slate-800">{formatBanglaDateTime(cert.created_at)}</p>
              </div>
            </div>

            {cert.unions && (
              <div className="flex items-center gap-3 text-slate-600">
                <FileText className="w-4 h-4 text-slate-400 shrink-0" />
                <div>
                  <p className="text-[11px] text-slate-400">ইউনিয়ন পরিষদ</p>
                  <p className="font-semibold text-slate-800">{cert.unions.name_bn}</p>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Delete Confirm Modal */}
      {showDeleteModal && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-xs flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-2xl max-w-sm w-full p-6 text-center shadow-xl border border-slate-100">
            <div className="w-12 h-12 rounded-full bg-bdRed-50 text-bdRed-600 flex items-center justify-center mx-auto mb-4">
              <Trash2 className="w-6 h-6" />
            </div>
            <h3 className="font-bold text-base text-slate-800 mb-2">সনদটি মুছে ফেলতে চান?</h3>
            <p className="text-xs text-slate-500 mb-6 leading-relaxed">
              এই সনদে `deleted_at` ফিল্ড যুক্ত হবে এবং সাধারণ তালিকা থেকে লুকানো থাকবে। কোনো ডেটা চিরতরে মুছে যাবে
              না।
            </p>
            <div className="flex items-center justify-center gap-3">
              <button
                onClick={() => setShowDeleteModal(false)}
                disabled={actionLoading}
                className="px-4 py-2 border border-slate-200 text-slate-600 rounded-xl text-xs font-semibold hover:bg-slate-50 flex-1"
              >
                বাতিল
              </button>
              <button
                onClick={handleSoftDelete}
                disabled={actionLoading}
                className="px-4 py-2 bg-bdRed-600 text-white rounded-xl text-xs font-bold hover:bg-bdRed-700 flex-1 shadow-sm"
              >
                {actionLoading ? 'অপেক্ষা করুন...' : 'হ্যাঁ, মুছুন'}
              </button>
            </div>
          </div>
        </div>
      )}
    </AdminLayout>
  );
}
