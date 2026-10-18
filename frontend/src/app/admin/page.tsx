"use client";

import { useEffect, useState, useSyncExternalStore, type FormEvent } from "react";
import { useRouter } from "next/navigation";
import {
  clearSession,
  getAuthHeaders,
  getServerSessionSnapshot,
  getSessionSnapshot,
  readSession,
  subscribeToSession,
} from "@/lib/auth";

type Authority = { authorityId: string; code: string; name: string; status: string };
type Department = { departmentId: string; authorityId: string; code: string; name: string; status: string };
type StateOption = { stateId: string; code: string; name: string };
type CityOption = { cityId: string; stateId: string; code: string; name: string };
type SubcategoryOption = { subcategoryId: string; name: string };
type CategoryOption = { categoryId: string; name: string; subcategories: SubcategoryOption[] };

const API_BASE_URL = "http://localhost:8080";

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers: { ...getAuthHeaders(), ...init?.headers },
  });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new Error(body?.message ?? `Request failed (${response.status}).`);
  }
  return response.json();
}

async function fetchConfiguration() {
  return Promise.all([
    request<Authority[]>("/api/v1/admin/authorities"),
    request<Department[]>("/api/v1/admin/departments"),
    request<StateOption[]>("/api/v1/geography/states"),
    request<CategoryOption[]>("/api/v1/categories"),
  ]);
}

function fieldClass() {
  return "mt-1 block w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm";
}

