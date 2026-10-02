'use client';

import React, { useEffect, useState, useMemo } from 'react';
import { AdminLayout } from '@/components/AdminLayout';
import { useAuth } from '@/components/AuthProvider';
import { supabase } from '@/lib/supabase';
import { AuditLog, Profile } from '@/lib/types';
import { toBanglaNumber, formatBanglaDateTime } from '@/lib/bangla';
import { LoadingSkeleton } from '@/components/LoadingSkeleton';
import { EmptyState } from '@/components/EmptyState';
import {
  ShieldAlert,
  ChevronDown,
  ChevronRight,
  Filter,
  RefreshCw,
  ChevronLeft,
  User,
  Calendar,
  Layers,
} from 'lucide-react';

export default function AuditPage() {
  const { profile } = useAuth();
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [users, setUsers] = useState<Profile[]>([]);
  const [loading, setLoading] = useState(true);

  // Filters
  const [selectedUser, setSelectedUser] = useState('all');
  const [selectedEntity, setSelectedEntity] = useState('all');
  const [selectedAction, setSelectedAction] = useState('all');
  const [selectedDate, setSelectedDate] = useState('');

  // Expandable row state (log IDs)
  const [expandedRowId, setExpandedRowId] = useState<string | null>(null);

  // Pagination
  const [currentPage, setCurrentPage] = useState(1);
  const pageSize = 20;

  useEffect(() => {
    async function loadMeta() {
      if (!profile) return;
      try {
        let uQuery = supabase.from('profiles').select('*').order('full_name');
        if (profile.role !== 'super_admin' && profile.union_id) {
          uQuery = uQuery.eq('union_id', profile.union_id);
        }
        const { data: uData } = await uQuery;
        if (uData) setUsers(uData);
      } catch (err) {
        console.error('Meta load error:', err);
      }
    }
    loadMeta();
  }, [profile]);

  const fetchLogs = async () => {
    if (!profile) return;
    setLoading(true);
    try {
      let query = supabase
        .from('audit_logs')
        .select('*, profiles(full_name, phone)')
        .order('created_at', { ascending: false });

      if (selectedUser !== 'all') {
        query = query.eq('user_id', selectedUser);
      }

      if (selectedEntity !== 'all') {
        query = query.eq('entity', selectedEntity);
      }

      if (selectedAction !== 'all') {
        query = query.eq('action', selectedAction);
      }

      if (selectedDate) {
        const start = new Date(selectedDate);
        start.setHours(0, 0, 0, 0);
        const end = new Date(selectedDate);
        end.setHours(23, 59, 59, 999);
        query = query.gte('created_at', start.toISOString()).lte('created_at', end.toISOString());
      }

      const { data, error } = await query;
      if (error) {
        console.error('Audit fetch error:', error);
      } else if (data) {
        setLogs(data as AuditLog[]);
      }
    } catch (err) {
      console.error('Audit exception:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLogs();
  }, [profile, selectedUser, selectedEntity, selectedAction, selectedDate]);

  // Pagination
  const totalPages = Math.ceil(logs.length / pageSize) || 1;
  const paginatedLogs = useMemo(() => {
    const start = (currentPage - 1) * pageSize;
    return logs.slice(start, start + pageSize);
  }, [logs, currentPage]);

  const toggleExpand = (id: string) => {
    setExpandedRowId((prev) => (prev === id ? null : id));
  };

  return (
    <AdminLayout>
      {/* Title */}
      <div className="mb-6 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h1 className="text-xl md:text-2xl font-bold text-slate-800">অডিট লগ ও সিস্টেম নিরাপত্তা</h1>
          <p className="text-xs md:text-sm text-slate-500 mt-0.5">
            মোট রেকর্ড: {toBanglaNumber(logs.length)} টি (শুধুমাত্র রিড-অনলি)
          </p>
        </div>

        <button
          onClick={fetchLogs}
          className="flex items-center gap-1.5 px-3.5 py-2 bg-white border border-slate-200 text-slate-700 hover:bg-slate-50 rounded-xl text-xs font-semibold shadow-xs transition w-fit"
        >
          <RefreshCw className="w-4 h-4 text-slate-500" />
          <span>রিফ্রেশ</span>
        </button>
      </div>

      {/* Filter Bar */}
      <div className="bg-white p-4 rounded-2xl border border-slate-200 shadow-xs mb-6 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3 text-xs">
        {/* User filter */}
        <div>
          <label className="block text-[11px] font-bold text-slate-500 uppercase mb-1">ব্যবহারকারী</label>
          <select
            value={selectedUser}
            onChange={(e) => setSelectedUser(e.target.value)}
            className="w-full py-2 px-3 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-1 focus:ring-bdGreen-600 font-medium"
          >
            <option value="all">সকল ব্যবহারকারী</option>
            {users.map((u) => (
              <option key={u.id} value={u.id}>
                {u.full_name}
              </option>
            ))}
          </select>
        </div>

        {/* Entity filter */}
        <div>
          <label className="block text-[11px] font-bold text-slate-500 uppercase mb-1">টেবিল (Entity)</label>
          <select
            value={selectedEntity}
            onChange={(e) => setSelectedEntity(e.target.value)}
            className="w-full py-2 px-3 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-1 focus:ring-bdGreen-600 font-mono"
          >
            <option value="all">সকল টেবিল</option>
            <option value="certificates">certificates</option>
            <option value="profiles">profiles</option>
            <option value="certificate_types">certificate_types</option>
            <option value="unions">unions</option>
          </select>
        </div>

        {/* Action filter */}
        <div>
          <label className="block text-[11px] font-bold text-slate-500 uppercase mb-1">অ্যাকশন</label>
          <select
            value={selectedAction}
            onChange={(e) => setSelectedAction(e.target.value)}
            className="w-full py-2 px-3 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-1 focus:ring-bdGreen-600 font-mono"
          >
            <option value="all">সকল অ্যাকশন</option>
            <option value="INSERT">INSERT</option>
            <option value="UPDATE">UPDATE</option>
            <option value="DELETE">DELETE</option>
            <option value="SOFT_DELETE">SOFT_DELETE</option>
          </select>
        </div>

        {/* Date filter */}
        <div>
          <label className="block text-[11px] font-bold text-slate-500 uppercase mb-1">তারিখ</label>
          <input
            type="date"
            value={selectedDate}
            onChange={(e) => setSelectedDate(e.target.value)}
            className="w-full py-2 px-3 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-1 focus:ring-bdGreen-600"
          />
        </div>
      </div>

      {loading ? (
        <LoadingSkeleton rows={6} />
      ) : logs.length === 0 ? (
        <EmptyState
          title="কোনো অডিট রেকর্ড পাওয়া যায়নি"
          description="বর্তমান ফিল্টারের সাথে মিলে এমন কোনো অডিট ইভেন্ট সংরক্ষিত নেই।"
        />
      ) : (
        <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 text-slate-600 border-b border-slate-200">
                <tr>
                  <th className="py-3 px-4 w-8"></th>
                  <th className="py-3 px-4 font-semibold">তারিখ ও সময়</th>
                  <th className="py-3 px-4 font-semibold">অ্যাকশন</th>
                  <th className="py-3 px-4 font-semibold">টেবিল / এনটিটি</th>
                  <th className="py-3 px-4 font-semibold">এনটিটি আইডি</th>
                  <th className="py-3 px-4 font-semibold">ব্যবহারকারী</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {paginatedLogs.map((log) => {
                  const isExpanded = expandedRowId === log.id;
                  return (
                    <React.Fragment key={log.id}>
                      <tr
                        onClick={() => toggleExpand(log.id)}
                        className="cursor-pointer hover:bg-slate-50/80 transition select-none"
                      >
                        <td className="py-3 px-4 text-slate-400">
                          {isExpanded ? (
                            <ChevronDown className="w-4 h-4 text-bdGreen-700" />
                          ) : (
                            <ChevronRight className="w-4 h-4" />
                          )}
                        </td>
                        <td className="py-3 px-4 text-slate-600 whitespace-nowrap">
                          {formatBanglaDateTime(log.created_at)}
                        </td>
                        <td className="py-3 px-4">
                          <span
                            className={`px-2 py-0.5 rounded-md font-mono text-[11px] font-bold ${
                              log.action === 'INSERT'
                                ? 'bg-emerald-50 text-emerald-700'
                                : log.action === 'UPDATE'
                                ? 'bg-blue-50 text-blue-700'
                                : 'bg-rose-50 text-rose-700'
                            }`}
                          >
                            {log.action}
                          </span>
                        </td>
                        <td className="py-3 px-4 font-mono text-[11px] text-slate-700">{log.entity}</td>
                        <td className="py-3 px-4 font-mono text-[11px] text-slate-500 truncate max-w-[150px]">
                          {log.entity_id || '—'}
                        </td>
                        <td className="py-3 px-4 font-semibold text-slate-800">
                          {log.profiles?.full_name || 'সিস্টেম / অজানা'}
                        </td>
                      </tr>

                      {/* Expandable JSON details row */}
                      {isExpanded && (
                        <tr className="bg-slate-50/90">
                          <td colSpan={6} className="p-4 pl-12">
                            <div className="bg-slate-900 text-emerald-400 p-4 rounded-xl font-mono text-[11px] overflow-x-auto shadow-inner">
                              <div className="text-slate-400 mb-2 border-b border-slate-700 pb-1 flex justify-between">
                                <span>অডিট লগ বিস্তারিত (JSON):</span>
                                <span>ID: {log.id}</span>
                              </div>
                              <pre>{JSON.stringify(log.details, null, 2) || '{}'}</pre>
                            </div>
                          </td>
                        </tr>
                      )}
                    </React.Fragment>
                  );
                })}
              </tbody>
            </table>
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex items-center justify-between p-4 border-t border-slate-100 text-xs">
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
        </div>
      )}
    </AdminLayout>
  );
}
