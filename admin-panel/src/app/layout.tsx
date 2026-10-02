import type { Metadata } from 'next';
import { Noto_Sans_Bengali } from 'next/font/google';
import './globals.css';
import { AuthProvider } from '@/components/AuthProvider';

const notoSansBengali = Noto_Sans_Bengali({
  subsets: ['bengali'],
  weight: ['300', '400', '500', '600', '700', '800'],
  variable: '--font-noto-bengali',
  display: 'swap',
});

export const metadata: Metadata = {
  title: 'ইউপি সনদ অ্যাডমিন প্যানেল | UP Sonod Admin',
  description: 'বাংলাদেশ ইউনিয়ন পরিষদ ডিজিটাল সনদ ও প্রত্যয়নপত্র অ্যাডমিন পোর্টাল',
};

export const dynamic = 'force-dynamic';

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="bn" className={notoSansBengali.variable}>
      <body className={`min-h-screen bg-slate-50 antialiased font-bangla ${notoSansBengali.className}`}>
        <AuthProvider>{children}</AuthProvider>
      </body>
    </html>
  );
}
