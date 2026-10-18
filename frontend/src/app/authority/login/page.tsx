import AuthPanel from "@/components/auth/AuthPanel";
import Link from "next/link";

export default function AuthorityLoginPage() {
  return (
    <main className="flex min-h-screen items-center justify-center bg-slate-900 px-5 py-10">
      <div className="w-full max-w-md">
        <div className="mb-6 text-center text-white">
          <p className="text-sm font-bold uppercase tracking-[0.18em] text-sky-300">Civic Transparency</p>
          <h1 className="mt-3 text-3xl font-semibold">Authority staff sign in</h1>
          <p className="mt-2 text-sm text-slate-300">
            Staff accounts are provisioned by an authorized system administrator.
          </p>
        </div>
        <AuthPanel initialMode="login" showRegister={false} />
        <p className="mt-5 text-center text-sm text-slate-300">
          <Link href="/" className="font-medium text-sky-300 hover:text-white">Back to citizen portal</Link>
        </p>
      </div>
    </main>
  );
}