export default function AdminPage() {
  const router = useRouter();
  const session = useSyncExternalStore(
    subscribeToSession,
    getSessionSnapshot,
    getServerSessionSnapshot
  );
  const [authorities, setAuthorities] = useState<Authority[]>([]);
  const [departments, setDepartments] = useState<Department[]>([]);
  const [states, setStates] = useState<StateOption[]>([]);
  const [cities, setCities] = useState<CityOption[]>([]);
  const [categories, setCategories] = useState<CategoryOption[]>([]);
  const [stateId, setStateId] = useState("");
  const [cityId, setCityId] = useState("");
  const [routingStateId, setRoutingStateId] = useState("");
  const [routingCityId, setRoutingCityId] = useState("");
  const [routingCities, setRoutingCities] = useState<CityOption[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  useEffect(() => {
    const current = readSession();
    if (!current) {
      router.replace("/authority/login");
    } else if (!current.roles.includes("SYSTEM_ADMIN")) {
      router.replace(current.roles.some((role) =>
        ["AUTHORITY_ADMIN", "AUTHORITY_OFFICER"].includes(role)
      ) ? "/authority" : "/dashboard");
    }
  }, [router]);

  useEffect(() => {
    if (!session?.roles.includes("SYSTEM_ADMIN")) return;
    let active = true;

    async function load() {
      try {
        const [authorityData, departmentData, stateData, categoryData] = await fetchConfiguration();
        if (!active) return;
        setAuthorities(authorityData);
        setDepartments(departmentData);
        setStates(stateData);
        setCategories(categoryData);
      } catch (loadError) {
        if (active) {
          setError(loadError instanceof Error ? loadError.message : "Could not load setup data.");
        }
      } finally {
        if (active) setLoading(false);
      }
    }

    void load();
    return () => {
      active = false;
    };
  }, [session]);

  async function submit(
    event: FormEvent<HTMLFormElement>,
    path: string,
    buildPayload: (form: FormData) => Record<string, unknown>,
    successMessage: string
  ) {
    event.preventDefault();
    const formElement = event.currentTarget;
    setError("");
    setNotice("");
    setSaving(true);
    try {
      const form = new FormData(formElement);
      await request(path, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(buildPayload(form)),
      });
      formElement.reset();
      setStateId("");
      setCityId("");
      setRoutingStateId("");
      setRoutingCityId("");
      setNotice(successMessage);
      const [authorityData, departmentData, stateData, categoryData] = await fetchConfiguration();
      setAuthorities(authorityData);
      setDepartments(departmentData);
      setStates(stateData);
      setCategories(categoryData);
    } catch (submitError) {
      setError(submitError instanceof Error ? submitError.message : "Could not save configuration.");
    } finally {
      setSaving(false);
    }
  }

  async function loadCities(selectedStateId: string, forRouting = false) {
    if (!selectedStateId) {
      if (forRouting) setRoutingCities([]);
      else setCities([]);
      return;
    }
    try {
      const result = await request<CityOption[]>(`/api/v1/geography/states/${selectedStateId}/cities`);
      if (forRouting) setRoutingCities(result);
      else setCities(result);
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : "Could not load cities.");
    }
  }

  function signOut() {
    clearSession();
    router.replace("/");
  }

  if (!session || !session.roles.includes("SYSTEM_ADMIN")) {
    return <main className="flex min-h-screen items-center justify-center text-sm text-slate-500">Loading system administration…</main>;
  }

  return (
    <main className="min-h-screen bg-slate-50 text-slate-900">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-4">
          <div>
            <p className="text-xs font-bold uppercase tracking-[0.16em] text-sky-700">Civic Transparency</p>
            <h1 className="mt-1 text-xl font-semibold">System administration</h1>
          </div>
          <button type="button" onClick={signOut} className="text-sm font-semibold text-slate-600 hover:text-slate-900">
            Sign out
          </button>
        </div>
      </header>

      <div className="mx-auto max-w-7xl px-6 py-8">
        <div className="rounded-xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-900">
          Use this restricted screen to provision authorities, departments, staff accounts, and complaint-routing rules.
          Staff self-registration is intentionally disabled.
        </div>
        {error && <p role="alert" className="mt-4 rounded-lg bg-red-50 p-4 text-sm text-red-700">{error}</p>}
        {notice && <p role="status" className="mt-4 rounded-lg bg-emerald-50 p-4 text-sm text-emerald-800">{notice}</p>}
        {loading && <p className="mt-4 text-sm text-slate-500">Loading configuration…</p>}

        <div className="mt-6 grid gap-6 lg:grid-cols-2">
          <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <h2 className="text-lg font-semibold">1. Register an authority</h2>
            <p className="mt-1 text-sm text-slate-500">Create the organization that owns one or more departments.</p>
            <form className="mt-5 space-y-4" onSubmit={(event) => void submit(
              event,
              "/api/v1/admin/authorities",
              (form) => ({
                code: String(form.get("code")).trim().toUpperCase(),
                name: String(form.get("name")).trim(),
                stateId: String(form.get("stateId") || "") || null,
                cityId: String(form.get("cityId") || "") || null,
                status: "ACTIVE",
              }),
              "Authority created."
            )}>
              <label className="block text-sm font-medium">Code<input name="code" required maxLength={50} className={fieldClass()} placeholder="CITY-WORKS" /></label>
              <label className="block text-sm font-medium">Authority name<input name="name" required maxLength={200} className={fieldClass()} /></label>
              <label className="block text-sm font-medium">State
                <select name="stateId" className={fieldClass()} value={stateId} onChange={(event) => {
                  setStateId(event.target.value);
                  setCityId("");
                  void loadCities(event.target.value);
                }}>
                  <option value="">All / not specified</option>
                  {states.map((state) => <option key={state.stateId} value={state.stateId}>{state.name}</option>)}
                </select>
              </label>
              <label className="block text-sm font-medium">City
                <select name="cityId" className={fieldClass()} value={cityId} onChange={(event) => setCityId(event.target.value)}>
                  <option value="">All / not specified</option>
                  {cities.map((city) => <option key={city.cityId} value={city.cityId}>{city.name}</option>)}
                </select>
              </label>
              <button disabled={saving} className="rounded-lg bg-sky-700 px-4 py-2 text-sm font-semibold text-white disabled:opacity-50">
                {saving ? "Saving…" : "Create authority"}
              </button>
            </form>
            <ul className="mt-5 space-y-2 border-t border-slate-100 pt-4 text-sm">
              {authorities.map((authority) => <li key={authority.authorityId}>{authority.name} <span className="text-slate-400">({authority.code}, {authority.status})</span></li>)}
            </ul>
          </section>

          <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <h2 className="text-lg font-semibold">2. Add a department</h2>
            <form className="mt-5 space-y-4" onSubmit={(event) => void submit(
              event,
              "/api/v1/admin/departments",
              (form) => ({
                authorityId: String(form.get("authorityId")),
                code: String(form.get("code")).trim().toUpperCase(),
                name: String(form.get("name")).trim(),
              }),
              "Department created."
            )}>
              <label className="block text-sm font-medium">Authority
                <select name="authorityId" required className={fieldClass()} defaultValue="">
                  <option value="" disabled>Select authority</option>
                  {authorities.filter((item) => item.status === "ACTIVE").map((item) => <option key={item.authorityId} value={item.authorityId}>{item.name}</option>)}
                </select>
              </label>
              <label className="block text-sm font-medium">Department code<input name="code" required maxLength={50} className={fieldClass()} placeholder="ROADS" /></label>
              <label className="block text-sm font-medium">Department name<input name="name" required maxLength={200} className={fieldClass()} placeholder="Roads Department" /></label>
              <button disabled={saving || authorities.length === 0} className="rounded-lg bg-sky-700 px-4 py-2 text-sm font-semibold text-white disabled:opacity-50">
                {saving ? "Saving…" : "Create department"}
              </button>
            </form>
            <ul className="mt-5 space-y-2 border-t border-slate-100 pt-4 text-sm">
              {departments.map((department) => <li key={department.departmentId}>{department.name} <span className="text-slate-400">({department.code}, {department.status})</span></li>)}
            </ul>
          </section>

          <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <h2 className="text-lg font-semibold">3. Register authority staff</h2>
            <p className="mt-1 text-sm text-slate-500">Share the initial password securely; staff can sign in at the authority portal.</p>
            <form className="mt-5 space-y-4" onSubmit={(event) => void submit(
              event,
              "/api/v1/admin/authority-users",
              (form) => ({
                email: String(form.get("email")).trim(),
                username: String(form.get("username")).trim(),
                password: String(form.get("password")),
                departmentId: String(form.get("departmentId")),
                employeeReference: String(form.get("employeeReference")).trim() || null,
                roleCode: String(form.get("roleCode")),
              }),
              "Staff login created. Give the staff member their username and temporary password privately."
            )}>
              <label className="block text-sm font-medium">Department
                <select name="departmentId" required className={fieldClass()} defaultValue="">
                  <option value="" disabled>Select department</option>
                  {departments.filter((item) => item.status === "ACTIVE").map((item) => <option key={item.departmentId} value={item.departmentId}>{item.name}</option>)}
                </select>
              </label>
              <label className="block text-sm font-medium">Role
                <select name="roleCode" required className={fieldClass()} defaultValue="AUTHORITY_OFFICER">
                  <option value="AUTHORITY_OFFICER">Authority Officer</option>
                  <option value="AUTHORITY_ADMIN">Authority Administrator</option>
                </select>
              </label>
              <label className="block text-sm font-medium">Email<input name="email" type="email" required maxLength={320} className={fieldClass()} /></label>
              <label className="block text-sm font-medium">Username<input name="username" required maxLength={100} className={fieldClass()} /></label>
              <label className="block text-sm font-medium">Initial password<input name="password" type="password" required minLength={12} maxLength={128} className={fieldClass()} autoComplete="new-password" /></label>
              <label className="block text-sm font-medium">Employee reference (optional)<input name="employeeReference" maxLength={100} className={fieldClass()} /></label>
              <button disabled={saving || departments.length === 0} className="rounded-lg bg-sky-700 px-4 py-2 text-sm font-semibold text-white disabled:opacity-50">
                {saving ? "Saving…" : "Create staff login"}
              </button>
            </form>
          </section>

          <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <h2 className="text-lg font-semibold">4. Add a routing rule</h2>
            <p className="mt-1 text-sm text-slate-500">Choose a department and the location/category criteria for incoming complaints.</p>
            <form className="mt-5 space-y-4" onSubmit={(event) => void submit(
              event,
              "/api/v1/admin/routing-rules",
              (form) => ({
                name: String(form.get("name")).trim(),
                stateId: String(form.get("stateId") || "") || null,
                cityId: String(form.get("cityId") || "") || null,
                categoryId: String(form.get("categoryId") || "") || null,
                departmentId: String(form.get("departmentId")),
                priority: Number(form.get("priority")),
                active: true,
              }),
              "Routing rule created and activated."
            )}>
              <label className="block text-sm font-medium">Rule name<input name="name" required maxLength={200} className={fieldClass()} placeholder="Road complaints to Roads Department" /></label>
              <label className="block text-sm font-medium">Department
                <select name="departmentId" required className={fieldClass()} defaultValue="">
                  <option value="" disabled>Select department</option>
                  {departments.filter((item) => item.status === "ACTIVE").map((item) => <option key={item.departmentId} value={item.departmentId}>{item.name}</option>)}
                </select>
              </label>
              <label className="block text-sm font-medium">State
                <select name="stateId" className={fieldClass()} value={routingStateId} onChange={(event) => {
                  setRoutingStateId(event.target.value);
                  setRoutingCityId("");
                  void loadCities(event.target.value, true);
                }}>
                  <option value="">Any state</option>
                  {states.map((state) => <option key={state.stateId} value={state.stateId}>{state.name}</option>)}
                </select>
              </label>
              <label className="block text-sm font-medium">City
                <select name="cityId" className={fieldClass()} value={routingCityId} onChange={(event) => setRoutingCityId(event.target.value)}>
                  <option value="">Any city</option>
                  {routingCities.map((city) => <option key={city.cityId} value={city.cityId}>{city.name}</option>)}
                </select>
              </label>
              <label className="block text-sm font-medium">Category
                <select name="categoryId" className={fieldClass()} defaultValue="">
                  <option value="">Any category</option>
                  {categories.map((category) => <option key={category.categoryId} value={category.categoryId}>{category.name}</option>)}
                </select>
              </label>
              <label className="block text-sm font-medium">Priority<input name="priority" type="number" required defaultValue={10} className={fieldClass()} /></label>
              <button disabled={saving || departments.length === 0} className="rounded-lg bg-sky-700 px-4 py-2 text-sm font-semibold text-white disabled:opacity-50">
                {saving ? "Saving…" : "Create routing rule"}
              </button>
            </form>
          </section>
        </div>
      </div>
    </main>
  );
}
