import type { Metadata } from 'next';
import './globals.css';
import { AuthProvider } from '@/components/AuthProvider';

export const metadata: Metadata = {
  title: 'ইউপি সনদ অ্যাডমিন প্যানেল | UP Sonod Admin',
  description: 'বাংলাদেশ ইউনিয়ন পরিষদ ডিজিটাল সনদ ও প্রত্যয়নপত্র অ্যাডমিন পোর্টাল',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="bn">
      <head>
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link rel="preconnect" href="https://fonts.gstatic.com" crossOrigin="anonymous" />
        <link
          href="https://fonts.googleapis.com/css2?family=Noto+Sans+Bengali:wght@300;400;500;600;700;800&display=swap"
          rel="stylesheet"
        />
      </head>
      <body className="min-h-screen bg-slate-50 antialiased font-bangla">
        <AuthProvider>{children}</AuthProvider>
      </body>
    </html>
  );
}
