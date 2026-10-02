'use client';

import React, { useEffect, useState, useMemo, useCallback } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { AdminLayout } from '@/components/AdminLayout';
import { useAuth } from '@/components/AuthProvider';
import { supabase } from '@/lib/supabase';
import { Certificate, CertificateType, Profile } from '@/lib/types';
import { toBanglaNumber, formatBanglaDate } from '@/lib/bangla';
import { StatusBadge } from '@/components/StatusBadge';
import { EmptyState } from '@/components/EmptyState';
import {
  Search,
  Download,
  Eye,
  Trash2,
  RefreshCw,
  ChevronLeft,
  ChevronRight,
  User,
  Calendar,
} from 'lucide-react';

export default function CertificatesPage() {
  const router = useRouter();
  const { profile } = useAuth();
  const [certificates, setCertificates] = useState<Certificate[]>([]);
  const [types, setTypes] = useState<CertificateType[]>([]);
  const [users, setUsers] = useState<Profile[]>([]);
  const [loading, setLoading] = useState(true);

  // Filters & Search
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedType, setSelectedType] = useState('all');
  const [selectedStatus, setSelectedStatus] = useState('all');
  const [selectedUser, setSelectedUser] = useState('all');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [showDeleted, setShowDeleted] = useState(false);

  // Pagination
  const [currentPage, setCurrentPage] = useState(1);
  const pageSize = 20;

  // Confirm delete modal
  const [deleteTargetId, setDeleteTargetId] = useState<string | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  // Load filter dropdown options
  useEffect(() => {
    async function loadMeta() {
      if (!profile) return;
      try {
        const { data: tData } = await supabase.from('certificate_types').select('*').order('title_bn');
        if (tData) setTypes(tData);

        let uQuery = supabase.from('profiles').select('*').order('full_name');
        if (profile.role !== 'super_admin' && profile.union_id) {
          uQuery = uQuery.eq('union_id', profile.union_id);
        }
        const { data: uData } = await uQuery;
        if (uData) setUsers(uData);
      } catch (err) {
        console.error('Error loading meta:', err);
      }
    }
    loadMeta();
  }, [profile]);

  // Fetch certificates
  const fetchCertificates = useCallback(async () => {
    if (!profile) return;
    setLoading(true);

    try {
      let query = supabase
        .from('certificates')
        .select('*, certificate_types(*), profiles(*)')
        .order('created_at', { ascending: false });

      if (profile.role !== 'super_admin' && profile.union_id) {
        query = query.eq('union_id', profile.union_id);
      }

      // Soft delete filter:
      if (showDeleted) {
        query = query.not('deleted_at', 'is', null);
      } else {
        query = query.is('deleted_at', null);
      }

      if (selectedType !== 'all') {
        query = query.eq('type_id', selectedType);
      }

      if (selectedStatus !== 'all') {
        query = query.eq('status', selectedStatus);
      }

      if (selectedUser !== 'all') {
        query = query.eq('created_by', selectedUser);
      }

      if (startDate) {
        query = query.gte('created_at', new Date(startDate).toISOString());
      }
      if (endDate) {
        const end = new Date(endDate);
        end.setHours(23, 59, 59, 999);
        query = query.lte('created_at', end.toISOString());
      }

      const { data, error } = await query;
      if (error) {
        console.error('Failed to fetch certificates:', error);
      } else if (data) {
        setCertificates(data as Certificate[]);
      }
    } catch (err) {
      console.error('Exception fetching certificates:', err);
    } finally {
      setLoading(false);
    }
  }, [profile, showDeleted, selectedType, selectedStatus, selectedUser, startDate, endDate]);

  useEffect(() => {
    fetchCertificates();
  }, [fetchCertificates]);

  // Client-side search by applicant name or serial_no
  const filteredCertificates = useMemo(() => {
    if (!searchTerm.trim()) return certificates;
    const term = searchTerm.toLowerCase().trim();
    return certificates.filter((c) => {
      const serial = (c.serial_no || '').toLowerCase();
      const applicant = (c.data?.applicant_name || c.data?.applicantName || '').toLowerCase();
      return serial.includes(term) || applicant.includes(term);
    });
  }, [certificates, searchTerm]);

  // Paginated records
  const totalPages = Math.ceil(filteredCertificates.length / pageSize) || 1;
  const paginatedCertificates = useMemo(() => {
    const start = (currentPage - 1) * pageSize;
    return filteredCertificates.slice(start, start + pageSize);
  }, [filteredCertificates, currentPage]);

  // Soft delete handler: UPDATE deleted_at = now()
  const handleSoftDelete = async (id: string) => {
    setIsDeleting(true);
    try {
      const { error } = await supabase
        .from('certificates')
        .update({ deleted_at: new Date().toISOString() })
        .eq('id', id);

      if (error) {
        console.error('Soft delete error:', error);
        alert('সনদ মুছে ফেলতে সমস্যা হয়েছে।');
      } else {
        setDeleteTargetId(null);
        await fetchCertificates();
      }
    } catch (err) {
      console.error('Delete exception:', err);
    } finally {
      setIsDeleting(false);
    }
  };

  // Restore handler: UPDATE deleted_at = null
  const handleRestore = async (id: string) => {
    try {
      const { error } = await supabase
        .from('certificates')
        .update({ deleted_at: null })
        .eq('id', id);

      if (error) {
        console.error('Restore error:', error);
        alert('সনদ পুনরুদ্ধার করতে সমস্যা হয়েছে।');
      } else {
        await fetchCertificates();
      }
    } catch (err) {
      console.error('Restore exception:', err);
    }
  };

  // Export CSV of filtered certificates
  const handleExportCSV = () => {
    if (filteredCertificates.length === 0) {
      alert('এক্সপোর্ট করার মতো কোনো সনদ নেই।');
      return;
    }

    const headers = ['ক্রমিক নম্বর', 'স্মারক নম্বর', 'সনদের ধরন', 'আবেদনকারীর নাম', 'প্রস্তুতকারী', 'স্ট্যাটাস', 'তারিখ'];
    const rows = filteredCertificates.map((c, idx) => {
      const applicant = c.data?.applicant_name || c.data?.applicantName || '—';
      const type = c.certificate_types?.title_bn || c.type_id;
      const creator = c.profiles?.full_name || '—';
      return [
        idx + 1,
        `"${c.serial_no || 'অনির্ধারিত'}"`,
        `"${type}"`,
        `"${applicant}"`,
        `"${creator}"`,
        `"${c.status}"`,
        `"${formatBanglaDate(c.created_at)}"`,
      ];
    });

    const csvContent = 'data:text/csv;charset=utf-8,\uFEFF' + [headers.join(','), ...rows.map((e) => e.join(','))].join('\n');
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', `সনদ_তালিকা_${new Date().toISOString().slice(0, 10)}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <AdminLayout>
      {/* Page Title & Actions */}
      <div className="mb-6 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h1 className="text-xl md:text-2xl font-bold text-slate-800">সনদপত্র ও প্রত্যয়ন তালিকা</h1>
          <p className="text-xs md:text-sm text-slate-500 mt-0.5">
            মোট {toBanglaNumber(filteredCertificates.length)} টি সনদপত্র পাওয়া গেছে
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={handleExportCSV}
            className="flex items-center gap-1.5 px-3.5 py-2 bg-white border border-slate-200 text-slate-700 hover:bg-slate-50 rounded-xl text-xs font-semibold shadow-xs transition"
          >
            <Download className="w-4 h-4 text-slate-500" />
            <span>CSV এক্সপোর্ট</span>
          </button>
          <button
            onClick={() => fetchCertificates()}
            className="p-2 bg-white border border-slate-200 text-slate-700 hover:bg-slate-50 rounded-xl shadow-xs transition"
            title="রিফ্রেশ"
          >
            <RefreshCw className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="bg-white p-4 rounded-2xl border border-slate-200 shadow-xs mb-6 space-y-3">
        <div className="flex flex-col md:flex-row gap-3">
          {/* Search Input */}
          <div className="flex-1 relative">
            <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder="আবেদনকারীর নাম বা স্মারক নম্বর দিয়ে খুঁজুন..."
              className="w-full pl-10 pr-4 py-2 text-xs md:text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-bdGreen-600 focus:bg-white transition"
            />
          </div>

          {/* Type Filter */}
          <select
            value={selectedType}
            onChange={(e) => setSelectedType(e.target.value)}
            className="text-xs md:text-sm py-2 px-3 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-bdGreen-600 font-medium"
          >
            <option value="all">সকল সনদের ধরন</option>
            {types.map((t) => (
              <option key={t.id} value={t.id}>
                {t.title_bn}
              </option>
            ))}
          </select>

          {/* Status Filter */}
          <select
            value={selectedStatus}
            onChange={(e) => setSelectedStatus(e.target.value)}
            className="text-xs md:text-sm py-2 px-3 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-bdGreen-600 font-medium"
          >
            <option value="all">সকল স্ট্যাটাস</option>
            <option value="generated">ইস্যুকৃত (Generated)</option>
            <option value="signed">স্বাক্ষরিত (Signed)</option>
            <option value="printed">প্রিন্টকৃত (Printed)</option>
            <option value="draft">খসড়া (Draft)</option>
            <option value="rejected">বাতিলকৃত (Rejected)</option>
          </select>
        </div>

        {/* Second Row Filters: User, Dates & Soft Deleted Toggle */}
        <div className="flex flex-wrap items-center gap-3 pt-2 border-t border-slate-100 text-xs">
          {users.length > 0 && (
            <div className="flex items-center gap-1.5">
              <User className="w-3.5 h-3.5 text-slate-400" />
              <select
                value={selectedUser}
                onChange={(e) => setSelectedUser(e.target.value)}
                className="py-1 px-2.5 bg-slate-50 border border-slate-200 rounded-lg text-slate-700"
              >
                <option value="all">সকল ইউজার</option>
                {users.map((u) => (
                  <option key={u.id} value={u.id}>
                    {u.full_name}
                  </option>
                ))}
              </select>
            </div>
          )}

          <div className="flex items-center gap-1.5">
            <Calendar className="w-3.5 h-3.5 text-slate-400" />
            <input
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              className="py-1 px-2 bg-slate-50 border border-slate-200 rounded-lg text-slate-700 text-xs"
              title="শুরুর তারিখ"
            />
            <span className="text-slate-400">হতে</span>
            <input
              type="date"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              className="py-1 px-2 bg-slate-50 border border-slate-200 rounded-lg text-slate-700 text-xs"
              title="শেষ তারিখ"
            />
          </div>

          {/* Show Deleted Toggle (for Admins) */}
          <label className="flex items-center gap-2 cursor-pointer ml-auto text-slate-600 select-none py-1">
            <input
              type="checkbox"
              checked={showDeleted}
              onChange={(e) => setShowDeleted(e.target.checked)}
              className="rounded text-bdRed-600 focus:ring-bdRed-500 w-4 h-4"
            />
            <span className={showDeleted ? 'text-bdRed-600 font-bold' : ''}>
              মুছে ফেলা সনদ দেখুন
            </span>
          </label>
        </div>
      </div>

      {/* Certificates Content */}
      {loading ? (
        <div className="p-8 text-center text-slate-500 font-medium">লোড হচ্ছে...</div>
      ) : filteredCertificates.length === 0 ? (
        <EmptyState
          title="কোনো সনদপত্র পাওয়া যায়নি"
          description="আপনার সার্চ বা ফিল্টারের সাথে মিলে এমন কোনো সনদ ডেটাবেসে নেই।"
        />
      ) : (
        <>
          {/* Desktop Table View */}
          <div className="hidden md:block bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 text-slate-600 border-b border-slate-200">
                <tr>
                  <th className="py-3 px-4 font-semibold">স্মারক নম্বর</th>
                  <th className="py-3 px-4 font-semibold">সনদের নাম</th>
                  <th className="py-3 px-4 font-semibold">আবেদনকারীর নাম</th>
                  <th className="py-3 px-4 font-semibold">প্রস্তুতকারী</th>
                  <th className="py-3 px-4 font-semibold">স্ট্যাটাস</th>
                  <th className="py-3 px-4 font-semibold">তারিখ</th>
                  <th className="py-3 px-4 font-semibold text-right">পদক্ষেপ</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {paginatedCertificates.map((cert) => {
                  const applicant = cert.data?.applicant_name || cert.data?.applicantName || '—';
                  const isDel = cert.deleted_at !== null;
                  return (
                    <tr
                      key={cert.id}
                      onClick={() => router.push(`/certificates/${cert.id}`)}
                      className={`cursor-pointer hover:bg-slate-50/80 transition ${
                        isDel ? 'bg-rose-50/40 opacity-80' : ''
                      }`}
                    >
                      <td className="py-3 px-4 font-bold text-slate-800 whitespace-nowrap">
                        {cert.serial_no || 'অনির্ধারিত'}
                      </td>
                      <td className="py-3 px-4 text-slate-700 font-medium">
                        {cert.certificate_types?.title_bn || cert.type_id}
                      </td>
                      <td className="py-3 px-4 text-slate-900 font-bold">{applicant}</td>
                      <td className="py-3 px-4 text-slate-600">
                        {cert.profiles?.full_name || 'ইউজার'}
                      </td>
                      <td className="py-3 px-4 whitespace-nowrap">
                        <StatusBadge status={cert.status} />
                      </td>
                      <td className="py-3 px-4 text-slate-500 whitespace-nowrap">
                        {formatBanglaDate(cert.created_at)}
                      </td>
                      <td className="py-3 px-4 text-right" onClick={(e) => e.stopPropagation()}>
                        <div className="flex items-center justify-end gap-1.5">
                          <Link
                            href={`/certificates/${cert.id}`}
                            className="p-1.5 text-slate-600 hover:text-bdGreen-700 hover:bg-slate-100 rounded-lg"
                            title="বিস্তারিত দেখুন"
                          >
                            <Eye className="w-4 h-4" />
                          </Link>
                          {isDel ? (
                            <button
                              onClick={() => handleRestore(cert.id)}
                              className="px-2 py-1 text-[11px] font-bold text-emerald-700 bg-emerald-50 hover:bg-emerald-100 rounded-md transition"
                            >
                              পুনরুদ্ধার
                            </button>
                          ) : (
                            <button
                              onClick={() => setDeleteTargetId(cert.id)}
                              className="p-1.5 text-bdRed-600 hover:bg-bdRed-50 rounded-lg transition"
                              title="সফট ডিলিট করুন"
                            >
                              <Trash2 className="w-4 h-4" />
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>

          {/* Mobile Card View */}
          <div className="md:hidden space-y-3">
            {paginatedCertificates.map((cert) => {
              const applicant = cert.data?.applicant_name || cert.data?.applicantName || '—';
              const isDel = cert.deleted_at !== null;
              return (
                <div
                  key={cert.id}
                  onClick={() => router.push(`/certificates/${cert.id}`)}
                  className={`bg-white p-4 rounded-xl border border-slate-200 shadow-xs cursor-pointer active:scale-[0.99] transition ${
                    isDel ? 'border-rose-200 bg-rose-50/20' : ''
                  }`}
                >
                  <div className="flex items-start justify-between gap-2 mb-2">
                    <div>
                      <span className="text-[11px] font-mono font-bold text-slate-500">
                        {cert.serial_no || 'স্মারক নম্বর নেই'}
                      </span>
                      <h4 className="font-bold text-sm text-slate-800 leading-tight">
                        {cert.certificate_types?.title_bn}
                      </h4>
                    </div>
                    <StatusBadge status={cert.status} />
                  </div>

                  <div className="text-xs space-y-1 py-1 text-slate-600 border-t border-slate-100 mt-2 pt-2">
                    <p>
                      <span className="text-slate-400">আবেদনকারী:</span>{' '}
                      <strong className="text-slate-800">{applicant}</strong>
                    </p>
                    <p>
                      <span className="text-slate-400">প্রস্তুতকারী:</span>{' '}
                      {cert.profiles?.full_name || 'ইউজার'}
                    </p>
                    <p>
                      <span className="text-slate-400">তারিখ:</span>{' '}
                      {formatBanglaDate(cert.created_at)}
                    </p>
                  </div>

                  <div
                    className="flex items-center justify-end gap-2 pt-2 border-t border-slate-100 mt-2"
                    onClick={(e) => e.stopPropagation()}
                  >
                    <Link
                      href={`/certificates/${cert.id}`}
                      className="px-3 py-1 bg-slate-100 text-slate-700 text-xs font-semibold rounded-lg flex items-center gap-1"
                    >
                      <Eye className="w-3.5 h-3.5" />
                      <span>বিস্তারিত</span>
                    </Link>
                    {isDel ? (
                      <button
                        onClick={() => handleRestore(cert.id)}
                        className="px-3 py-1 bg-emerald-50 text-emerald-700 text-xs font-bold rounded-lg"
                      >
                        পুনরুদ্ধার
                      </button>
                    ) : (
                      <button
                        onClick={() => setDeleteTargetId(cert.id)}
                        className="px-3 py-1 bg-bdRed-50 text-bdRed-600 text-xs font-semibold rounded-lg flex items-center gap-1"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                        <span>মুছুন</span>
                      </button>
                    )}
                  </div>
                </div>
              );
            })}
          </div>

          {/* Pagination Controls */}
          {totalPages > 1 && (
            <div className="flex items-center justify-between mt-6 px-2 text-xs">
              <span className="text-slate-500">
                পৃষ্ঠা {toBanglaNumber(currentPage)} / {toBanglaNumber(totalPages)}
              </span>
              <div className="flex items-center gap-2">
                <button
                  disabled={currentPage <= 1}
                  onClick={() => setCurrentPage((p) => Math.max(p - 1, 1))}
                  className="p-2 border border-slate-200 bg-white rounded-lg disabled:opacity-40"
                >
                  <ChevronLeft className="w-4 h-4" />
                </button>
                <button
                  disabled={currentPage >= totalPages}
                  onClick={() => setCurrentPage((p) => Math.min(p + 1, totalPages))}
                  className="p-2 border border-slate-200 bg-white rounded-lg disabled:opacity-40"
                >
                  <ChevronRight className="w-4 h-4" />
                </button>
              </div>
            </div>
          )}
        </>
      )}

      {/* Bangla Confirm Soft Delete Modal */}
      {deleteTargetId && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-xs flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-2xl max-w-sm w-full p-6 text-center shadow-xl border border-slate-100">
            <div className="w-12 h-12 rounded-full bg-bdRed-50 text-bdRed-600 flex items-center justify-center mx-auto mb-4">
              <Trash2 className="w-6 h-6" />
            </div>
            <h3 className="font-bold text-base text-slate-800 mb-2">সনদটি মুছে ফেলতে চান?</h3>
            <p className="text-xs text-slate-500 mb-6 leading-relaxed">
              সনদটি সফট-ডিলিট করা হবে (ডাটাবেস থেকে স্থায়ীভাবে মুছে যাবে না)। পরবর্তীতে প্রয়োজনে পুনরুদ্ধার করতে
              পারবেন।
            </p>
            <div className="flex items-center justify-center gap-3">
              <button
                onClick={() => setDeleteTargetId(null)}
                disabled={isDeleting}
                className="px-4 py-2 border border-slate-200 text-slate-600 rounded-xl text-xs font-semibold hover:bg-slate-50 flex-1"
              >
                বাতিল
              </button>
              <button
                onClick={() => handleSoftDelete(deleteTargetId)}
                disabled={isDeleting}
                className="px-4 py-2 bg-bdRed-600 text-white rounded-xl text-xs font-bold hover:bg-bdRed-700 flex-1 shadow-sm"
              >
                {isDeleting ? 'মুছে ফেলা হচ্ছে...' : 'হ্যাঁ, মুছুন'}
              </button>
            </div>
          </div>
        </div>
      )}
    </AdminLayout>
  );
}
