// Supabase Edge Function: create-user
// Handles secure user creation, password reset, and status toggle.
// Authorized exclusively for 'super_admin' and 'union_admin'.

import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.45.6";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

serve(async (req: Request) => {
  // 1. Handle CORS preflight
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "";
    const supabaseAnonKey = Deno.env.get("SUPABASE_ANON_KEY") ?? "";
    const supabaseServiceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";

    if (!supabaseUrl || !supabaseServiceRoleKey) {
      return new Response(
        JSON.stringify({ error: "সার্ভার কনফিগারেশন ত্রুটি। অ্যাডমিনের সাথে যোগাযোগ করুন।" }),
        { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    // 2. Caller Authentication verification
    const authHeader = req.headers.get("Authorization");
    if (!authHeader || !authHeader.startsWith("Bearer ")) {
      return new Response(
        JSON.stringify({ error: "অননুমোদিত প্রবেশ। অনুগ্রহ করে পুনরায় লগইন করুন।" }),
        { status: 401, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    // Client using caller's JWT to verify caller session
    const callerClient = createClient(supabaseUrl, supabaseAnonKey, {
      global: { headers: { Authorization: authHeader } },
      auth: { persistSession: false },
    });

    const { data: { user: callerUser }, error: authError } = await callerClient.auth.getUser();
    if (authError || !callerUser) {
      return new Response(
        JSON.stringify({ error: "অবৈধ সেশন বা টোকেনের মেয়াদ শেষ হয়েছে।" }),
        { status: 401, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    // Service Client (Admin Privileges)
    const serviceClient = createClient(supabaseUrl, supabaseServiceRoleKey, {
      auth: { persistSession: false },
    });

    // 3. Load caller's profile
    const { data: callerProfile, error: profileError } = await serviceClient
      .from("profiles")
      .select("id, role, union_id, full_name, is_active")
      .eq("id", callerUser.id)
      .single();

    if (profileError || !callerProfile) {
      return new Response(
        JSON.stringify({ error: "আপনার ইউজার প্রোফাইল ডাটাবেসে পাওয়া যায়নি।" }),
        { status: 403, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    if (!callerProfile.is_active) {
      return new Response(
        JSON.stringify({ error: "আপনার অ্যাকাউন্টটি বর্তমানে নিষ্ক্রিয় রয়েছে।" }),
        { status: 403, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    if (callerProfile.role !== "super_admin" && callerProfile.role !== "union_admin") {
      return new Response(
        JSON.stringify({ error: "এই সুবিধাটি শুধুমাত্র অ্যাডমিনদের জন্য সংরক্ষিত।" }),
        { status: 403, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    // 4. Parse request body
    const body = await req.json().catch(() => ({}));
    const action = body.action || "create";

    // -------------------------------------------------------------
    // ACTION: CREATE USER
    // -------------------------------------------------------------
    if (action === "create") {
      const email = typeof body.email === "string" ? body.email.trim().toLowerCase() : "";
      const password = typeof body.password === "string" ? body.password : "";
      const fullName = typeof body.full_name === "string" ? body.full_name.trim() : "";
      const phone = typeof body.phone === "string" ? body.phone.trim() : null;
      const requestedRole = typeof body.role === "string" ? body.role : "operator";
      const requestedUnionId = typeof body.union_id === "string" && body.union_id.trim() ? body.union_id.trim() : null;

      // Validation
      if (!fullName || fullName.length < 2) {
        return new Response(
          JSON.stringify({ error: "ব্যবহারকারীর পূর্ণ নাম অবশ্যই দিতে হবে (কমপক্ষে ২ অক্ষর)।" }),
          { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }

      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!email || !emailRegex.test(email)) {
        return new Response(
          JSON.stringify({ error: "সঠিক ইমেইল ঠিকানা প্রদান করুন।" }),
          { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }

      if (!password || password.length < 8) {
        return new Response(
          JSON.stringify({ error: "পাসওয়ার্ড কমপক্ষে ৮ অক্ষরের হতে হবে।" }),
          { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }

      // Role and Union Authorization Rules
      let targetRole: "super_admin" | "union_admin" | "operator" = "operator";
      let targetUnionId: string | null = null;

      if (callerProfile.role === "union_admin") {
        // Union admin can ONLY create operators in their own union
        targetRole = "operator";
        targetUnionId = callerProfile.union_id;
      } else if (callerProfile.role === "super_admin") {
        // Super admin can create operator, union_admin, or super_admin
        if (requestedRole === "super_admin") {
          targetRole = "super_admin";
          targetUnionId = requestedUnionId;
        } else if (requestedRole === "union_admin") {
          targetRole = "union_admin";
          targetUnionId = requestedUnionId;
        } else {
          targetRole = "operator";
          targetUnionId = requestedUnionId;
        }
      }

      // Create user using Supabase Auth Admin API
      const { data: newAuthData, error: createError } = await serviceClient.auth.admin.createUser({
        email: email,
        password: password,
        email_confirm: true,
        user_metadata: {
          full_name: fullName,
          phone: phone,
          role: targetRole,
          union_id: targetUnionId,
        },
      });

      if (createError || !newAuthData?.user) {
        const errorMsg = createError?.message?.toLowerCase() || "";
        if (errorMsg.includes("already registered") || errorMsg.includes("already exists") || errorMsg.includes("unique")) {
          return new Response(
            JSON.stringify({ error: "এই ইমেইল দিয়ে ইতোমধ্যে একটি অ্যাকাউন্ট নিবন্ধিত রয়েছে।" }),
            { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
          );
        }
        return new Response(
          JSON.stringify({ error: "নতুন ব্যবহারকারী অ্যাকাউন্ট তৈরি করতে ব্যর্থ হয়েছে।" }),
          { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }

      const newUserId = newAuthData.user.id;

      // Update the profile row using serviceClient to ensure role and union_id are saved
      const { error: profileUpdateError } = await serviceClient
        .from("profiles")
        .upsert({
          id: newUserId,
          full_name: fullName,
          phone: phone,
          role: targetRole,
          union_id: targetUnionId,
          is_active: true,
          updated_at: new Date().toISOString(),
        });

      if (profileUpdateError) {
        console.error("Profile upsert error:", profileUpdateError);
      }

      // Record in audit_logs
      await serviceClient.from("audit_logs").insert({
        user_id: callerUser.id,
        action: "CREATE_USER",
        entity: "profiles",
        entity_id: newUserId,
        details: {
          created_by_role: callerProfile.role,
          created_user_email: email,
          created_user_role: targetRole,
          created_user_union_id: targetUnionId,
          full_name: fullName,
        },
      });

      return new Response(
        JSON.stringify({
          success: true,
          message: "নতুন ব্যবহারকারী সফলভাবে তৈরি করা হয়েছে।",
          user: {
            id: newUserId,
            email: email,
            full_name: fullName,
            role: targetRole,
            union_id: targetUnionId,
            is_active: true,
          },
        }),
        { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    // -------------------------------------------------------------
    // ACTION: RESET PASSWORD
    // -------------------------------------------------------------
    if (action === "reset-password") {
      const targetUserId = body.userId;
      const newPassword = body.newPassword;

      if (!targetUserId || typeof targetUserId !== "string") {
        return new Response(
          JSON.stringify({ error: "ব্যবহারকারী শনাক্তকারী (ID) পাওয়া যায়নি।" }),
          { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }

      if (!newPassword || typeof newPassword !== "string" || newPassword.length < 8) {
        return new Response(
          JSON.stringify({ error: "নতুন পাসওয়ার্ড কমপক্ষে ৮ অক্ষরের হতে হবে।" }),
          { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }

      // Load target profile
      const { data: targetProfile, error: targetError } = await serviceClient
        .from("profiles")
        .select("id, role, union_id, full_name")
        .eq("id", targetUserId)
        .single();

      if (targetError || !targetProfile) {
        return new Response(
          JSON.stringify({ error: "ব্যবহারকারীকে খুঁজে পাওয়া যায়নি।" }),
          { status: 404, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }

      // Role check: union_admin can only reset password of operators in own union
      if (callerProfile.role === "union_admin") {
        if (targetProfile.union_id !== callerProfile.union_id || targetProfile.role !== "operator") {
          return new Response(
            JSON.stringify({ error: "আপনি শুধুমাত্র আপনার ইউনিয়নের অপারেটরদের পাসওয়ার্ড পরিবর্তন করতে পারবেন।" }),
            { status: 403, headers: { ...corsHeaders, "Content-Type": "application/json" } }
          );
        }
      }

      // Update password via Auth Admin API
      const { error: resetError } = await serviceClient.auth.admin.updateUserById(targetUserId, {
        password: newPassword,
      });

      if (resetError) {
        return new Response(
          JSON.stringify({ error: "পাসওয়ার্ড আপডেট করতে সমস্যা হয়েছে।" }),
          { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }

      // Record audit log
      await serviceClient.from("audit_logs").insert({
        user_id: callerUser.id,
        action: "RESET_PASSWORD",
        entity: "profiles",
        entity_id: targetUserId,
        details: {
          target_user_name: targetProfile.full_name,
          target_user_role: targetProfile.role,
        },
      });

      return new Response(
        JSON.stringify({
          success: true,
          message: `${targetProfile.full_name}-এর পাসওয়ার্ড সফলভাবে পরিবর্তন করা হয়েছে।`,
        }),
        { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    // -------------------------------------------------------------
    // ACTION: SET ACTIVE STATUS
    // -------------------------------------------------------------
    if (action === "set-active") {
      const targetUserId = body.userId;
      const isActive = Boolean(body.isActive);

      if (!targetUserId || typeof targetUserId !== "string") {
        return new Response(
          JSON.stringify({ error: "ব্যবহারকারী শনাক্তকারী (ID) পাওয়া যায়নি।" }),
          { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }

      if (targetUserId === callerUser.id) {
        return new Response(
          JSON.stringify({ error: "নিজের অ্যাকাউন্টের সক্রিয়/নিষ্ক্রিয় স্ট্যাটাস পরিবর্তন করা যাবে না।" }),
          { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }

      // Load target profile
      const { data: targetProfile, error: targetError } = await serviceClient
        .from("profiles")
        .select("id, role, union_id, full_name")
        .eq("id", targetUserId)
        .single();

      if (targetError || !targetProfile) {
        return new Response(
          JSON.stringify({ error: "ব্যবহারকারীকে খুঁজে পাওয়া যায়নি।" }),
          { status: 404, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }

      // Role check: union_admin can only toggle operators in own union
      if (callerProfile.role === "union_admin") {
        if (targetProfile.union_id !== callerProfile.union_id || targetProfile.role !== "operator") {
          return new Response(
            JSON.stringify({ error: "আপনি শুধুমাত্র আপনার ইউনিয়নের অপারেটরদের স্ট্যাটাস পরিবর্তন করতে পারবেন।" }),
            { status: 403, headers: { ...corsHeaders, "Content-Type": "application/json" } }
          );
        }
      }

      // Update profile
      const { error: updateError } = await serviceClient
        .from("profiles")
        .update({
          is_active: isActive,
          updated_at: new Date().toISOString(),
        })
        .eq("id", targetUserId);

      if (updateError) {
        return new Response(
          JSON.stringify({ error: "স্ট্যাটাস পরিবর্তন করতে ব্যর্থ হয়েছে।" }),
          { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }

      // Record audit log
      await serviceClient.from("audit_logs").insert({
        user_id: callerUser.id,
        action: isActive ? "ACTIVATE_USER" : "DEACTIVATE_USER",
        entity: "profiles",
        entity_id: targetUserId,
        details: {
          target_user_name: targetProfile.full_name,
          target_user_role: targetProfile.role,
          is_active: isActive,
        },
      });

      return new Response(
        JSON.stringify({
          success: true,
          message: `${targetProfile.full_name}-এর অ্যাকাউন্ট সফলভাবে ${isActive ? "সক্রিয়" : "নিষ্ক্রিয়"} করা হয়েছে।`,
          is_active: isActive,
        }),
        { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    return new Response(
      JSON.stringify({ error: "অননুমোদিত অ্যাকশন অনুরোধ।" }),
      { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  } catch (err: unknown) {
    console.error("Unhandled edge function exception:", err);
    return new Response(
      JSON.stringify({ error: "সার্ভারে অনাকাঙ্ক্ষিত ত্রুটি হয়েছে। অনুগ্রহ করে পরে আবার চেষ্টা করুন।" }),
      { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  }
});
