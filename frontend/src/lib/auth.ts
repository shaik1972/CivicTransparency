export type StoredSession = {
  publicAlias: string;
  username: string;
  roles: string[];
  userId: string;
  citizenId: string;
};

let cachedSessionKey: string | null = null;
let cachedSession: StoredSession | null = null;

export function getSessionSnapshot(): StoredSession | null {
  if (typeof window === "undefined") {
    return null;
  }

  const sessionKey = [
    "civic.accessToken",
    "civic.tokenExpiresAt",
    "civic.userId",
    "civic.citizenId",
    "civic.username",
    "civic.publicAlias",
    "civic.roles",
  ]
    .map((key) => sessionStorage.getItem(key) ?? "")
    .join("\u0000");

  if (sessionKey !== cachedSessionKey) {
    cachedSessionKey = sessionKey;
    cachedSession = readSession();
  }

  return cachedSession;
}

export const subscribeToSession = () => () => {};
export const getServerSessionSnapshot = () => null;

export function clearSession() {
  if (typeof window === "undefined") return;
  sessionStorage.removeItem("civic.accessToken");
  sessionStorage.removeItem("civic.tokenExpiresAt");
  sessionStorage.removeItem("civic.userId");
  sessionStorage.removeItem("civic.citizenId");
  sessionStorage.removeItem("civic.username");
  sessionStorage.removeItem("civic.publicAlias");
  sessionStorage.removeItem("civic.roles");
}

export function readSession(): StoredSession | null {
  if (typeof window === "undefined") return null;

  const accessToken = sessionStorage.getItem("civic.accessToken");
  if (!accessToken) return null;

  const expiresAtMs = Number(sessionStorage.getItem("civic.tokenExpiresAt") ?? "0");
  if (Number.isFinite(expiresAtMs) && expiresAtMs > 0 && expiresAtMs <= Date.now()) {
    clearSession();
    return null;
  }

  const rawRoles = sessionStorage.getItem("civic.roles");

  return {
    publicAlias: sessionStorage.getItem("civic.publicAlias") || "Citizen",
    username: sessionStorage.getItem("civic.username") || "",
    roles: rawRoles ? JSON.parse(rawRoles) : ["CITIZEN"],
    userId: sessionStorage.getItem("civic.userId") || "",
    citizenId: sessionStorage.getItem("civic.citizenId") || "",
  };
}

export function saveSession(payload: {
  accessToken: string;
  expiresAt: string;
  userId: string;
  citizenId: string | null;
  username?: string | null;
  publicAlias?: string | null;
  roles?: string[];
}) {
  if (typeof window === "undefined") return;

  sessionStorage.setItem("civic.accessToken", payload.accessToken);
  sessionStorage.setItem("civic.tokenExpiresAt", String(new Date(payload.expiresAt).getTime()));
  sessionStorage.setItem("civic.userId", payload.userId);
  sessionStorage.setItem("civic.citizenId", payload.citizenId ?? "");

  if (payload.username) sessionStorage.setItem("civic.username", payload.username);
  if (payload.publicAlias) sessionStorage.setItem("civic.publicAlias", payload.publicAlias);
  if (payload.roles) sessionStorage.setItem("civic.roles", JSON.stringify(payload.roles));
}

export function getAuthHeaders(): Record<string, string> {
  const token = typeof window !== "undefined" ? sessionStorage.getItem("civic.accessToken") : null;
  return token ? { Authorization: `Bearer ${token}` } : {};
}
