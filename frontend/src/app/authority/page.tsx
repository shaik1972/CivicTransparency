"use client";

import { useEffect, useState, useSyncExternalStore } from "react";
import { useRouter } from "next/navigation";
import {
  clearSession,
  getAuthHeaders,
  getServerSessionSnapshot,
  getSessionSnapshot,
  readSession,
  subscribeToSession,
} from "@/lib/auth";

type AuthorityComplaint = {
  complaintId: string;
  complaintReference: string;
  title: string;
  description: string;
  category: string;
  severity: string;
  state: string;
  city: string;
  createdAt: string;
  departmentId: string;
  departmentName: string;
};

type WorkflowTransition = {
  transitionId: string;
  fromState: string | null;
  toState: string;
  reason: string;
  createdAt: string;
};

const API_BASE_URL = "http://localhost:8080";
const NEXT_STATES: Record<string, string[]> = {
  ASSIGNED: ["IN_PROGRESS", "ON_HOLD", "RESOLVED", "REJECTED"],
  IN_PROGRESS: ["ON_HOLD", "RESOLVED", "REJECTED"],
  ON_HOLD: ["IN_PROGRESS", "RESOLVED", "REJECTED"],
  RESOLVED: ["CLOSED"],
};

export default function AuthorityPage() {
  const router = useRouter();
  const session = useSyncExternalStore(
    subscribeToSession,
    getSessionSnapshot,
    getServerSessionSnapshot
  );
  const [complaints, setComplaints] = useState<AuthorityComplaint[]>([]);
  const [selectedComplaintId, setSelectedComplaintId] = useState("");
  const [history, setHistory] = useState<WorkflowTransition[]>([]);
  const [nextState, setNextState] = useState("");
  const [reason, setReason] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  const selectedComplaint = complaints.find((complaint) => complaint.complaintId === selectedComplaintId);
  const availableStates = selectedComplaint ? NEXT_STATES[selectedComplaint.state] ?? [] : [];

  useEffect(() => {
    const current = readSession();
    if (!current) {
      router.replace("/authority/login");
      return;
    }
    if (current.roles.includes("SYSTEM_ADMIN")) {
      router.replace("/admin");
    } else if (!current.roles.some((role) =>
      ["AUTHORITY_ADMIN", "AUTHORITY_OFFICER"].includes(role)
    )) {
      router.replace("/dashboard");
    }
  }, [router]);

  useEffect(() => {
    if (!session) return;

    async function loadQueue() {
      setLoading(true);
      setError("");
      try {
        const response = await fetch(`${API_BASE_URL}/api/v1/authority/complaints`, {
          headers: getAuthHeaders(),
        });
        if (!response.ok) {
          throw new Error("Unable to load the assigned complaint queue.");
        }
        const queue: AuthorityComplaint[] = await response.json();
        setComplaints(queue);
        setSelectedComplaintId((current) =>
          queue.some((complaint) => complaint.complaintId === current)
            ? current
            : queue[0]?.complaintId ?? ""
        );
      } catch (loadError) {
        setError(loadError instanceof Error ? loadError.message : "Unable to load the assigned complaint queue.");
      } finally {
        setLoading(false);
      }
    }

    void loadQueue();
  }, [session]);

  useEffect(() => {
    if (!selectedComplaintId) return;

    async function loadHistory() {
      try {
        const response = await fetch(
          `${API_BASE_URL}/api/v1/authority/complaints/${selectedComplaintId}/workflow/history`,
          { headers: getAuthHeaders() }
        );
        if (!response.ok) {
          throw new Error("Unable to load the complaint's workflow history.");
        }
        setHistory(await response.json());
      } catch (historyError) {
        setError(historyError instanceof Error ? historyError.message : "Unable to load workflow history.");
        setHistory([]);
      }
    }

    void loadHistory();
  }, [selectedComplaintId]);

  async function updateStatus(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selectedComplaint || !nextState || !reason.trim()) return;

    setSaving(true);
    setError("");
    setNotice("");
    try {
      const response = await fetch(
        `${API_BASE_URL}/api/v1/authority/complaints/${selectedComplaint.complaintId}/workflow/transitions`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json", ...getAuthHeaders() },
          body: JSON.stringify({ toState: nextState, reason: reason.trim() }),
        }
      );
      if (!response.ok) {
        const body = await response.json().catch(() => null);
        throw new Error(body?.message ?? "Unable to update the complaint status.");
      }

      setComplaints((current) => current.map((complaint) =>
        complaint.complaintId === selectedComplaint.complaintId
          ? { ...complaint, state: nextState }
          : complaint
      ));
      const historyResponse = await fetch(
        `${API_BASE_URL}/api/v1/authority/complaints/${selectedComplaint.complaintId}/workflow/history`,
        { headers: getAuthHeaders() }
      );
      if (!historyResponse.ok) {
        throw new Error("Status was updated, but its history could not be refreshed.");
      }
      setHistory(await historyResponse.json());
      setNotice(`Complaint status updated to ${nextState.replaceAll("_", " ")}.`);
      setReason("");
      setNextState("");
    } catch (updateError) {
      setError(updateError instanceof Error ? updateError.message : "Unable to update the complaint status.");
    } finally {
      setSaving(false);
    }
  }

  function signOut() {
    clearSession();
    router.replace("/");
  }

  if (!session) {
    return <main className="flex min-h-screen items-center justify-center text-sm text-slate-500">Loading authority portal...</main>;
  }

  return (
    <main className="min-h-screen bg-slate-50 text-slate-900">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-4">
          <div>
            <p className="text-xs font-bold uppercase tracking-[0.16em] text-sky-700">Civic Transparency</p>
            <h1 className="mt-1 text-xl font-semibold">Authority Portal</h1>
          </div>
          <div className="flex items-center gap-4">
            <span className="rounded-full bg-sky-100 px-3 py-1 text-xs font-semibold text-sky-800">
              {session.roles.join(", ")}
            </span>
            <button type="button" onClick={signOut} className="text-sm font-semibold text-slate-600 hover:text-slate-900">
              Sign out
            </button>
          </div>
        </div>
      </header>

      <div className="mx-auto grid max-w-7xl gap-6 px-6 py-8 lg:grid-cols-[minmax(0,1fr)_minmax(0,1.4fr)]">
        <section className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-semibold">Assigned complaints</h2>
            {loading && <span className="text-xs text-slate-500">Loading…</span>}
          </div>
          {error && <p role="alert" className="mt-4 rounded-lg bg-red-50 p-3 text-sm text-red-700">{error}</p>}
          {!loading && complaints.length === 0 && !error && (
            <p className="mt-4 text-sm text-slate-500">No active complaints are assigned to your department.</p>
          )}
          <ul className="mt-4 divide-y divide-slate-100">
            {complaints.map((complaint) => (
              <li key={complaint.complaintId}>
                <button
                  type="button"
                  onClick={() => {
                    setSelectedComplaintId(complaint.complaintId);
                    setHistory([]);
                    setNextState("");
                    setReason("");
                    setNotice("");
                  }}
                  className={`w-full rounded-lg px-3 py-4 text-left transition ${
                    selectedComplaintId === complaint.complaintId ? "bg-sky-50" : "hover:bg-slate-50"
                  }`}
                >
                  <span className="block text-xs font-semibold text-sky-700">{complaint.complaintReference}</span>
                  <span className="mt-1 block text-sm font-semibold">{complaint.title}</span>
                  <span className="mt-1 block text-xs text-slate-500">
                    {complaint.category} · {complaint.city} · {complaint.state.replaceAll("_", " ")}
                  </span>
                </button>
              </li>
            ))}
          </ul>
        </section>

        <section className="space-y-6">
          {selectedComplaint ? (
            <>
              <article className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div>
                    <p className="text-xs font-semibold text-sky-700">{selectedComplaint.complaintReference}</p>
                    <h2 className="mt-1 text-xl font-semibold">{selectedComplaint.title}</h2>
                    <p className="mt-1 text-xs text-slate-500">
                      {selectedComplaint.category} · {selectedComplaint.severity} · {selectedComplaint.city}
                    </p>
                  </div>
                  <span className="rounded-full bg-amber-100 px-3 py-1 text-xs font-semibold text-amber-800">
                    {selectedComplaint.state.replaceAll("_", " ")}
                  </span>
                </div>
                <p className="mt-5 whitespace-pre-wrap text-sm leading-6 text-slate-700">{selectedComplaint.description}</p>
                <p className="mt-4 text-xs text-slate-400">
                  Assigned to {selectedComplaint.departmentName} · reported {new Date(selectedComplaint.createdAt).toLocaleString()}
                </p>
              </article>

              <article className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
                <h3 className="text-base font-semibold">Update status</h3>
                {availableStates.length > 0 ? (
                  <form className="mt-4 space-y-4" onSubmit={updateStatus}>
                    <label className="block text-sm font-medium">
                      New status
                      <select
                        required
                        value={nextState}
                        onChange={(event) => setNextState(event.target.value)}
                        className="mt-1 block w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                      >
                        <option value="">Choose a status</option>
                        {availableStates.map((state) => (
                          <option key={state} value={state}>{state.replaceAll("_", " ")}</option>
                        ))}
                      </select>
                    </label>
                    <label className="block text-sm font-medium">
                      Update note
                      <textarea
                        required
                        minLength={1}
                        maxLength={1000}
                        value={reason}
                        onChange={(event) => setReason(event.target.value)}
                        placeholder="Describe the work or reason for the status change"
                        className="mt-1 block w-full rounded-lg border border-slate-300 px-3 py-2"
                        rows={3}
                      />
                    </label>
                    <button
                      type="submit"
                      disabled={saving || !nextState || !reason.trim()}
                      className="rounded-lg bg-sky-700 px-4 py-2 text-sm font-semibold text-white hover:bg-sky-800 disabled:opacity-50"
                    >
                      {saving ? "Saving…" : "Save update"}
                    </button>
                  </form>
                ) : (
                  <p className="mt-3 text-sm text-slate-500">This complaint is in a final state and cannot be updated.</p>
                )}
                {notice && <p role="status" className="mt-4 text-sm text-emerald-700">{notice}</p>}
                {error && <p role="alert" className="mt-4 text-sm text-red-700">{error}</p>}
              </article>

              <article className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
                <h3 className="text-base font-semibold">Status history</h3>
                <ol className="mt-4 space-y-4 border-l-2 border-slate-200 pl-4">
                  {history.map((entry) => (
                    <li key={entry.transitionId}>
                      <p className="text-sm font-semibold">{entry.toState.replaceAll("_", " ")}</p>
                      {entry.reason && <p className="mt-1 text-sm text-slate-600">{entry.reason}</p>}
                      <p className="mt-1 text-xs text-slate-400">{new Date(entry.createdAt).toLocaleString()}</p>
                    </li>
                  ))}
                </ol>
              </article>
            </>
          ) : (
            <div className="rounded-2xl border border-dashed border-slate-300 bg-white p-8 text-sm text-slate-500">
              Select an assigned complaint to view details and update its status.
            </div>
          )}
        </section>
      </div>
    </main>
  );
}
