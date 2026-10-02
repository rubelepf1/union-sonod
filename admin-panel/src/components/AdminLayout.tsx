'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import {
  LayoutDashboard,
  FileText,
  Users,
  ScrollText,
  Building2,
  ShieldAlert,
  LogOut,
  Menu,
  X,
  UserCheck,
} from 'lucide-react';
import { useAuth } from './AuthProvider';
import { RoleBadge } from './RoleBadge';

const navItems = [
  { href: '/dashboard', label: 'ড্যাশবোর্ড', icon: LayoutDashboard },
  { href: '/certificates', label: 'সনদপত্র', icon: FileText },
  { href: '/users', label: 'ইউজার', icon: Users },
  { href: '/templates', label: 'টেমপ্লেট', icon: ScrollText },
  { href: '/union', label: 'পরিষদ তথ্য', icon: Building2 },
  { href: '/audit', label: 'অডিট লগ', icon: ShieldAlert },
];

export const AdminLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const pathname = usePathname();
  const { profile, union, signOut } = useAuth();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col md:flex-row font-bangla text-slate-900">
      {/* Desktop Sidebar */}
      <aside className="hidden md:flex flex-col w-64 bg-white border-r border-slate-200 shadow-sm shrink-0">
        {/* Brand Header */}
        <div className="h-16 flex items-center px-6 border-b border-slate-100 gap-3">
          <div className="w-10 h-10 rounded-xl bg-bdGreen-600 text-white flex items-center justify-center font-bold text-lg shadow-sm">
            ইউপি
          </div>
          <div className="overflow-hidden">
            <h1 className="font-bold text-base leading-tight text-slate-800 truncate">
              {union?.name_bn || 'ইউপি সনদ'}
            </h1>
            <p className="text-xs text-slate-500 truncate">অ্যাডমিন কন্ট্রোল প্যানেল</p>
          </div>
        </div>

        {/* Navigation Items */}
        <nav className="flex-1 px-3 py-4 space-y-1.5 overflow-y-auto">
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = pathname === item.href || (pathname ? pathname.startsWith(`${item.href}/`) : false);
            return (
              <Link
                key={item.href}
                href={item.href}
                className={`flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-sm font-medium transition-all ${
                  isActive
                    ? 'bg-bdGreen-50 text-bdGreen-700 font-semibold shadow-xs'
                    : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
                }`}
              >
                <Icon
                  className={`w-5 h-5 transition-colors ${
                    isActive ? 'text-bdGreen-600' : 'text-slate-400 group-hover:text-slate-600'
                  }`}
                />
                <span>{item.label}</span>
              </Link>
            );
          })}
        </nav>

        {/* User Profile in Sidebar */}
        <div className="p-4 border-t border-slate-100 bg-slate-50/50">
          <div className="flex items-center gap-3 mb-3">
            <div className="w-10 h-10 rounded-full bg-bdGreen-100 text-bdGreen-700 flex items-center justify-center font-bold shrink-0">
              <UserCheck className="w-5 h-5" />
            </div>
            <div className="overflow-hidden flex-1">
              <div className="font-medium text-sm text-slate-800 truncate">
                {profile?.full_name || 'অ্যাডমিন'}
              </div>
              <div className="mt-0.5">
                <RoleBadge role={profile?.role || 'operator'} />
              </div>
            </div>
          </div>
          <button
            onClick={() => signOut()}
            className="w-full flex items-center justify-center gap-2 px-3 py-2 text-xs font-semibold text-bdRed-600 hover:text-bdRed-700 hover:bg-bdRed-50 rounded-lg transition border border-bdRed-100"
          >
            <LogOut className="w-4 h-4" />
            <span>লগআউট করুন</span>
          </button>
        </div>
      </aside>

      {/* Mobile Top Header */}
      <header className="md:hidden bg-white border-b border-slate-200 px-4 h-14 flex items-center justify-between sticky top-0 z-30 shadow-xs">
        <div className="flex items-center gap-2.5">
          <div className="w-8 h-8 rounded-lg bg-bdGreen-600 text-white flex items-center justify-center font-bold text-sm">
            ইউপি
          </div>
          <div className="overflow-hidden">
            <h2 className="font-bold text-sm text-slate-800 truncate leading-tight">
              {union?.name_bn || 'ইউপি সনদ অ্যাডমিন'}
            </h2>
          </div>
        </div>
        <div className="flex items-center gap-2">
          {profile?.role && <RoleBadge role={profile.role} />}
          <button
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="p-1.5 rounded-lg text-slate-600 hover:bg-slate-100"
            aria-label="Toggle menu"
          >
            {mobileMenuOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
          </button>
        </div>
      </header>

      {/* Mobile Slide-down Menu */}
      {mobileMenuOpen && (
        <div className="md:hidden bg-white border-b border-slate-200 shadow-md p-4 space-y-2 z-20">
          <div className="pb-3 border-b border-slate-100 flex items-center justify-between">
            <div>
              <p className="text-sm font-semibold text-slate-800">{profile?.full_name}</p>
              <p className="text-xs text-slate-500">{profile?.phone || 'ফোন নম্বর নেই'}</p>
            </div>
            <button
              onClick={() => signOut()}
              className="flex items-center gap-1.5 px-3 py-1.5 text-xs text-bdRed-600 bg-bdRed-50 rounded-lg font-medium"
            >
              <LogOut className="w-3.5 h-3.5" />
              <span>লগআউট</span>
            </button>
          </div>
          <div className="grid grid-cols-2 gap-2 pt-1">
            {navItems.map((item) => {
              const Icon = item.icon;
              const isActive = pathname === item.href;
              return (
                <Link
                  key={item.href}
                  href={item.href}
                  onClick={() => setMobileMenuOpen(false)}
                  className={`flex items-center gap-2 p-2.5 rounded-lg text-xs font-medium ${
                    isActive ? 'bg-bdGreen-50 text-bdGreen-700 font-bold' : 'text-slate-700 hover:bg-slate-50'
                  }`}
                >
                  <Icon className="w-4 h-4 text-bdGreen-600" />
                  <span>{item.label}</span>
                </Link>
              );
            })}
          </div>
        </div>
      )}

      {/* Main Content Area */}
      <main className="flex-1 flex flex-col min-w-0 overflow-y-auto pb-20 md:pb-6">
        <div className="flex-1 p-4 md:p-8 max-w-7xl mx-auto w-full">{children}</div>
      </main>

      {/* Mobile Bottom Tab Bar */}
      <nav className="md:hidden fixed bottom-0 left-0 right-0 bg-white/95 backdrop-blur-md border-t border-slate-200 h-16 flex items-center justify-around px-2 z-30 shadow-lg">
        {navItems.slice(0, 5).map((item) => {
          const Icon = item.icon;
          const isActive = pathname === item.href || (pathname && item.href !== '/dashboard' ? pathname.startsWith(item.href) : false);
          return (
            <Link
              key={item.href}
              href={item.href}
              className={`flex flex-col items-center justify-center w-14 py-1 gap-1 text-[10px] font-medium transition ${
                isActive ? 'text-bdGreen-700 font-bold' : 'text-slate-400 hover:text-slate-600'
              }`}
            >
              <div
                className={`p-1 rounded-xl transition ${
                  isActive ? 'bg-bdGreen-100 text-bdGreen-700' : 'text-slate-400'
                }`}
              >
                <Icon className="w-4 h-4" />
              </div>
              <span className="truncate max-w-[56px] text-center leading-none">{item.label}</span>
            </Link>
          );
        })}
      </nav>
    </div>
  );
};
