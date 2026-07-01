import { createClient } from "@supabase/supabase-js";

interface Env {
  SUPABASE_URL: string;
  SUPABASE_SERVICE_ROLE_KEY: string;
  DO: Fetcher;
}

const CORS = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "GET, POST, PUT, DELETE, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type, Authorization",
  "Access-Control-Max-Age": "86400",
};

function json(data: unknown, status = 200): Response {
  return new Response(JSON.stringify(data), {
    status,
    headers: { ...CORS, "Content-Type": "application/json" },
  });
}

function getUserId(req: Request): string | null {
  return req.headers.get("X-Rork-User-Id");
}

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    const url = new URL(request.url);

    if (request.method === "OPTIONS") {
      return new Response(null, { status: 204, headers: CORS });
    }

    const supabase = createClient(env.SUPABASE_URL, env.SUPABASE_SERVICE_ROLE_KEY);

    // ── GET /api/me ──────────────────────────────────────────
    if (url.pathname === "/api/me" && request.method === "GET") {
      const userId = getUserId(request);
      if (!userId) return json({ error: "Unauthorized" }, 401);

      // Upsert profile
      await supabase.from("profiles").upsert({ id: userId }, { onConflict: "id" });

      // Find household membership
      const { data: member } = await supabase
        .from("household_members")
        .select("household_id, households(code, name)")
        .eq("user_id", userId)
        .maybeSingle();

      return json({
        user_id: userId,
        household: member
          ? { id: member.household_id, code: (member.households as any)?.code, name: (member.households as any)?.name }
          : null,
      });
    }

    // ── POST /api/households ─────────────────────────────────
    if (url.pathname === "/api/households" && request.method === "POST") {
      const userId = getUserId(request);
      if (!userId) return json({ error: "Unauthorized" }, 401);

      // Check if user already has a household
      const { data: existingMember } = await supabase
        .from("household_members")
        .select("household_id")
        .eq("user_id", userId)
        .maybeSingle();
      if (existingMember) {
        return json({ error: "Already in a household" }, 409);
      }

      const body = await request.json().catch(() => ({}));
      const name = (body.name as string) || "My Household";

      // Create household
      const { data: hh, error: hhErr } = await supabase
        .from("households")
        .insert({ name })
        .select()
        .single();
      if (hhErr || !hh) return json({ error: "Failed to create household" }, 500);

      // Add current user as member
      const { error: memberErr } = await supabase
        .from("household_members")
        .insert({ household_id: hh.id, user_id: userId });
      if (memberErr) return json({ error: "Failed to join household" }, 500);

      // Create empty budget snapshot
      await supabase.from("budget_snapshots").insert({
        household_id: hh.id,
        data: {},
        updated_by: userId,
      });

      return json({ household: { id: hh.id, code: hh.code, name: hh.name } }, 201);
    }

    // ── POST /api/households/join ────────────────────────────
    if (url.pathname === "/api/households/join" && request.method === "POST") {
      const userId = getUserId(request);
      if (!userId) return json({ error: "Unauthorized" }, 401);

      // Check if already in a household
      const { data: existingMember } = await supabase
        .from("household_members")
        .select("household_id")
        .eq("user_id", userId)
        .maybeSingle();
      if (existingMember) {
        return json({ error: "Already in a household" }, 409);
      }

      const body = await request.json().catch(() => ({}));
      const code = (body.code as string)?.trim().toUpperCase();
      if (!code) return json({ error: "Invite code required" }, 400);

      // Find household by code
      const { data: hh, error: hhErr } = await supabase
        .from("households")
        .select("*")
        .eq("code", code)
        .maybeSingle();
      if (hhErr || !hh) return json({ error: "Household not found" }, 404);

      // Join
      const { error: memberErr } = await supabase
        .from("household_members")
        .insert({ household_id: hh.id, user_id: userId });
      if (memberErr) {
        if (memberErr.code === "23505") return json({ error: "Already a member" }, 409);
        return json({ error: "Failed to join" }, 500);
      }

      return json({ household: { id: hh.id, code: hh.code, name: hh.name } }, 200);
    }

    // ── GET /api/data ────────────────────────────────────────
    if (url.pathname === "/api/data" && request.method === "GET") {
      const userId = getUserId(request);
      if (!userId) return json({ error: "Unauthorized" }, 401);

      // Find user's household
      const { data: member } = await supabase
        .from("household_members")
        .select("household_id")
        .eq("user_id", userId)
        .maybeSingle();
      if (!member) return json({ error: "No household" }, 404);

      // Get latest budget snapshot
      const { data: snapshot } = await supabase
        .from("budget_snapshots")
        .select("*")
        .eq("household_id", member.household_id)
        .order("updated_at", { ascending: false })
        .limit(1)
        .maybeSingle();

      return json({
        data: snapshot?.data ?? {},
        version: snapshot?.version ?? 0,
        updated_at: snapshot?.updated_at ?? null,
      });
    }

    // ── PUT /api/data ────────────────────────────────────────
    if (url.pathname === "/api/data" && request.method === "PUT") {
      const userId = getUserId(request);
      if (!userId) return json({ error: "Unauthorized" }, 401);

      const { data: member } = await supabase
        .from("household_members")
        .select("household_id")
        .eq("user_id", userId)
        .maybeSingle();
      if (!member) return json({ error: "No household" }, 404);

      const body = await request.json().catch(() => ({}));
      const newData = body.data;
      const clientVersion = body.version as number | undefined;

      if (newData === undefined) return json({ error: "Missing data field" }, 400);

      // Get current version for conflict detection
      const { data: current } = await supabase
        .from("budget_snapshots")
        .select("id, version")
        .eq("household_id", member.household_id)
        .order("updated_at", { ascending: false })
        .limit(1)
        .maybeSingle();

      if (current && clientVersion !== undefined && clientVersion < current.version) {
        return json(
          {
            error: "Conflict",
            server_version: current.version,
            server_data: current,
          },
          409,
        );
      }

      const nextVersion = (current?.version ?? 0) + 1;

      const { data: snapshot, error } = await supabase
        .from("budget_snapshots")
        .insert({
          household_id: member.household_id,
          data: newData,
          version: nextVersion,
          updated_by: userId,
        })
        .select("version, updated_at")
        .single();

      if (error) return json({ error: "Save failed" }, 500);

      return json({ version: snapshot.version, updated_at: snapshot.updated_at });
    }

    return json({ error: "Not found" }, 404);
  },
} satisfies ExportedHandler<Env>;
