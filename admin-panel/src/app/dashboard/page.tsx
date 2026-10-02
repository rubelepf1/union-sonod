'use client';

import React, { useEffect, useState } from 'react';
import { AdminLayout } from '@/components/AdminLayout';
import { useAuth } from '@/components/AuthProvider';
import { supabase } from '@/lib/supabase';
import { toBanglaNumber, formatBanglaDate, formatBanglaDateTime } from '@/lib/bangla';
import { LoadingSkeleton } from '@/components/LoadingSkeleton';
import {
  FileText,
  CalendarCheck,
  AlertCircle,
  TrendingUp,
  UserCheck,
  Activity,
} from 'lucide-react';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  CartesianGrid,
  Cell,
} from 'recharts';

interface StatCardProps {
  title: string;
  count: number;
  icon: React.ElementType;
  color: string;
  bgLight: string;
}

const StatCard: React.FC<StatCardProps> = ({ title, count, icon: Icon, color, bgLight }) => (
  <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs flex items-center justify-between">
    <div>
      <p className="text-xs font-semibold text-slate-500 mb-1">{title}</p>
      <h3 className="text-2xl md:text-3xl font-extrabold text-slate-800 tracking-tight">
        {toBanglaNumber(count)} <span className="text-sm font-medium text-slate-400">টি</span>
      </h3>
    </div>
    <div className={`w-12 h-12 rounded-xl ${bgLight} ${color} flex items-center justify-center shrink-0`}>
      <Icon className="w-6 h-6" />
    </div>
  </div>
);

