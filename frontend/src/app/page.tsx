import AuthPanel from "@/components/auth/AuthPanel";
import Link from "next/link";

export default function Home() {
  return (
    <main className="min-h-screen bg-slate-50">
      <section className="grid min-h-screen lg:grid-cols-2">
        <div className="relative flex flex-col justify-between overflow-hidden bg-slate-900 px-8 py-10 text-white sm:px-12 lg:px-16">
          <div className="relative z-10">
            <div className="text-sm font-bold uppercase tracking-[0.18em] text-sky-300">
              Civic Transparency
            </div>

            <div className="mt-20 max-w-xl lg:mt-28">
              <p className="text-sm font-semibold uppercase tracking-[0.16em] text-sky-300">
                A better way to be heard
              </p>

              <h1 className="mt-5 text-5xl font-semibold leading-[1.05] tracking-tight sm:text-6xl">
                Make your city better.
              </h1>

              <p className="mt-7 max-w-lg text-lg leading-8 text-slate-300">
                See something that needs attention? Report civic issues,
                follow the action taken, and see how your community is
                progressing.
              </p>

              <div className="mt-10 grid max-w-lg grid-cols-1 gap-4 sm:grid-cols-3">
                <div className="rounded-xl border border-white/10 bg-white/5 p-4">
                  <div className="text-2xl">01</div>
                  <p className="mt-2 text-sm text-slate-300">Report an issue</p>
                </div>

                <div className="rounded-xl border border-white/10 bg-white/5 p-4">
                  <div className="text-2xl">02</div>
                  <p className="mt-2 text-sm text-slate-300">
                    Follow the action
                  </p>
                </div>

                <div className="rounded-xl border border-white/10 bg-white/5 p-4">
                  <div className="text-2xl">03</div>
                  <p className="mt-2 text-sm text-slate-300">
                    See public progress
                  </p>
                </div>
              </div>
            </div>
          </div>

          <div className="relative z-10 mt-16 flex flex-col gap-3 border-t border-white/10 pt-6 text-sm text-slate-400 sm:flex-row sm:items-center sm:justify-between">
            <span>Citizen Accountability Platform</span>
            <div className="flex flex-wrap items-center gap-x-4 gap-y-2">
              <Link href="/authority/login" className="font-medium text-sky-300 hover:text-white">
                Authority staff sign in
              </Link>
              <span>Your identity and sensitive information are protected.</span>
            </div>
          </div>
        </div>

        <div className="flex items-center justify-center px-6 py-10 sm:px-10 lg:px-16">
          <div className="w-full max-w-md">
            <AuthPanel />
          </div>
        </div>
      </section>
    </main>
  );
}