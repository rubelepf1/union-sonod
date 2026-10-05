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
  Printer,
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

        {/* Action Buttons: Print / Delete / Restore */}
        <div className="flex items-center gap-2">
          <button
            onClick={() => window.print()}
            className="px-4 py-2 bg-bdGreen-600 hover:bg-bdGreen-700 text-white rounded-xl text-xs font-bold shadow-xs flex items-center gap-2 transition"
          >
            <Printer className="w-4 h-4" />
            <span>প্রিন্ট সনদ (Print A4)</span>
          </button>

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

      {/* Official Printable Certificate (A4 Sheet layout for print & preview) */}
      {(() => {
        const requiresPhotoList = ['citizenship', 'character', 'unmarried', 'non_remarriage', 'disability', 'freedom_fighter_child', 'permanent_resident'];
        const shouldHavePhoto = requiresPhotoList.includes(cert.type_id) || Boolean((cert.certificate_types as any)?.requires_photo);
        const applicantPhoto = (dataFields as any).applicant_photo_url || (dataFields as any).photo_url || (cert as any).applicant_photo_url;

        return (
          <div className="mt-8 bg-white p-8 md:p-12 rounded-2xl border-2 border-emerald-800 shadow-sm printable-area">
            {/* Top Emblem & Header */}
            <div className="text-center pb-4 border-b-2 border-slate-300 relative">
              {/* Optional Photo on Top Right (ONLY for certificate types that require photo) */}
              {shouldHavePhoto && (
                <div className="absolute top-0 right-0 w-16 h-20 border border-slate-400 bg-slate-50 flex items-center justify-center overflow-hidden rounded">
                  {applicantPhoto ? (
                    <img src={applicantPhoto} alt="ছবি" className="w-full h-full object-cover" />
                  ) : (
                    <span className="text-[10px] text-slate-400 text-center leading-tight">পাসপোর্ট<br />ছবি</span>
                  )}
                </div>
              )}

              {/* Union Parishad Logo / Monogram */}
              {cert.unions?.logo_url ? (
                <img
                  src={cert.unions.logo_url}
                  alt="ইউপি লোগো"
                  className="w-14 h-14 mx-auto rounded-full object-cover border border-emerald-700 mb-1"
                />
              ) : (
                <div className="w-14 h-14 mx-auto rounded-full border-2 border-emerald-700 bg-white flex flex-col items-center justify-center p-1 mb-1 shadow-xs">
                  <div className="w-8 h-8 rounded-full bg-emerald-700 flex items-center justify-center text-white text-[10px] font-bold">
                    ইউপি
                  </div>
                </div>
              )}
              <p className="text-xs font-bold text-emerald-800 tracking-wider">স্থানীয় সরকার বিভাগ</p>
              <h2 className="text-xl md:text-2xl font-bold text-slate-900 mt-1">
                {cert.unions?.name_bn || (dataFields as any).union_name || 'ইউনিয়ন পরিষদ কার্যালয়'}
              </h2>
              <p className="text-xs text-slate-600 mt-0.5">
                উপজেলা: {cert.unions?.upazila || (dataFields as any).upazila || '—'}, জেলা:{' '}
                {cert.unions?.district || (dataFields as any).district || '—'}
              </p>

              <div className="inline-block mt-3 px-4 py-1 rounded-full bg-emerald-50 border border-emerald-300">
                <h1 className="text-base md:text-lg font-extrabold text-emerald-800">
                  {cert.certificate_types?.title_bn || cert.type_id}
                </h1>
              </div>
            </div>

        {/* Serial No and Date */}
        <div className="flex items-center justify-between mt-4 pb-2 border-b border-slate-200 text-xs text-slate-700">
          <div>
            <strong>স্মারক নং:</strong> {cert.serial_no || '—'}
          </div>
          <div>
            <strong>তারিখ:</strong> {(dataFields as any).issue_date_bn || formatBanglaDateTime(cert.created_at).split(' ')[0]}
          </div>
        </div>

        {/* Body Text */}
        <div className="mt-6 text-sm text-slate-800 leading-relaxed text-justify whitespace-pre-line">
          {(dataFields as any).generated_body_text ||
            `এই মর্মে প্রত্যয়ন করা যাইতেছে যে, ${(dataFields as any).applicant_name || 'আবেদনকারী'}, পিতা/স্বামী: ${
              (dataFields as any).father_husband_name || '—'
            }, মাতা: ${(dataFields as any).mother_name || '—'}, গ্রাম: ${
              (dataFields as any).village || '—'
            }, ওয়ার্ড নং: ${(dataFields as any).ward_no || '—'}, ডাকঘর: ${
              (dataFields as any).post_office || '—'
            }, অত্র ইউনিয়নের একজন স্থায়ী বাসিন্দা ও জন্মসূত্রে বাংলাদেশের নাগরিক। তিনি রাষ্ট্রবিরোধী কোনো কার্যকলাপে জড়িত নহেন।`}
        </div>

        {/* Heirs Table (if applicable) */}
        {Array.isArray((dataFields as any).heirs) && (dataFields as any).heirs.length > 0 && (
          <div className="mt-6">
            <h4 className="text-xs font-bold text-slate-700 mb-2">ওয়ারিশগণের তালিকা:</h4>
            <table className="w-full text-left border-collapse border border-slate-300 text-xs">
              <thead className="bg-slate-100">
                <tr>
                  <th className="border border-slate-300 p-2">ক্র.নং</th>
                  <th className="border border-slate-300 p-2">নাম</th>
                  <th className="border border-slate-300 p-2">সম্পর্ক</th>
                  <th className="border border-slate-300 p-2">বয়স</th>
                  <th className="border border-slate-300 p-2">মন্তব্য</th>
                </tr>
              </thead>
              <tbody>
                {(dataFields as any).heirs.map((h: any, idx: number) => (
                  <tr key={idx}>
                    <td className="border border-slate-300 p-2">{toBanglaNumber(idx + 1)}</td>
                    <td className="border border-slate-300 p-2 font-medium">{h.name || h.heirName}</td>
                    <td className="border border-slate-300 p-2">{h.relation}</td>
                    <td className="border border-slate-300 p-2">{toBanglaNumber(h.age || '')}</td>
                    <td className="border border-slate-300 p-2">{h.remarks || '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* Signatures Block */}
        <div className="mt-16 pt-8 flex items-end justify-between text-xs text-slate-800">
          <div className="text-center">
            <div className="w-36 border-t border-slate-400 mb-1 mx-auto"></div>
            <p className="font-bold">সত্যায়নকারী ইউপি সদস্য</p>
            <p className="text-[11px] text-slate-500">স্বাক্ষর ও সিল</p>
          </div>

          <div className="text-center">
            <div className="w-44 border-t border-slate-400 mb-1 mx-auto"></div>
            <p className="font-bold text-sm">
              {cert.unions?.chairman_name || (dataFields as any).chairman_name || 'চেয়ারম্যান'}
            </p>
            <p className="font-semibold text-emerald-800">চেয়ারম্যান</p>
            <p className="text-[11px] text-slate-600">
              {cert.unions?.name_bn || (dataFields as any).union_name || 'ইউনিয়ন পরিষদ'}
            </p>
          </div>
        </div>
      </div>
        );
      })()}

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
