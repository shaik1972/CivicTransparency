"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";

type UserSession = {
  publicAlias: string;
  username: string;
  roles: string[];
  userId: string;
  citizenId: string;
};

export default function DashboardPage() {
  const router = useRouter();
  const [session, setSession] = useState<UserSession | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = sessionStorage.getItem("civic.accessToken");
    if (!token) {
      router.push("/");
      return;
    }

    const publicAlias = sessionStorage.getItem("civic.publicAlias") || "Citizen";
    const username = sessionStorage.getItem("civic.username") || "";
    const userId = sessionStorage.getItem("civic.userId") || "";
    const citizenId = sessionStorage.getItem("civic.citizenId") || "";
    const rawRoles = sessionStorage.getItem("civic.roles");
    const roles: string[] = rawRoles ? JSON.parse(rawRoles) : ["CITIZEN"];

    setSession({ publicAlias, username, roles, userId, citizenId });
    setLoading(false);
  }, [router]);

  function handleSignOut() {
    sessionStorage.clear();
    router.push("/");
  }

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-slate-900 text-white">
        <div className="text-center">
          <div className="inline-block h-8 w-8 animate-spin rounded-full border-4 border-sky-400 border-t-transparent"></div>
          <p className="mt-4 text-sm text-slate-300">Loading citizen portal...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900">
      {/* Header / Navbar */}
      <header className="sticky top-0 z-30 border-b border-slate-200 bg-white/80 backdrop-blur-md">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-4">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-sky-700 font-bold text-white shadow-sm">
              CT
            </div>
            <div>
              <span className="text-sm font-bold uppercase tracking-[0.16em] text-sky-700">
                Civic Transparency
              </span>
              <h1 className="text-base font-semibold leading-tight text-slate-900">
                Citizen Portal
              </h1>
            </div>
          </div>

          <div className="flex items-center gap-4">
            <div className="hidden sm:flex flex-col items-end">
              <span className="text-sm font-semibold text-slate-900">
                {session?.publicAlias}
              </span>
              <span className="text-xs text-slate-500">
                @{session?.username || "citizen"}
              </span>
            </div>

            <span className="rounded-full bg-sky-100 px-3 py-1 text-xs font-semibold text-sky-800">
              {session?.roles[0] || "CITIZEN"}
            </span>

            <button
              onClick={handleSignOut}
              className="rounded-lg border border-slate-200 bg-white px-3.5 py-2 text-xs font-semibold text-slate-700 shadow-sm transition hover:bg-slate-50 hover:text-slate-900"
            >
              Sign out
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="mx-auto max-w-7xl px-6 py-10">
        {/* Welcome Hero */}
        <section className="rounded-2xl bg-gradient-to-r from-slate-900 via-slate-800 to-sky-950 p-8 text-white shadow-md sm:p-10">
          <div className="max-w-2xl">
            <span className="rounded-md bg-white/10 px-3 py-1 text-xs font-medium uppercase tracking-wider text-sky-300">
              Account Active
            </span>
            <h2 className="mt-4 text-3xl font-bold tracking-tight sm:text-4xl">
              Welcome back, {session?.publicAlias}.
            </h2>
            <p className="mt-3 text-base text-slate-300">
              Track your reported civic issues, monitor authority progress, and participate in keeping your city clean and safe.
            </p>
          </div>
        </section>

        {/* Overview Stats */}
        <section className="mt-8 grid grid-cols-1 gap-6 sm:grid-cols-3">
          <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <div className="flex items-center justify-between">
              <span className="text-sm font-semibold text-slate-500">Reported Issues</span>
              <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-sky-50 text-sky-700 font-bold">
                0
              </span>
            </div>
            <p className="mt-4 text-2xl font-bold text-slate-900">0</p>
            <p className="mt-1 text-xs text-slate-500">Total complaints submitted by you</p>
          </div>

          <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <div className="flex items-center justify-between">
              <span className="text-sm font-semibold text-slate-500">In Progress</span>
              <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-amber-50 text-amber-700 font-bold">
                0
              </span>
            </div>
            <p className="mt-4 text-2xl font-bold text-slate-900">0</p>
            <p className="mt-1 text-xs text-slate-500">Assigned to municipal authorities</p>
          </div>

          <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <div className="flex items-center justify-between">
              <span className="text-sm font-semibold text-slate-500">Resolved</span>
              <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-emerald-50 text-emerald-700 font-bold">
                0
              </span>
            </div>
            <p className="mt-4 text-2xl font-bold text-slate-900">0</p>
            <p className="mt-1 text-xs text-slate-500">Successfully closed complaints</p>
          </div>
        </section>

        {/* Quick Actions */}
        <section className="mt-10">
          <h3 className="text-lg font-semibold text-slate-900">Quick Actions</h3>
          <div className="mt-4 grid grid-cols-1 gap-6 sm:grid-cols-3">
            <div className="group flex flex-col justify-between rounded-2xl border border-slate-200 bg-white p-6 shadow-sm transition hover:border-sky-500 hover:shadow-md">
              <div>
                <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-sky-700 text-white font-bold text-xl">
                  +
                </div>
                <h4 className="mt-4 font-semibold text-slate-900 group-hover:text-sky-700">
                  Report a Civic Issue
                </h4>
                <p className="mt-2 text-sm text-slate-500">
                  Submit potholes, road damage, garbage accumulation, or streetlight failures with photos.
                </p>
              </div>
              <Link
                href="/report"
                className="mt-6 w-full inline-block rounded-lg bg-sky-600 text-center py-2.5 text-xs font-semibold text-white shadow-sm hover:bg-sky-500"
              >
                Report Issue
              </Link>
            </div>

            <div className="group flex flex-col justify-between rounded-2xl border border-slate-200 bg-white p-6 shadow-sm transition hover:border-sky-500 hover:shadow-md">
              <div>
                <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-slate-900 text-white font-bold text-xl">
                  📊
                </div>
                <h4 className="mt-4 font-semibold text-slate-900 group-hover:text-sky-700">
                  Public Dashboard
                </h4>
                <p className="mt-2 text-sm text-slate-500">
                  View city-wide resolution statistics, ward performance, and active transparency metrics.
                </p>
              </div>
              <button
                disabled
                className="mt-6 w-full rounded-lg bg-slate-100 py-2.5 text-xs font-semibold text-slate-500 cursor-not-allowed"
              >
                Browse Metrics (Coming Next)
              </button>
            </div>

            <div className="group flex flex-col justify-between rounded-2xl border border-slate-200 bg-white p-6 shadow-sm transition hover:border-sky-500 hover:shadow-md">
              <div>
                <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-slate-900 text-white font-bold text-xl">
                  🛡️
                </div>
                <h4 className="mt-4 font-semibold text-slate-900 group-hover:text-sky-700">
                  Privacy & Profile
                </h4>
                <p className="mt-2 text-sm text-slate-500">
                  Your identity is protected. Operational location details are generalized for public safety.
                </p>
              </div>
              <button
                disabled
                className="mt-6 w-full rounded-lg bg-slate-100 py-2.5 text-xs font-semibold text-slate-500 cursor-not-allowed"
              >
                View Privacy Controls
              </button>
            </div>
          </div>
        </section>
      </main>
    </div>
  );
}
