import { CertificateStatus, UserRole } from './types';

const banglaDigits: Record<string, string> = {
  '0': '০', '1': '১', '2': '২', '3': '৩', '4': '৪',
  '5': '৫', '6': '৬', '7': '৭', '8': '৮', '9': '৯',
};

export function toBanglaNumber(value: number | string | null | undefined): string {
  if (value === null || value === undefined) return '০';
  return value.toString().replace(/[0-9]/g, (digit) => banglaDigits[digit] || digit);
}

const banglaMonths = [
  'জানুয়ারি', 'ফেব্রুয়ারি', 'মার্চ', 'এপ্রিল', 'মে', 'জুন',
  'জুলাই', 'আগস্ট', 'সেপ্টেম্বর', 'অক্টোবর', 'নভেম্বর', 'ডিসেম্বর'
];

export function formatBanglaDate(dateStr: string | null | undefined): string {
  if (!dateStr) return '—';
  try {
    const d = new Date(dateStr);
    if (isNaN(d.getTime())) return dateStr;
    const day = toBanglaNumber(d.getDate().toString().padStart(2, '0'));
    const month = banglaMonths[d.getMonth()];
    const year = toBanglaNumber(d.getFullYear());
    return `${day} ${month}, ${year}`;
  } catch {
    return dateStr;
  }
}

export function formatBanglaDateTime(dateStr: string | null | undefined): string {
  if (!dateStr) return '—';
  try {
    const d = new Date(dateStr);
    if (isNaN(d.getTime())) return dateStr;
    const datePart = formatBanglaDate(dateStr);
    let hours = d.getHours();
    const minutes = toBanglaNumber(d.getMinutes().toString().padStart(2, '0'));
    const ampm = hours >= 12 ? 'অপরাহ্ন' : 'পূর্বাহ্ন';
    hours = hours % 12 || 12;
    return `${datePart} (${toBanglaNumber(hours)}:${minutes} ${ampm})`;
  } catch {
    return dateStr;
  }
}

export function getStatusMeta(status: CertificateStatus | string) {
  switch (status) {
    case 'generated':
      return { label: 'ইস্যু করা হয়েছে', bg: 'bg-emerald-50 text-emerald-700 border-emerald-200' };
    case 'signed':
      return { label: 'স্বাক্ষরিত', bg: 'bg-blue-50 text-blue-700 border-blue-200' };
    case 'printed':
      return { label: 'প্রিন্টকৃত', bg: 'bg-purple-50 text-purple-700 border-purple-200' };
    case 'draft':
      return { label: 'খসড়া (Draft)', bg: 'bg-amber-50 text-amber-700 border-amber-200' };
    case 'rejected':
      return { label: 'বাতিলকৃত', bg: 'bg-rose-50 text-rose-700 border-rose-200' };
    default:
      return { label: status, bg: 'bg-gray-50 text-gray-700 border-gray-200' };
  }
}

export function getRoleMeta(role: UserRole | string) {
  switch (role) {
    case 'super_admin':
      return { label: 'সুপার অ্যাডমিন', bg: 'bg-purple-100 text-purple-800 border-purple-300' };
    case 'union_admin':
      return { label: 'ইউনিয়ন অ্যাডমিন', bg: 'bg-emerald-100 text-emerald-800 border-emerald-300' };
    case 'operator':
      return { label: 'ইউজার / অপারেটর', bg: 'bg-blue-100 text-blue-800 border-blue-300' };
    default:
      return { label: role, bg: 'bg-gray-100 text-gray-800 border-gray-300' };
  }
}
