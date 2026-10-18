"use client";

import { useRouter } from "next/navigation";
import { useEffect, useState, type FormEvent } from "react";
import { saveSession } from "@/lib/auth";

type Mode = "login" | "register";

type AuthPanelProps = {
  initialMode?: Mode;
  showRegister?: boolean;
};

type StateOption = {
  stateId: string;
  code: string;
  name: string;
};

type CityOption = {
  cityId: string;
  stateId: string;
  code: string;
  name: string;
};

type RegistrationResponse = {
  citizenId: string;
  userId: string;
  username: string;
  publicAlias: string;
  profileStatus: string;
  accountStatus: string;
  roleCode: string;
};

type LoginResponse = {
  accessToken: string;
  tokenType: string;
  expiresAt: string;
  userId: string;
  citizenId: string | null;
  username: string | null;
  publicAlias: string | null;
  roles: string[];
};

const API_BASE_URL = "http://localhost:8080";

export default function AuthPanel({ initialMode = "register", showRegister = true }: AuthPanelProps) {
  const [mode, setMode] = useState<Mode>(initialMode);

  return (
    <section className="flex max-h-[calc(100vh-4rem)] flex-col overflow-hidden rounded-2xl border border-slate-200 bg-white p-6 shadow-sm sm:p-8">
      {showRegister && <div className="flex rounded-lg bg-slate-100 p-1">
        <button
          type="button"
          onClick={() => setMode("login")}
          className={`flex-1 rounded-md px-4 py-2.5 text-sm font-semibold transition ${
            mode === "login"
              ? "bg-white text-slate-900 shadow-sm"
              : "text-slate-500 hover:text-slate-900"
          }`}
        >
          Login
        </button>

        <button
          type="button"
          onClick={() => setMode("register")}
          className={`flex-1 rounded-md px-4 py-2.5 text-sm font-semibold transition ${
            mode === "register"
              ? "bg-white text-slate-900 shadow-sm"
              : "text-slate-500 hover:text-slate-900"
          }`}
        >
          Register
        </button>
      </div>}

      {showRegister && mode === "register" ? (
        <div className="min-h-0 flex-1 overflow-y-auto overflow-x-hidden pr-2">
          <RegistrationForm />
        </div>
      ) : (
        <LoginForm />
      )}
    </section>
  );
}

