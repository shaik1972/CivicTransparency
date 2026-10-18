"use client";

import { useEffect, useState, useSyncExternalStore } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import {
  clearSession,
  getAuthHeaders,
  getServerSessionSnapshot,
  getSessionSnapshot,
  readSession,
  subscribeToSession,
} from "@/lib/auth";

type ComplaintSummary = {
  complaintId: string;
  complaintReference: string;
  title: string;
  category: string;
  severity: string;
  state: string;
  city: string;
  createdAt: string;
};

type ComplaintStatusHistory = {
  complaintId: string;
  complaintReference: string;
  currentState: string;
  history: {
    transitionId: string;
    fromState: string | null;
    toState: string;
    reason: string;
    createdAt: string;
  }[];
};

const API_BASE_URL = "http://localhost:8080";

export default function DashboardPage() {
  const router = useRouter();
  const session = useSyncExternalStore(
    subscribeToSession,
    getSessionSnapshot,
    getServerSessionSnapshot
  );
  const [complaints, setComplaints] = useState<ComplaintSummary[]>([]);
  const [complaintsLoading, setComplaintsLoading] = useState(true);
  const [complaintsError, setComplaintsError] = useState("");
  const [statusHistory, setStatusHistory] = useState<ComplaintStatusHistory | null>(null);
  const [statusHistoryLoading, setStatusHistoryLoading] = useState(false);
  const [statusHistoryError, setStatusHistoryError] = useState("");

  useEffect(() => {
    const currentSession = readSession();
    if (!currentSession) {
      router.push("/");
    } else if (currentSession.roles.some((role) =>
      ["AUTHORITY_ADMIN", "AUTHORITY_OFFICER", "SYSTEM_ADMIN"].includes(role)
    )) {
      router.replace("/authority");
    } else if (!currentSession.citizenId) {
      router.replace("/");
    }
  }, [router]);

  useEffect(() => {
    if (!session?.citizenId) {
      return;
    }

    async function loadComplaints() {
      try {
        const response = await fetch(`${API_BASE_URL}/api/v1/complaints/mine`, {
          headers: getAuthHeaders(),
        });
        if (!response.ok) {
          throw new Error("Unable to load your complaints. Please sign in again and retry.");
        }
        setComplaints(await response.json());
      } catch (error) {
        setComplaintsError(error instanceof Error ? error.message : "Unable to load your complaints.");
      } finally {
        setComplaintsLoading(false);
      }
    }

    loadComplaints();
  }, [session]);

  function handleSignOut() {
    clearSession();
    router.push("/");
  }

  async function toggleStatusHistory(complaintId: string) {
    if (statusHistory?.complaintId === complaintId) {
      setStatusHistory(null);
      setStatusHistoryError("");
      return;
    }

    setStatusHistoryLoading(true);
    setStatusHistoryError("");
    try {
      const response = await fetch(
        `${API_BASE_URL}/api/v1/complaints/${complaintId}/status`,
        { headers: getAuthHeaders() }
      );
      if (!response.ok) {
        throw new Error("Unable to load status history. Please retry.");
      }
      setStatusHistory(await response.json());
    } catch (error) {
      setStatusHistoryError(error instanceof Error ? error.message : "Unable to load status history.");
      setStatusHistory(null);
    } finally {
      setStatusHistoryLoading(false);
    }
  }

  if (!session) {
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
                {complaints.length}
              </span>
            </div>
            <p className="mt-4 text-2xl font-bold text-slate-900">{complaints.length}</p>
            <p className="mt-1 text-xs text-slate-500">Total complaints submitted by you</p>
          </div>

          <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <div className="flex items-center justify-between">
              <span className="text-sm font-semibold text-slate-500">In Progress</span>
              <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-amber-50 text-amber-700 font-bold">
                {complaints.filter((complaint) => !["RESOLVED", "REJECTED", "CLOSED"].includes(complaint.state)).length}
              </span>
            </div>
            <p className="mt-4 text-2xl font-bold text-slate-900">{complaints.filter((complaint) => !["RESOLVED", "REJECTED", "CLOSED"].includes(complaint.state)).length}</p>
            <p className="mt-1 text-xs text-slate-500">Assigned to municipal authorities</p>
          </div>

          <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <div className="flex items-center justify-between">
              <span className="text-sm font-semibold text-slate-500">Resolved</span>
              <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-emerald-50 text-emerald-700 font-bold">
                {complaints.filter((complaint) => ["RESOLVED", "CLOSED"].includes(complaint.state)).length}
              </span>
            </div>
            <p className="mt-4 text-2xl font-bold text-slate-900">{complaints.filter((complaint) => ["RESOLVED", "CLOSED"].includes(complaint.state)).length}</p>
            <p className="mt-1 text-xs text-slate-500">Successfully closed complaints</p>
          </div>
        </section>

        <section className="mt-10">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-semibold text-slate-900">Your Reports</h3>
            {complaintsLoading && <span className="text-xs text-slate-500">Loading...</span>}
          </div>
          <div className="mt-4 overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
            {complaintsError && <p role="alert" className="p-6 text-sm text-red-700">{complaintsError}</p>}
            {complaints.length === 0 && !complaintsLoading ? (
              !complaintsError && <p className="p-6 text-sm text-slate-500">No complaints submitted yet.</p>
            ) : (
              complaints.slice(0, 5).map((complaint) => (
                <div key={complaint.complaintId} className="border-b border-slate-100 p-5 last:border-b-0">
                  <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                    <div>
                      <p className="text-sm font-semibold text-slate-900">{complaint.title}</p>
                      <p className="mt-1 text-xs text-slate-500">{complaint.complaintReference} · {complaint.category} · {complaint.city}</p>
                    </div>
                    <div className="flex items-center gap-3">
                      <span className="w-fit rounded-full bg-sky-100 px-3 py-1 text-xs font-semibold text-sky-800">{complaint.state}</span>
                      <button
                        type="button"
                        onClick={() => void toggleStatusHistory(complaint.complaintId)}
                        className="text-xs font-semibold text-sky-700 hover:text-sky-900"
                      >
                        {statusHistory?.complaintId === complaint.complaintId ? "Hide updates" : "View updates"}
                      </button>
                    </div>
                  </div>
                  {statusHistoryLoading && <p className="mt-3 text-xs text-slate-500">Loading status history...</p>}
                  {statusHistoryError && <p role="alert" className="mt-3 text-xs text-red-700">{statusHistoryError}</p>}
                  {statusHistory?.complaintId === complaint.complaintId && (
                    <div className="mt-4 border-l-2 border-sky-200 pl-4">
                      <p className="text-xs font-semibold text-slate-700">Current status: {statusHistory.currentState}</p>
                      <ol className="mt-3 space-y-3">
                        {statusHistory.history.map((entry) => (
                          <li key={entry.transitionId} className="text-xs text-slate-600">
                            <span className="font-semibold text-slate-800">
                              {entry.toState.replaceAll("_", " ")}
                            </span>
                            {entry.reason && <span> — {entry.reason}</span>}
                            <span className="mt-1 block text-slate-400">
                              {new Date(entry.createdAt).toLocaleString()}
                            </span>
                          </li>
                        ))}
                      </ol>
                    </div>
                  )}
                </div>
              ))
            )}
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
