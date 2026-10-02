# ইউপি সনদ অ্যাডমিন প্যানেল (UP Sonod Admin Panel Web App)

বাংলাদেশ ইউনিয়ন পরিষদ ডিজিটাল সনদ ও প্রত্যয়ন ব্যবস্থাপনা অ্যাডমিন পোর্টাল।

## টেকনোলজি স্ট্যাক (Tech Stack)
- **Framework:** Next.js 14 (App Router)
- **Language:** TypeScript
- **Styling:** Tailwind CSS + Noto Sans Bengali (Google Fonts)
- **Backend & Auth:** Supabase (`@supabase/supabase-js`)
- **Charts:** Recharts
- **Icons:** Lucide React

---

## লোকাল ডেভেলপমেন্ট (Local Setup)

১. প্রথমে `admin-panel` ফোল্ডারে প্রবেশ করুন:
```bash
cd admin-panel
```

২. প্রয়োজনীয় ডিপেনডেন্সি ইনস্টল করুন:
```bash
npm install
```

৩. `.env.local` ফাইল তৈরি করুন এবং আপনার Supabase Anon Key যোগ করুন:
```properties
NEXT_PUBLIC_SUPABASE_URL=https://your-project-id.supabase.co
NEXT_PUBLIC_SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.your-anon-key
```

৪. ডেভেলপমেন্ট সার্ভার চালু করুন:
```bash
npm run dev
```
ব্রাউজারে [http://localhost:3000](http://localhost:3000) ওপেন করুন।

---

## Vercel-এ ডেপ্লয় করার নিয়ম (Vercel Deployment)

১. গিটহাবে প্রজেক্ট পুশ করে [Vercel](https://vercel.com)-এ **Add New Project** সিলেক্ট করুন।
২. Root Directory হিসেবে `admin-panel` সিলেক্ট করুন (যদি এটি সাব-ফোল্ডারে থাকে)।
৩. **Environment Variables** সেকশনে এই দুটি ভ্যারিয়েবল যোগ করুন:
   - `NEXT_PUBLIC_SUPABASE_URL`: আপনার Supabase Project URL
   - `NEXT_PUBLIC_SUPABASE_ANON_KEY`: আপনার Supabase Anon Public Key
৪. **Deploy** বাটনে ক্লিক করুন। কয়েক সেকেন্ডের মধ্যে লাইভ লিঙ্ক তৈরি হয়ে যাবে।

---

## নিরাপত্তা বৈশিষ্ট্যসমূহ (Security Features)
- শুধুমাত্র Supabase Anon Key ব্যবহার করা হয়েছে। সার্ভিস রোল কি (service_role key) কখনোই ব্যবহৃত হয় না।
- ডেটাবেসের Row Level Security (RLS) পলিসির ওপর সম্পূর্ণ নির্ভরতা।
- সফট ডিলিট নীতি: কোনো সনদপত্র হার্ড ডিলিট হয় না (`UPDATE certificates SET deleted_at = now()`)।
