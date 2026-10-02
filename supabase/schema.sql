-- ============================================================================
-- ইউপি সনদ (UP Sonod) - Complete Supabase Database Schema, RLS, Triggers & RPC
-- Paste this script into your Supabase Dashboard -> SQL Editor and click "RUN"
-- ============================================================================

-- 1. Enable UUID Extension
create extension if not exists "uuid-ossp";

-- 2. Create 'unions' Table
create table if not exists public.unions (
    id uuid primary key default gen_random_uuid(),
    name_bn text not null,
    upazila text not null,
    district text not null,
    chairman_name text not null,
    phone text,
    email text,
    logo_url text,
    created_at timestamptz not null default now()
);

-- 3. Create 'profiles' Table (extends auth.users)
create table if not exists public.profiles (
    id uuid primary key references auth.users(id) on delete cascade,
    role text not null check (role in ('super_admin', 'union_admin', 'operator')) default 'operator',
    union_id uuid references public.unions(id) on delete set null,
    full_name text not null,
    phone text,
    is_active boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

-- 4. Create 'certificate_types' Table (Dynamic Certificate Templates)
create table if not exists public.certificate_types (
    id text primary key,
    title_bn text not null,
    english_name text,
    category text default 'নাগরিক সেবা',
    fields jsonb not null default '[]'::jsonb,
    template_bn text not null,
    is_active boolean not null default true,
    created_at timestamptz not null default now()
);

-- 5. Create 'certificates' Table
create table if not exists public.certificates (
    id uuid primary key default gen_random_uuid(),
    union_id uuid not null references public.unions(id) on delete cascade,
    type_id text not null references public.certificate_types(id) on delete restrict,
    created_by uuid references auth.users(id) on delete set null,
    data jsonb not null default '{}'::jsonb,
    serial_no text not null,
    status text not null check (status in ('draft', 'generated', 'printed', 'signed', 'rejected')) default 'generated',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    deleted_at timestamptz
);

-- 6. Create 'audit_logs' Table
create table if not exists public.audit_logs (
    id uuid primary key default gen_random_uuid(),
    user_id uuid references auth.users(id) on delete set null,
    action text not null,
    entity text not null,
    entity_id text,
    details jsonb default '{}'::jsonb,
    created_at timestamptz not null default now()
);

-- Indices for rapid querying
create index if not exists idx_certificates_union on public.certificates(union_id);
create index if not exists idx_certificates_created_by on public.certificates(created_by);
create index if not exists idx_certificates_type on public.certificates(type_id);
create index if not exists idx_certificates_deleted_at on public.certificates(deleted_at);
create index if not exists idx_profiles_union on public.profiles(union_id);

-- ============================================================================
-- HELPER FUNCTIONS FOR ROW LEVEL SECURITY (SECURITY DEFINER to avoid recursion)
-- ============================================================================

create or replace function public.current_profile()
returns public.profiles
language sql
security definer
set search_path = public
stable
as $$
    select * from public.profiles where id = auth.uid() limit 1;
$$;

create or replace function public.is_active_user()
returns boolean
language sql
security definer
set search_path = public
stable
as $$
    select coalesce((select is_active from public.profiles where id = auth.uid()), false);
$$;

create or replace function public.is_super_admin()
returns boolean
language sql
security definer
set search_path = public
stable
as $$
    select coalesce((select (role = 'super_admin' and is_active = true) from public.profiles where id = auth.uid()), false);
$$;

create or replace function public.current_union_id()
returns uuid
language sql
security definer
set search_path = public
stable
as $$
    select union_id from public.profiles where id = auth.uid();
$$;

-- ============================================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- Inactive users are strictly blocked from all operations.
-- ============================================================================

alter table public.unions enable row level security;
alter table public.profiles enable row level security;
alter table public.certificate_types enable row level security;
alter table public.certificates enable row level security;
alter table public.audit_logs enable row level security;

-- Policies for 'unions'
create policy "Active users can view unions"
    on public.unions for select
    using (public.is_active_user());

create policy "Super admin can manage unions"
    on public.unions for all
    using (public.is_super_admin());

create policy "Union admin can update their own union"
    on public.unions for update
    using (public.is_active_user() and id = public.current_union_id());

-- Policies for 'profiles'
create policy "Super admin can view all profiles"
    on public.profiles for select
    using (public.is_super_admin());

create policy "Union admin can view profiles in their union"
    on public.profiles for select
    using (public.is_active_user() and union_id = public.current_union_id());

create policy "Users can view their own profile"
    on public.profiles for select
    using (id = auth.uid() and is_active = true);

create policy "Super admin can manage all profiles"
    on public.profiles for all
    using (public.is_super_admin());

create policy "Union admin can update operators in their union"
    on public.profiles for update
    using (
        public.is_active_user()
        and union_id = public.current_union_id()
        and role = 'operator'
    );

create policy "Users can update their own contact info"
    on public.profiles for update
    using (id = auth.uid() and is_active = true)
    with check (id = auth.uid() and is_active = true);

-- Policies for 'certificate_types'
create policy "Active users can view certificate types"
    on public.certificate_types for select
    using (public.is_active_user() and is_active = true);

create policy "Super admin can manage certificate types"
    on public.certificate_types for all
    using (public.is_super_admin());

-- Policies for 'certificates'
create policy "Super admin can view all certificates"
    on public.certificates for select
    using (public.is_super_admin());

create policy "Union admin can view their union's certificates"
    on public.certificates for select
    using (
        public.is_active_user()
        and union_id = public.current_union_id()
        and deleted_at is null
    );

create policy "Operator can view own certificates"
    on public.certificates for select
    using (
        public.is_active_user()
        and created_by = auth.uid()
        and deleted_at is null
    );

create policy "Active users can insert certificates for their union"
    on public.certificates for insert
    with check (
        public.is_active_user()
        and union_id = public.current_union_id()
        and created_by = auth.uid()
    );

create policy "Super admin and Union admin can update certificates"
    on public.certificates for update
    using (
        public.is_super_admin()
        or (public.is_active_user() and union_id = public.current_union_id())
    );

create policy "Operator can update own certificates"
    on public.certificates for update
    using (
        public.is_active_user()
        and created_by = auth.uid()
        and deleted_at is null
    );

create policy "Union admin can soft delete certificates"
    on public.certificates for delete
    using (
        public.is_super_admin()
        or (public.is_active_user() and union_id = public.current_union_id())
    );

-- Policies for 'audit_logs'
create policy "Super admin can view all audit logs"
    on public.audit_logs for select
    using (public.is_super_admin());

create policy "Union admin can view audit logs for their users"
    on public.audit_logs for select
    using (
        public.is_active_user()
        and user_id in (select id from public.profiles where union_id = public.current_union_id())
    );

-- ============================================================================
-- TRIGGERS
-- ============================================================================

-- Trigger 1: Auto-create user profile on Supabase auth signup
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
    v_role text;
    v_full_name text;
    v_phone text;
    v_union_id uuid;
begin
    v_role := coalesce(new.raw_user_meta_data->>'role', 'operator');
    v_full_name := coalesce(new.raw_user_meta_data->>'full_name', split_part(new.email, '@', 1));
    v_phone := new.raw_user_meta_data->>'phone';
    
    if new.raw_user_meta_data->>'union_id' is not null then
        begin
            v_union_id := (new.raw_user_meta_data->>'union_id')::uuid;
        exception when others then
            v_union_id := null;
        end;
    end if;

    insert into public.profiles (id, role, union_id, full_name, phone, is_active)
    values (new.id, v_role, v_union_id, v_full_name, v_phone, true)
    on conflict (id) do nothing;

    return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
    after insert on auth.users
    for each row execute function public.handle_new_user();

-- Trigger 2: Write Audit Logs for Certificate and Profile actions
create or replace function public.log_entity_audit()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
    v_user_id uuid;
    v_entity text;
    v_entity_id text;
    v_action text;
    v_details jsonb;
begin
    v_user_id := auth.uid();
    v_entity := TG_TABLE_NAME;
    v_action := TG_OP;

    if (TG_OP = 'DELETE') then
        v_entity_id := OLD.id::text;
        v_details := to_jsonb(OLD);
    else
        v_entity_id := NEW.id::text;
        v_details := to_jsonb(NEW);
    end if;

    insert into public.audit_logs (user_id, action, entity, entity_id, details)
    values (v_user_id, v_action, v_entity, v_entity_id, v_details);

    return coalesce(NEW, OLD);
end;
$$;

drop trigger if exists audit_certificates_trigger on public.certificates;
create trigger audit_certificates_trigger
    after insert or update or delete on public.certificates
    for each row execute function public.log_entity_audit();

-- ============================================================================
-- SERVER-SIDE FUNCTION: SERIAL NUMBER GENERATOR PER UNION PER YEAR
-- Format: UP/{YEAR}/{0001} (e.g. UP/2026/0001)
-- ============================================================================

create or replace function public.generate_certificate_serial_no(
    p_union_id uuid,
    p_year int default extract(year from now())::int
)
returns text
language plpgsql
security definer
set search_path = public
as $$
declare
    v_prefix text;
    v_pattern text;
    v_count int;
    v_next_serial text;
begin
    v_prefix := 'UP/' || p_year::text || '/';
    v_pattern := v_prefix || '%';

    -- Count existing certificates for this union and year
    select count(*)
    into v_count
    from public.certificates
    where union_id = p_union_id
      and serial_no like v_pattern;

    v_next_serial := v_prefix || lpad((v_count + 1)::text, 4, '0');
    return v_next_serial;
end;
$$;

-- Grant execution permissions
grant execute on function public.generate_certificate_serial_no(uuid, int) to authenticated, anon;

-- ============================================================================
-- INITIAL SEED DATA: Unions & 14 Certificate Types
-- ============================================================================

-- 1. Sample Union Seed
insert into public.unions (id, name_bn, upazila, district, chairman_name, phone, email)
values (
    '00000000-0000-0000-0000-000000000001',
    '৭নং কাঞ্চনপুর ইউনিয়ন পরিষদ',
    'রামগঞ্জ',
    'লক্ষ্মীপুর',
    'মোঃ ইকবাল হোসেন',
    '01811987654',
    'kanchanpur.up@gmail.com'
) on conflict (id) do nothing;

-- 2. Preload 14 Certificate Types
insert into public.certificate_types (id, title_bn, english_name, category, template_bn) values
('citizenship', 'নাগরিকত্ব সনদ', 'Citizenship Certificate', 'নাগরিক সেবা', 'এই মর্মে প্রত্যয়ন পত্র প্রদান করা যাইতেছে যে, {applicant_name}, পিতা/স্বামী: {father_husband_name}, মাতা: {mother_name}, গ্রাম: {village}, ওয়ার্ড নং: {ward_no}, ডাকঘর: {post_office}, উপজেলা: {upazila}, জেলা: {district}। তিনি অত্র ইউনিয়নের একজন স্থায়ী বাসিন্দা এবং জন্মসূত্রে ও আইনত বাংলাদেশের প্রকৃত নাগরিক। আমার জানামতে তিনি কোনো রাষ্ট্রবিরোধী বা আইন পরিপন্থী কর্মকাণ্ডে লিপ্ত নহেন। তাঁহার নৈতিক চরিত্র উত্তম।'),
('character', 'চারিত্রিক সনদ', 'Character Certificate', 'নাগরিক সেবা', 'এই মর্মে প্রত্যয়ন করা যাইতেছে যে, {applicant_name}, পিতা/স্বামী: {father_husband_name}, মাতা: {mother_name}, গ্রাম: {village}, ওয়ার্ড নং: {ward_no}, ডাকঘর: {post_office}, উপজেলা: {upazila}, জেলা: {district}। তিনি আমার ব্যক্তিগতভাবে পরিচিত। তিনি সৎ, চরিত্রবান ও শান্ত স্বভাবের নাগরিক। আমার জানামতে তিনি রাষ্ট্র ও সমাজবিরোধী কোনো কার্যকলাপে জড়িত নহেন।'),
('succession', 'ওয়ারিশান সনদ', 'Succession Certificate', 'উত্তরাধিকার', 'এই মর্মে প্রত্যয়ন পত্র প্রদান করা যাইতেছে যে, মরহুম/মরহুমা {deceased_name}, পিতা/স্বামী: {father_husband_name}, মাতা: {mother_name}, গ্রাম: {village}, ওয়ার্ড নং: {ward_no}, ডাকঘর: {post_office}, উপজেলা: {upazila}, জেলা: {district} অত্র ইউনিয়নের স্থায়ী বাসিন্দা ছিলেন। তিনি বিগত {death_date} খ্রিঃ তারিখে মৃত্যুবরণ করেন। মৃত্যুকালে তিনি নিম্নবর্ণিত ওয়ারিশগণকে রেখে যান।'),
('annual_income', 'বার্ষিক আয়ের প্রত্যয়ন', 'Annual Income Certificate', 'আর্থিক সেবা', 'এই মর্মে প্রত্যয়ন পত্র প্রদান করা যাইতেছে যে, {applicant_name}, পিতা/স্বামী: {father_husband_name}, মাতা: {mother_name}, গ্রাম: {village}, ওয়ার্ড নং: {ward_no}, ডাকঘর: {post_office}, উপজেলা: {upazila}, জেলা: {district}। স্থানীয় তদন্ত ও তথ্যানুযায়ী তাঁহার {income_source} উৎস হইতে সর্বমোট বাৎসরিক আয় {income_amount} (কথায়: {income_in_words}) টাকা।'),
('landless', 'ভূমিহীন সনদ', 'Landless Certificate', 'সামাজিক সুরক্ষা', 'এই মর্মে প্রত্যয়ন প্রদান করা যাইতেছে যে, {applicant_name}, পিতা/স্বামী: {father_husband_name}, মাতা: {mother_name}, গ্রাম: {village}, ওয়ার্ড নং: {ward_no}, ডাকঘর: {post_office}, উপজেলা: {upazila}, জেলা: {district}। তিনি অত্র ইউনিয়নের স্থায়ী বাসিন্দা। স্থানীয় তদন্তে নিশ্চিত হওয়া গিয়াছে যে, তাঁহার ও তাঁহার পরিবারের নামে অত্র এলাকায় কোনো আবাদি বা অনাবাদি জমিজমা কিংবা বসতভিটা নাই। তিনি একজন প্রকৃত ভূমিহীন ব্যক্তি।'),
('unmarried', 'অবিবাহিত সনদ', 'Unmarried Certificate', 'নাগরিক সেবা', 'এই মর্মে প্রত্যয়ন করা যাইতেছে যে, {applicant_name}, পিতা: {father_husband_name}, মাতা: {mother_name}, গ্রাম: {village}, ওয়ার্ড নং: {ward_no}, ডাকঘর: {post_office}, উপজেলা: {upazila}, জেলা: {district}। তিনি অত্র ইউনিয়নের স্থায়ী বাসিন্দা। অদ্যবধি তিনি কোনো বিবাহ বন্ধনে আবদ্ধ হন নাই। তিনি সম্পূর্ণ অবিবাহিত।'),
('married', 'বিবাহিত সনদ', 'Married Certificate', 'নাগরিক সেবা', 'এই মর্মে প্রত্যয়ন প্রদান করা যাইতেছে যে, {applicant_name}, পিতা/স্বামী: {father_husband_name}, মাতা: {mother_name}, গ্রাম: {village}, ওয়ার্ড নং: {ward_no}, ডাকঘর: {post_office}, উপজেলা: {upazila}, জেলা: {district}। তিনি {spouse_name}-এর সহিত ধর্মীয় ও আইনগত বিধান মোতাবেক বিবাহ বন্ধনে আবদ্ধ হইয়া বর্তমানে সুখে-শান্তিতে দাম্পত্য জীবন যাপন করিতেছেন।'),
('non_remarriage', 'পুনর্বিবাহ না হওয়া সনদ', 'Non-remarriage Certificate', 'সামাজিক সুরক্ষা', 'এই মর্মে প্রত্যয়ন করা যাইতেছে যে, {applicant_name}, মৃত স্বামী: {late_spouse_name}, মাতা: {mother_name}, গ্রাম: {village}, ওয়ার্ড নং: {ward_no}, ডাকঘর: {post_office}, উপজেলা: {upazila}, জেলা: {district}। তাঁহার স্বামীর মৃত্যুর পর হইতে তিনি অদ্যবধি কোনো দ্বিতীয় বা পুনর্বিবাহে আবদ্ধ হন নাই।'),
('disability', 'প্রতিবন্ধী প্রত্যয়ন', 'Disability Certificate', 'সামাজিক সুরক্ষা', 'এই মর্মে প্রত্যয়ন পত্র দেওয়া যাইতেছে যে, {applicant_name}, পিতা/স্বামী: {father_husband_name}, মাতা: {mother_name}, গ্রাম: {village}, ওয়ার্ড নং: {ward_no}, ডাকঘর: {post_office}, উপজেলা: {upazila}, জেলা: {district}। তিনি অত্র ইউনিয়নের একজন স্থায়ী বাসিন্দা এবং তিনি জন্মগত/দুর্ঘটনাজনিত কারণে একজন প্রকৃত {disability_type}।'),
('freedom_fighter_child', 'মুক্তিযোদ্ধা সন্তান সনদ', 'Freedom Fighter Child Certificate', 'বিশেষ প্রত্যয়ন', 'এই মর্মে প্রত্যয়ন করা যাইতেছে যে, {applicant_name}, গ্রাম: {village}, ওয়ার্ড নং: {ward_no}, ডাকঘর: {post_office}, উপজেলা: {upazila}, জেলা: {district}। তিনি মহান মুক্তিযুদ্ধে অংশগ্রহণকারী বীর মুক্তিযোদ্ধা {ff_name} (গেজেট/মুক্তিবার্তা নং: {ff_gazette_no})-এর ঔরসজাত {relation_with_ff}।'),
('permanent_resident', 'স্থায়ী বাসিন্দা সনদ', 'Permanent Resident Certificate', 'নাগরিক সেবা', 'এই মর্মে প্রত্যয়ন পত্র প্রদান করা যাইতেছে যে, {applicant_name}, পিতা/স্বামী: {father_husband_name}, মাতা: {mother_name}, গ্রাম: {village}, ওয়ার্ড নং: {ward_no}, ডাকঘর: {post_office}, উপজেলা: {upazila}, জেলা: {district}। তিনি পরিবার-পরিজনসহ দীর্ঘদিন যাবৎ অত্র ইউনিয়নের উল্লেখিত ঠিকানায় স্থায়ীভাবে বসবাস করিয়া আসিতেছেন।'),
('poverty', 'দরিদ্র সনদ', 'Poverty Certificate', 'সামাজিক সুরক্ষা', 'এই মর্মে প্রত্যয়ন করা যাইতেছে যে, {applicant_name}, পিতা/স্বামী: {father_husband_name}, মাতা: {mother_name}, গ্রাম: {village}, ওয়ার্ড নং: {ward_no}, ডাকঘর: {post_office}, উপজেলা: {upazila}, জেলা: {district}। তিনি অত্যন্ত দরিদ্র, নিঃস্ব ও অনগ্রসর পরিবারের সদস্য।'),
('business_status', 'ব্যবসা প্রত্যয়ন', 'Business Certificate', 'বাণিজ্যিক সেবা', 'এই মর্মে প্রত্যয়ন পত্র প্রদান করা যাইতেছে যে, {applicant_name}, পিতা/স্বামী: {father_husband_name}, মাতা: {mother_name}, গ্রাম: {village}, ওয়ার্ড নং: {ward_no}, ডাকঘর: {post_office}, উপজেলা: {upazila}, জেলা: {district}। তিনি অত্র ইউনিয়নের বাজারে {business_name} নামে একটি ব্যবসা সফলতার সহিত আইনানুগভাবে পরিচালনা করিয়া আসিতেছেন।'),
('death', 'মৃত্যু প্রত্যয়ন', 'Death Certificate', 'নাগরিক সেবা', 'এই মর্মে প্রত্যয়ন করা যাইতেছে যে, {deceased_name}, পিতা/স্বামী: {father_husband_name}, মাতা: {mother_name}, গ্রাম: {village}, ওয়ার্ড নং: {ward_no}, ডাকঘর: {post_office}, উপজেলা: {upazila}, জেলা: {district}। তিনি বিগত {date_of_death} খ্রিঃ তারিখে স্বাভাবিক মৃত্যুবরণ করিয়াছেন।')
on conflict (id) do update set
    title_bn = excluded.title_bn,
    english_name = excluded.english_name,
    category = excluded.category,
    template_bn = excluded.template_bn;
