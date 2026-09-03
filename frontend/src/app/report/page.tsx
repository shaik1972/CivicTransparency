"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import CategoryLocationSelector, {
  CategoryLocationSelection,
} from "@/components/reporting/CategoryLocationSelector";

export default function ReportPage() {
  const router = useRouter();
  const [selection, setSelection] = useState<CategoryLocationSelection | null>(null);

  // Bengaluru seeded UUID
  const cityId = "22222222-2222-4222-8222-222222222222";

  return (
    <div className="min-h-screen bg-slate-50 font-sans text-slate-900">
      {/* Header */}
      <header className="sticky top-0 z-10 border-b border-slate-200 bg-white/80 px-6 py-4 backdrop-blur">
        <div className="mx-auto flex max-w-5xl items-center justify-between">
          <div className="flex items-center gap-4">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-sky-600 text-lg font-bold text-white shadow-sm">
              CT
            </div>
            <h1 className="text-xl font-bold tracking-tight text-slate-900">
              Report Civic Issue
            </h1>
          </div>
          <Link
            href="/dashboard"
            className="text-sm font-medium text-slate-500 hover:text-slate-900"
          >
            Cancel
          </Link>
        </div>
      </header>

      {/* Main Content */}
      <main className="mx-auto max-w-3xl px-6 py-8">
        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm sm:p-8">
          <CategoryLocationSelector
            cityId={cityId}
            onSelectionChange={setSelection}
          />

          {/* Next Step / Submit Action (Placeholder for now) */}
          <div className="mt-8 flex justify-end border-t border-slate-100 pt-6">
            <button
              type="button"
              disabled={!selection}
              onClick={() => {
                if (selection) {
                  const params = new URLSearchParams({
                    categoryId: selection.categoryId,
                    cityId: cityId,
                  });
                  if (selection.subcategoryId) params.append("subcategoryId", selection.subcategoryId);
                  if (selection.wardId) params.append("wardId", selection.wardId);
                  
                  router.push(`/report/details?${params.toString()}`);
                }
              }}
              className="rounded-xl bg-sky-600 px-6 py-3 text-sm font-semibold text-white shadow-sm hover:bg-sky-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-sky-600 disabled:opacity-50 disabled:cursor-not-allowed transition"
            >
              Continue to Details →
            </button>
          </div>
        </div>
      </main>
    </div>
  );
}