export default function DashboardPage() {
  const { profile, union, isLoading: authLoading } = useAuth();
  const [loading, setLoading] = useState(true);
  const [totalCertificates, setTotalCertificates] = useState(0);
  const [todayCertificates, setTodayCertificates] = useState(0);
  const [monthCertificates, setMonthCertificates] = useState(0);
  const [pendingCertificates, setPendingCertificates] = useState(0);
  const [typeDistribution, setTypeDistribution] = useState<any[]>([]);
  const [topUsers, setTopUsers] = useState<any[]>([]);
  const [recentAudits, setRecentAudits] = useState<any[]>([]);

  useEffect(() => {
    async function fetchDashboardStats() {
      if (!profile) return;
      setLoading(true);

      try {
        const unionId = profile.role === 'super_admin' ? null : profile.union_id;

        // 1. Total active certificates
        let totalQuery = supabase.from('certificates').select('id', { count: 'exact', head: true }).is('deleted_at', null);
        if (unionId) totalQuery = totalQuery.eq('union_id', unionId);
        const { count: totalCount } = await totalQuery;
        setTotalCertificates(totalCount || 0);

        // 2. Today's certificates
        const todayStart = new Date();
        todayStart.setHours(0, 0, 0, 0);
        let todayQuery = supabase
          .from('certificates')
          .select('id', { count: 'exact', head: true })
          .is('deleted_at', null)
          .gte('created_at', todayStart.toISOString());
        if (unionId) todayQuery = todayQuery.eq('union_id', unionId);
        const { count: todayCount } = await todayQuery;
        setTodayCertificates(todayCount || 0);

        // 3. This Month's certificates
        const monthStart = new Date();
        monthStart.setDate(1);
        monthStart.setHours(0, 0, 0, 0);
        let monthQuery = supabase
          .from('certificates')
          .select('id', { count: 'exact', head: true })
          .is('deleted_at', null)
          .gte('created_at', monthStart.toISOString());
        if (unionId) monthQuery = monthQuery.eq('union_id', unionId);
        const { count: monthCount } = await monthQuery;
        setMonthCertificates(monthCount || 0);

        // 4. Pending / Draft certificates
        let pendingQuery = supabase
          .from('certificates')
          .select('id', { count: 'exact', head: true })
          .is('deleted_at', null)
          .in('status', ['draft', 'rejected']);
        if (unionId) pendingQuery = pendingQuery.eq('union_id', unionId);
        const { count: pendingCount } = await pendingQuery;
        setPendingCertificates(pendingCount || 0);

        // 5. Distribution by certificate types
        let certsQuery = supabase
          .from('certificates')
          .select('type_id, certificate_types(title_bn)')
          .is('deleted_at', null)
          .limit(1000);
        if (unionId) certsQuery = certsQuery.eq('union_id', unionId);
        const { data: certsData } = await certsQuery;

        if (certsData) {
          const counts: Record<string, { name: string; count: number }> = {};
          certsData.forEach((c: any) => {
            const title = c.certificate_types?.title_bn || 'অন্যান্য সনদ';
            if (!counts[title]) {
              counts[title] = { name: title, count: 0 };
            }
            counts[title].count++;
          });
          const chartData = Object.values(counts)
            .sort((a, b) => b.count - a.count)
            .slice(0, 6);
          setTypeDistribution(chartData);
        }

        // 6. Top Users by certificates created
        let usersQuery = supabase
          .from('certificates')
          .select('created_by, profiles(full_name, phone)')
          .is('deleted_at', null)
          .limit(1000);
        if (unionId) usersQuery = usersQuery.eq('union_id', unionId);
        const { data: usersCertData } = await usersQuery;

        if (usersCertData) {
          const userCounts: Record<string, { id: string; name: string; phone: string; count: number }> = {};
          usersCertData.forEach((c: any) => {
            const userId = c.created_by;
            const name = c.profiles?.full_name || 'ইউজার';
            const phone = c.profiles?.phone || '';
            if (!userCounts[userId]) {
              userCounts[userId] = { id: userId, name, phone, count: 0 };
            }
            userCounts[userId].count++;
          });
          const sortedUsers = Object.values(userCounts)
            .sort((a, b) => b.count - a.count)
            .slice(0, 5);
          setTopUsers(sortedUsers);
        }

        // 7. Last 10 audit logs
        const { data: auditsData } = await supabase
          .from('audit_logs')
          .select('id, action, entity, entity_id, created_at, profiles(full_name)')
          .order('created_at', { ascending: false })
          .limit(10);
        if (auditsData) {
          setRecentAudits(auditsData);
        }
      } catch (err) {
        console.error('Failed to load dashboard data:', err);
      } finally {
        setLoading(false);
      }
    }

    if (!authLoading && profile) {
      fetchDashboardStats();
    }
  }, [profile, authLoading]);

  if (authLoading || loading) {
    return (
      <AdminLayout>
        <LoadingSkeleton rows={5} />
      </AdminLayout>
    );
  }

  const barColors = ['#006A4E', '#008763', '#10b981', '#34d399', '#6ee7b7', '#a7f3d0'];

  return (
    <AdminLayout>
      {/* Page Title */}
      <div className="mb-6 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2">
        <div>
          <h1 className="text-xl md:text-2xl font-bold text-slate-800">
            {union?.name_bn ? `${union.name_bn} অ্যাডমিন ড্যাশবোর্ড` : 'অ্যাডমিন ড্যাশবোর্ড'}
          </h1>
          <p className="text-xs md:text-sm text-slate-500 mt-0.5">
            আজকের তারিখ: {formatBanglaDate(new Date().toISOString())}
          </p>
        </div>
      </div>

      {/* Top 4 Stat Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <StatCard
          title="মোট ইস্যুকৃত সনদ"
          count={totalCertificates}
          icon={FileText}
          color="text-bdGreen-700"
          bgLight="bg-bdGreen-50"
        />
        <StatCard
          title="আজকের নতুন সনদ"
          count={todayCertificates}
          icon={CalendarCheck}
          color="text-blue-700"
          bgLight="bg-blue-50"
        />
        <StatCard
          title="চলতি মাসের সনদ"
          count={monthCertificates}
          icon={TrendingUp}
          color="text-indigo-700"
          bgLight="bg-indigo-50"
        />
        <StatCard
          title="খসড়া / প্রক্রিয়াধীন সনদ"
          count={pendingCertificates}
          icon={AlertCircle}
          color="text-amber-700"
          bgLight="bg-amber-50"
        />
      </div>

      {/* Middle Row: Chart & Top Users */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-6">
        {/* Bar Chart (2 columns on large screen) */}
        <div className="lg:col-span-2 bg-white p-5 rounded-2xl border border-slate-200 shadow-xs flex flex-col">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h2 className="font-bold text-base text-slate-800">সনদের প্রকারভেদ অনুযায়ী পরিসংখ্যান</h2>
              <p className="text-xs text-slate-500">শীর্ষ সনদপত্রের তালিকা ও সংখ্যা</p>
            </div>
          </div>
          <div className="h-72 w-full mt-2">
            {typeDistribution.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={typeDistribution} margin={{ top: 10, right: 10, left: -20, bottom: 25 }}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                  <XAxis
                    dataKey="name"
                    tick={{ fontSize: 11, fill: '#64748b' }}
                    interval={0}
                    angle={-15}
                    textAnchor="end"
                  />
                  <YAxis tick={{ fontSize: 11, fill: '#64748b' }} allowDecimals={false} />
                  <Tooltip
                    formatter={(value: any) => [`${toBanglaNumber(value)} টি সনদ`, 'পরিমাণ']}
                    labelStyle={{ fontFamily: 'Noto Sans Bengali', fontWeight: 'bold' }}
                  />
                  <Bar dataKey="count" radius={[6, 6, 0, 0]}>
                    {typeDistribution.map((_, index) => (
                      <Cell key={`cell-${index}`} fill={barColors[index % barColors.length]} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            ) : (
              <div className="h-full flex items-center justify-center text-slate-400 text-xs">
                কোনো সনদের ডেটা পাওয়া যায়নি
              </div>
            )}
          </div>
        </div>

        {/* Top Users Card */}
        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs flex flex-col">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h2 className="font-bold text-base text-slate-800">সর্বোচ্চ সনদ প্রস্তুতকারী</h2>
              <p className="text-xs text-slate-500">অপারেটর ও ইউনিয়ন অ্যাডমিন</p>
            </div>
            <UserCheck className="w-5 h-5 text-bdGreen-600" />
          </div>

          <div className="flex-1 space-y-3">
            {topUsers.length > 0 ? (
              topUsers.map((u, idx) => (
                <div
                  key={u.id || idx}
                  className="flex items-center justify-between p-3 rounded-xl bg-slate-50 border border-slate-100"
                >
                  <div className="flex items-center gap-3 overflow-hidden">
                    <span className="w-7 h-7 rounded-lg bg-emerald-100 text-emerald-800 flex items-center justify-center text-xs font-bold shrink-0">
                      {toBanglaNumber(idx + 1)}
                    </span>
                    <div className="truncate">
                      <p className="text-xs font-bold text-slate-800 truncate">{u.name}</p>
                      {u.phone && <p className="text-[11px] text-slate-400">{u.phone}</p>}
                    </div>
                  </div>
                  <div className="text-right shrink-0">
                    <span className="text-xs font-extrabold text-bdGreen-700 bg-white px-2 py-1 rounded-md border border-slate-200">
                      {toBanglaNumber(u.count)} টি
                    </span>
                  </div>
                </div>
              ))
            ) : (
              <p className="text-xs text-slate-400 text-center py-10">কোনো তথ্য পাওয়া যায়নি</p>
            )}
          </div>
        </div>
      </div>

      {/* Bottom Row: Recent 10 Audit Events */}
      <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <Activity className="w-5 h-5 text-indigo-600" />
            <h2 className="font-bold text-base text-slate-800">সাম্প্রতিক অডিট অ্যাক্টিভিটি (সর্বশেষ ১০টি)</h2>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50 text-slate-600 border-b border-slate-100">
              <tr>
                <th className="py-2.5 px-3 font-semibold">সময়</th>
                <th className="py-2.5 px-3 font-semibold">অ্যাকশন</th>
                <th className="py-2.5 px-3 font-semibold">সংশ্লিষ্ট টেবিল</th>
                <th className="py-2.5 px-3 font-semibold">ব্যবহারকারী</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {recentAudits.length > 0 ? (
                recentAudits.map((a) => (
                  <tr key={a.id} className="hover:bg-slate-50/60">
                    <td className="py-2.5 px-3 text-slate-500 whitespace-nowrap">
                      {formatBanglaDateTime(a.created_at)}
                    </td>
                    <td className="py-2.5 px-3 font-medium text-slate-800">
                      <span className="px-2 py-0.5 rounded-md bg-slate-100 text-slate-700 font-mono text-[11px]">
                        {a.action}
                      </span>
                    </td>
                    <td className="py-2.5 px-3 text-slate-600 font-mono text-[11px]">
                      {a.entity}
                    </td>
                    <td className="py-2.5 px-3 text-slate-700">
                      {a.profiles?.full_name || 'সিস্টেম'}
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={4} className="py-6 text-center text-slate-400">
                    কোনো সাম্প্রতিক অডিট রেকর্ড নেই
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </AdminLayout>
  );
}