function LoginForm() {
  const router = useRouter();
  const [identifier, setIdentifier] = useState("");
  const [password, setPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");

    try {
      setSubmitting(true);
      const response = await fetch(`${API_BASE_URL}/api/v1/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ identifier, password }),
      });

      if (!response.ok) {
        const body = await response.json().catch(() => null);
        throw new Error(body?.message ?? "Unable to sign in.");
      }

      const data: LoginResponse = await response.json();
      saveSession({
        accessToken: data.accessToken,
        expiresAt: data.expiresAt,
        userId: data.userId,
        citizenId: data.citizenId,
        username: data.username,
        publicAlias: data.publicAlias,
        roles: data.roles,
      });

      const isAuthorityUser = data.roles.some((role) =>
        ["AUTHORITY_ADMIN", "AUTHORITY_OFFICER", "SYSTEM_ADMIN"].includes(role)
      );
      router.push(
        data.roles.includes("SYSTEM_ADMIN")
          ? "/admin"
          : isAuthorityUser
            ? "/authority"
            : "/dashboard"
      );
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unable to sign in.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="mt-8">
      <div>
        <h2 className="text-2xl font-semibold tracking-tight text-slate-900">
          Welcome back
        </h2>
        <p className="mt-2 text-sm leading-6 text-slate-500">
          Sign in to access your Civic Transparency account.
        </p>
      </div>

      {error && (
        <div role="alert" className="mt-6 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="mt-8 space-y-5">
        <Field
          label="Email or username"
          placeholder="you@example.com"
          value={identifier}
          onChange={setIdentifier}
          required
        />
        <Field
          label="Password"
          type="password"
          placeholder="Enter your password"
          value={password}
          onChange={setPassword}
          required
        />
        <button
          type="submit"
          disabled={submitting}
          className="w-full rounded-lg bg-sky-700 px-4 py-3 text-sm font-semibold text-white transition hover:bg-sky-800 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {submitting ? "Signing in..." : "Sign in"}
        </button>
        <p className="text-center text-xs leading-5 text-slate-500">
          Your session expires automatically to protect your account.
        </p>
      </form>
    </div>
  );
}

function RegistrationForm() {
  const [email, setEmail] = useState("");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [publicAlias, setPublicAlias] = useState("");
  const [phone, setPhone] = useState("");
  const [address, setAddress] = useState("");
  const [postalCode, setPostalCode] = useState("");

  const [states, setStates] = useState<StateOption[]>([]);
  const [cities, setCities] = useState<CityOption[]>([]);
  const [selectedStateId, setSelectedStateId] = useState("");
  const [selectedCityId, setSelectedCityId] = useState("");

  const [loadingStates, setLoadingStates] = useState(true);
  const [loadingCities, setLoadingCities] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const [error, setError] = useState("");
  const [success, setSuccess] = useState<RegistrationResponse | null>(null);

  useEffect(() => {
    async function loadStates() {
      try {
        setLoadingStates(true);
        setError("");

        const response = await fetch(
          `${API_BASE_URL}/api/v1/geography/states`
        );

        if (!response.ok) {
          throw new Error("Unable to load states.");
        }

        const data: StateOption[] = await response.json();
        setStates(data);
      } catch (err) {
        setError(
          err instanceof Error
            ? err.message
            : "Unable to load states."
        );
      } finally {
        setLoadingStates(false);
      }
    }

    loadStates();
  }, []);

  useEffect(() => {
    if (!selectedStateId) {
       return;
    }

    async function loadCities() {
      try {
        setLoadingCities(true);
        setError("");
        
        const response = await fetch(
          `${API_BASE_URL}/api/v1/geography/states/${selectedStateId}/cities`
        );

        if (!response.ok) {
          throw new Error("Unable to load cities.");
        }

        const data: CityOption[] = await response.json();
        setCities(data);
      } catch (err) {
        setError(
          err instanceof Error
            ? err.message
            : "Unable to load cities."
        );
      } finally {
        setLoadingCities(false);
      }
    }

    loadCities();
  }, [selectedStateId]);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    setError("");
    setSuccess(null);

    if (password !== confirmPassword) {
      setError("Password and confirm password must match.");
      return;
    }

    if (!selectedCityId) {
      setError("Please select your city / town.");
      return;
    }

    try {
      setSubmitting(true);

      const response = await fetch(
        `${API_BASE_URL}/api/v1/citizens/register`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            email,
            username: username || null,
            password,
            confirmPassword,
            publicAlias,
            phone,
            address,
            cityId: selectedCityId,
            postalCode: postalCode || null,
          }),
        }
      );

      if (!response.ok) {
        let message = "Registration failed.";

        try {
          const errorBody = await response.json();

          if (errorBody.message) {
            message = errorBody.message;
          } else if (errorBody.error) {
            message = errorBody.error;
          }
        } catch {
          // Keep the default error message.
        }

        throw new Error(message);
      }

      const data: RegistrationResponse = await response.json();

      setSuccess(data);

      setEmail("");
      setUsername("");
      setPassword("");
      setConfirmPassword("");
      setPublicAlias("");
      setPhone("");
      setAddress("");
      setPostalCode("");
      setSelectedStateId("");
      setSelectedCityId("");
      setCities([]);
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Registration failed."
      );
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="mt-8">
      <div>
        <h2 className="text-2xl font-semibold tracking-tight text-slate-900">
          Create your account
        </h2>

        <p className="mt-2 text-sm leading-6 text-slate-500">
          Register to report civic issues and follow their progress.
        </p>
      </div>

      {error && (
        <div
          role="alert"
          className="mt-6 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700"
        >
          {error}
        </div>
      )}

      {success && (
        <div
          role="status"
          className="mt-6 rounded-lg border border-green-200 bg-green-50 px-4 py-3 text-sm text-green-700"
        >
          <p className="font-semibold">
            Registration successful.
          </p>

          <p className="mt-1">
            Welcome, {success.publicAlias}.
          </p>

          <p className="mt-1 text-xs">
            Your account has been created successfully.
          </p>
        </div>
      )}

      <form
        onSubmit={handleSubmit}
        className="mt-8 space-y-5"
      >
        <Field
          label="Email"
          type="email"
          placeholder="you@example.com"
          value={email}
          onChange={setEmail}
          required
        />

        <Field
          label="Username"
          placeholder="Optional"
          value={username}
          onChange={setUsername}
          required={false}
        />

        <Field
          label="Password"
          type="password"
          placeholder="Minimum 8 characters"
          value={password}
          onChange={setPassword}
          required
        />

        <Field
          label="Confirm password"
          type="password"
          placeholder="Enter your password again"
          value={confirmPassword}
          onChange={setConfirmPassword}
          required
        />

        <Field
          label="Public alias"
          placeholder="Name shown with your public activity"
          value={publicAlias}
          onChange={setPublicAlias}
          required
        />

        <Field
          label="Phone number"
          type="tel"
          placeholder="Your phone number"
          value={phone}
          onChange={setPhone}
          required
        />

        <div>
          <label
            htmlFor="address"
            className="block text-sm font-medium text-slate-700"
          >
            Address
            <span className="ml-1 text-red-600" aria-hidden="true">
             *
            </span>
          </label>

          <textarea
            id="address"
            name="address"
            rows={3}
            placeholder="Your address"
            value={address}
            onChange={(event) => setAddress(event.target.value)}
            required
            className="mt-1.5 block w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-sky-600 focus:ring-2 focus:ring-sky-100"
          />
        </div>

        <div className="grid gap-5 sm:grid-cols-2">
          <div>
            <label
              htmlFor="state"
              className="block text-sm font-medium text-slate-700"
            >
              State
              <span className="ml-1 text-red-600" aria-hidden="true">
               *
              </span>
            </label>

            <select
              id="state"
              name="state"
              value={selectedStateId}              
              onChange={(event) => {
                setSelectedStateId(event.target.value);
                setSelectedCityId("");
                setCities([]);
              }}
              required
              disabled={loadingStates}
              className="mt-1.5 block w-full rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-sm text-slate-900 outline-none transition focus:border-sky-600 focus:ring-2 focus:ring-sky-100 disabled:bg-slate-100"
            >
              <option value="">
                {loadingStates
                  ? "Loading states..."
                  : "Select state"}
              </option>

              {states.map((state) => (
                <option
                  key={state.stateId}
                  value={state.stateId}
                >
                  {state.name}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label
              htmlFor="city"
              className="block text-sm font-medium text-slate-700"
            >
              City / Town
              <span className="ml-1 text-red-600" aria-hidden="true">
               *
              </span>
            </label>

            <select
              id="city"
              name="city"
              value={selectedCityId}
              onChange={(event) =>
                setSelectedCityId(event.target.value)
              }
              required
              disabled={!selectedStateId || loadingCities}
              className="mt-1.5 block w-full rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-sm text-slate-900 outline-none transition focus:border-sky-600 focus:ring-2 focus:ring-sky-100 disabled:bg-slate-100"
            >
              <option value="">
                {loadingCities
                  ? "Loading cities..."
                  : !selectedStateId
                    ? "Select state first"
                    : "Select city / town"}
              </option>

              {cities.map((city) => (
                <option
                  key={city.cityId}
                  value={city.cityId}
                >
                  {city.name}
                </option>
              ))}
            </select>
          </div>
        </div>

        <Field
          label="Postal code"
          placeholder="Postal code"
          value={postalCode}
          onChange={setPostalCode}
          required={false}
        />

        <button
          type="submit"
          disabled={submitting}
          className="w-full rounded-lg bg-sky-700 px-4 py-3 text-sm font-semibold text-white transition hover:bg-sky-800 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {submitting ? "Creating account..." : "Create account"}
        </button>

        <p className="text-center text-xs leading-5 text-slate-500">
          Your address and contact information are protected and are not
          publicly displayed.
        </p>
      </form>
    </div>
  );
}

function Field({
  label,
  type = "text",
  placeholder,
  value,
  onChange,
  required = true,
}: {
  label: string;
  type?: string;
  placeholder?: string;
  value: string;
  onChange: (value: string) => void;
  required?: boolean;
}) {
  return (
    <div>
      <label
        htmlFor={label}
        className="block text-sm font-medium text-slate-700"
      >
        {label}

        {required ? (
          <span className="ml-1 text-red-600" aria-hidden="true">
            *
          </span>
        ) : (
          <span className="ml-1 font-normal text-slate-400">
            (optional)
          </span>
        )}
      </label>

      <input
        id={label}
        name={label}
        type={type}
        placeholder={placeholder}
        value={value}
        onChange={(event) => onChange(event.target.value)}
        required={required}
        className="mt-1.5 block w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-sky-600 focus:ring-2 focus:ring-sky-100"
      />
    </div>
  );
}
