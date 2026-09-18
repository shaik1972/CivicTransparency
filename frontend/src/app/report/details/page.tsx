"use client";

import { Suspense, useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { getAuthHeaders, readSession } from "@/lib/auth";

const API_BASE_URL = "http://localhost:8080";

function ReportDetailsForm() {
  const router = useRouter();
  const searchParams = useSearchParams();

  const [loading, setLoading] = useState(false);
  const [description, setDescription] = useState("");
  
  const categoryId = searchParams.get("categoryId");
  const subcategoryId = searchParams.get("subcategoryId");
  const cityId = searchParams.get("cityId");
  const wardId = searchParams.get("wardId");

  useEffect(() => {
    if (!categoryId || !cityId || !readSession()) {
      router.push("/report");
    }
  }, [categoryId, cityId, router]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      const session = readSession();
      const citizenId = session?.citizenId;
      if (!session || !citizenId) {
        router.push("/");
        return;
      }

      const payload = {
        citizenId,
        categoryId: categoryId,
        subcategoryId: subcategoryId,
        description: description,
        cityId: cityId,
        wardId: wardId,
        // Mocking evidence
        evidence: [
          {
            storageKey: `s3://civic-bucket/mock-${Date.now()}.jpg`,
            mediaType: "image/jpeg",
            fileSizeBytes: 204800,
            sha256: "dummyhash"
          }
        ]
      };

      const res = await fetch(`${API_BASE_URL}/api/v1/complaints`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          ...getAuthHeaders(),
        },
        body: JSON.stringify(payload)
      });

      if (res.ok) {
        const data = await res.json();
        router.push(`/dashboard?success=true&complaintId=${data.complaintId}`);
      } else {
        alert("Failed to submit complaint.");
      }
    } catch (err) {
      console.error(err);
      alert("Error submitting complaint.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="mx-auto max-w-3xl">
        <h1 className="text-3xl font-bold tracking-tight text-slate-900 mb-8">
          Complaint Details
        </h1>
        
        <form onSubmit={handleSubmit} className="space-y-8 bg-white p-8 rounded-2xl shadow-sm border border-slate-200">
          <div>
            <label htmlFor="description" className="block text-sm font-medium leading-6 text-slate-900">
              Description of the Issue
            </label>
            <div className="mt-2">
              <textarea
                id="description"
                name="description"
                rows={4}
                required
                className="block w-full rounded-md border-0 py-1.5 text-slate-900 shadow-sm ring-1 ring-inset ring-slate-300 placeholder:text-slate-400 focus:ring-2 focus:ring-inset focus:ring-sky-600 sm:text-sm sm:leading-6"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="Please describe the issue in detail..."
              />
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium leading-6 text-slate-900">
              Evidence (Photos / Videos)
            </label>
            <div className="mt-2 flex justify-center rounded-lg border border-dashed border-slate-900/25 px-6 py-10">
              <div className="text-center">
                <svg className="mx-auto h-12 w-12 text-slate-300" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
                  <path fillRule="evenodd" d="M1.5 6a2.25 2.25 0 012.25-2.25h16.5A2.25 2.25 0 0122.5 6v12a2.25 2.25 0 01-2.25 2.25H3.75A2.25 2.25 0 011.5 18V6zM3 16.06V18c0 .414.336.75.75.75h16.5A.75.75 0 0021 18v-1.94l-2.69-2.689a1.5 1.5 0 00-2.12 0l-.88.879.97.97a.75.75 0 11-1.06 1.06l-5.16-5.159a1.5 1.5 0 00-2.12 0L3 16.061zm10.125-7.81a1.125 1.125 0 112.25 0 1.125 1.125 0 01-2.25 0z" clipRule="evenodd" />
                </svg>
                <div className="mt-4 flex text-sm leading-6 text-slate-600">
                  <label htmlFor="file-upload" className="relative cursor-pointer rounded-md bg-white font-semibold text-sky-600 focus-within:outline-none focus-within:ring-2 focus-within:ring-sky-600 focus-within:ring-offset-2 hover:text-sky-500">
                    <span>Upload a file</span>
                    <input id="file-upload" name="file-upload" type="file" className="sr-only" />
                  </label>
                  <p className="pl-1">or drag and drop</p>
                </div>
                <p className="text-xs leading-5 text-slate-600">PNG, JPG, MP4 up to 10MB</p>
                <p className="text-xs font-semibold text-amber-600 mt-2 bg-amber-50 inline-block px-2 py-1 rounded">Mock: File upload is simulated for now</p>
              </div>
            </div>
          </div>

          <div className="flex justify-end gap-x-3">
            <button
              type="button"
              onClick={() => router.back()}
              className="rounded-md bg-white px-3 py-2 text-sm font-semibold text-slate-900 shadow-sm ring-1 ring-inset ring-slate-300 hover:bg-slate-50"
            >
              Back
            </button>
            <button
              type="submit"
              disabled={loading}
              className="rounded-md bg-sky-600 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-sky-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-sky-600 disabled:opacity-50"
            >
              {loading ? "Submitting..." : "Submit Complaint"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default function ReportDetailsPage() {
  return (
    <Suspense fallback={<div className="min-h-screen bg-slate-50" />}>
      <ReportDetailsForm />
    </Suspense>
  );
}
