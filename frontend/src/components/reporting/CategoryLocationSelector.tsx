"use client";

import { useEffect, useState } from "react";

const API_BASE_URL = "http://localhost:8080";

// ─── Types ──────────────────────────────────────────────────────────────────

type Subcategory = {
  subcategoryId: string;
  categoryId: string;
  code: string;
  name: string;
  description: string;
  defaultPriority: string;
};

type Category = {
  categoryId: string;
  code: string;
  name: string;
  description: string;
  iconName: string;
  displayOrder: number;
  subcategories: Subcategory[];
};

type Ward = {
  wardId: string;
  cityId: string;
  code: string;
  name: string;
};

type Locality = {
  localityId: string;
  wardId: string;
  name: string;
};

export type CategoryLocationSelection = {
  categoryId: string;
  categoryCode: string;
  categoryName: string;
  subcategoryId: string;
  subcategoryCode: string;
  subcategoryName: string;
  wardId: string;
  wardName: string;
  localityId: string;
  localityName: string;
  streetAddress: string;
  landmark: string;
};

type Props = {
  cityId: string; // UUID of the city (Bengaluru: 22222222-2222-4222-8222-222222222222)
  onSelectionChange?: (selection: CategoryLocationSelection | null) => void;
};

// ─── Icon Map ────────────────────────────────────────────────────────────────

const ICON_MAP: Record<string, string> = {
  road: "🛣️",
  trash: "🗑️",
  droplet: "💧",
  shield: "🛡️",
  school: "🏫",
  utensils: "🍽️",
};

// ─── Priority colour ─────────────────────────────────────────────────────────

function priorityBadge(priority: string) {
  const map: Record<string, string> = {
    HIGH: "bg-red-100 text-red-700",
    MEDIUM: "bg-amber-100 text-amber-700",
    LOW: "bg-green-100 text-green-700",
  };
  return map[priority] ?? "bg-slate-100 text-slate-600";
}

// ─── Component ───────────────────────────────────────────────────────────────

export default function CategoryLocationSelector({
  cityId,
  onSelectionChange
}: Props) {
  // Category state
  const [categories, setCategories] = useState<Category[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<Category | null>(null);
  const [selectedSubcategory, setSelectedSubcategory] = useState<Subcategory | null>(null);

  // Geography state
  const [wards, setWards] = useState<Ward[]>([]);
  const [selectedWard, setSelectedWard] = useState<Ward | null>(null);
  const [localities, setLocalities] = useState<Locality[]>([]);
  const [selectedLocality, setSelectedLocality] = useState<Locality | null>(null);

  // Address
  const [streetAddress, setStreetAddress] = useState("");
  const [landmark, setLandmark] = useState("");

  // Loading
  const [loadingCategories, setLoadingCategories] = useState(true);
  const [loadingWards, setLoadingWards] = useState(true);
  const [loadingLocalities, setLoadingLocalities] = useState(false);

  // ── Fetch categories on mount ──────────────────────────────────────────────
  useEffect(() => {
    async function fetchCategories() {
      try {
        const res = await fetch(`${API_BASE_URL}/api/v1/categories`);
        const data: Category[] = await res.json();
        setCategories(data);
      } finally {
        setLoadingCategories(false);
      }
    }
    fetchCategories();
  }, []);

  // ── Fetch wards on mount ───────────────────────────────────────────────────
  useEffect(() => {
    async function fetchWards() {
      try {
        const res = await fetch(
          `${API_BASE_URL}/api/v1/geography/cities/${cityId}/wards`
        );
        const data: Ward[] = await res.json();
        setWards(data);
      } finally {
        setLoadingWards(false);
      }
    }
    fetchWards();
  }, [cityId]);

  // ── Fetch localities when ward changes ────────────────────────────────────
  useEffect(() => {
    if (!selectedWard) {
      return;
    }
    async function fetchLocalities() {
      setLoadingLocalities(true);
      try {
        const res = await fetch(
          `${API_BASE_URL}/api/v1/geography/wards/${selectedWard!.wardId}/localities`
        );
        const data: Locality[] = await res.json();
        setLocalities(data);
      } finally {
        setLoadingLocalities(false);
      }
    }
    fetchLocalities();
  }, [selectedWard]);

  // ── Notify parent on any selection change ─────────────────────────────────
  useEffect(() => {
    if (
      selectedCategory &&
      selectedSubcategory &&
      selectedWard &&
      selectedLocality
    ) {
      onSelectionChange?.({
        categoryId: selectedCategory.categoryId,
        categoryCode: selectedCategory.code,
        categoryName: selectedCategory.name,
        subcategoryId: selectedSubcategory.subcategoryId,
        subcategoryCode: selectedSubcategory.code,
        subcategoryName: selectedSubcategory.name,
        wardId: selectedWard.wardId,
        wardName: selectedWard.name,
        localityId: selectedLocality.localityId,
        localityName: selectedLocality.name,
        streetAddress,
        landmark,
      });
    } else {
      onSelectionChange?.(null);
    }
  }, [
    selectedCategory,
    selectedSubcategory,
    selectedWard,
    selectedLocality,
    streetAddress,
    landmark,
    onSelectionChange,
  ]);

  // ─── Handlers ─────────────────────────────────────────────────────────────

  function handleCategoryClick(cat: Category) {
    setSelectedCategory(cat);
    setSelectedSubcategory(null);
  }

  function handleSubcategoryChange(subcategoryId: string) {
    const sub =
      selectedCategory?.subcategories.find(
        (s) => s.subcategoryId === subcategoryId
      ) ?? null;
    setSelectedSubcategory(sub);
  }

  function handleWardChange(wardId: string) {
    const ward = wards.find((w) => w.wardId === wardId) ?? null;
    setSelectedWard(ward);
    setLocalities([]);
    setSelectedLocality(null);
  }

  function handleLocalityChange(localityId: string) {
    const loc = localities.find((l) => l.localityId === localityId) ?? null;
    setSelectedLocality(loc);
  }

  // ─── Render ───────────────────────────────────────────────────────────────

  return (
    <div className="space-y-8">
      {/* ── Step 1: Issue Category ─────────────────────────────────────── */}
      <section>
        <h3 className="text-base font-semibold text-slate-900">
          1. Select Issue Category
        </h3>
        <p className="mt-1 text-sm text-slate-500">
          Choose the type of civic problem you want to report.
        </p>

        {loadingCategories ? (
          <div className="mt-4 flex items-center gap-2 text-sm text-slate-500">
            <div className="h-4 w-4 animate-spin rounded-full border-2 border-sky-500 border-t-transparent" />
            Loading categories...
          </div>
        ) : (
          <div className="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-3">
            {categories.map((cat) => (
              <button
                key={cat.categoryId}
                type="button"
                onClick={() => handleCategoryClick(cat)}
                className={`flex flex-col items-start rounded-xl border p-4 text-left transition hover:shadow-sm ${
                  selectedCategory?.categoryId === cat.categoryId
                    ? "border-sky-500 bg-sky-50 ring-1 ring-sky-500"
                    : "border-slate-200 bg-white hover:border-sky-300"
                }`}
              >
                <span className="text-2xl" role="img" aria-label={cat.name}>
                  {ICON_MAP[cat.iconName] ?? "📋"}
                </span>
                <span className="mt-2 text-sm font-semibold text-slate-900">
                  {cat.name}
                </span>
                <span className="mt-0.5 text-xs text-slate-500 line-clamp-2">
                  {cat.description}
                </span>
              </button>
            ))}
          </div>
        )}

        {/* Subcategory select */}
        {selectedCategory && (
          <div className="mt-4">
            <label
              htmlFor="subcategory"
              className="block text-sm font-medium text-slate-700"
            >
              Specific issue type
            </label>
            <select
              id="subcategory"
              className="mt-1 w-full rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-sm text-slate-900 shadow-sm focus:border-sky-500 focus:outline-none focus:ring-1 focus:ring-sky-500"
              value={selectedSubcategory?.subcategoryId ?? ""}
              onChange={(e) => handleSubcategoryChange(e.target.value)}
            >
              <option value="">— Select specific issue —</option>
              {selectedCategory.subcategories.map((sub) => (
                <option key={sub.subcategoryId} value={sub.subcategoryId}>
                  {sub.name}
                </option>
              ))}
            </select>
            {selectedSubcategory && (
              <div className="mt-2 flex items-center gap-2">
                <span
                  className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold ${priorityBadge(
                    selectedSubcategory.defaultPriority
                  )}`}
                >
                  {selectedSubcategory.defaultPriority} priority
                </span>
                <span className="text-xs text-slate-500">
                  {selectedSubcategory.description}
                </span>
              </div>
            )}
          </div>
        )}
      </section>

      {/* ── Step 2: Location ───────────────────────────────────────────── */}
      <section>
        <h3 className="text-base font-semibold text-slate-900">
          2. Select Location
        </h3>
        <p className="mt-1 text-sm text-slate-500">
          Help authorities pinpoint the exact location of the issue.
        </p>

        <div className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-2">
          {/* Ward */}
          <div>
            <label
              htmlFor="ward"
              className="block text-sm font-medium text-slate-700"
            >
              Ward / Area
            </label>
            <select
              id="ward"
              className="mt-1 w-full rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-sm text-slate-900 shadow-sm focus:border-sky-500 focus:outline-none focus:ring-1 focus:ring-sky-500"
              value={selectedWard?.wardId ?? ""}
              onChange={(e) => handleWardChange(e.target.value)}
            >
              <option value="">
                {loadingWards ? "Loading wards..." : "— Select ward —"}
              </option>
              {wards.map((ward) => (
                <option key={ward.wardId} value={ward.wardId}>
                  {ward.name}
                </option>
              ))}
            </select>
          </div>

          {/* Locality */}
          <div>
            <label
              htmlFor="locality"
              className="block text-sm font-medium text-slate-700"
            >
              Neighbourhood / Locality
            </label>
            <select
              id="locality"
              className="mt-1 w-full rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-sm text-slate-900 shadow-sm focus:border-sky-500 focus:outline-none focus:ring-1 focus:ring-sky-500 disabled:opacity-50"
              value={selectedLocality?.localityId ?? ""}
              onChange={(e) => handleLocalityChange(e.target.value)}
              disabled={!selectedWard || loadingLocalities}
            >
              <option value="">
                {loadingLocalities
                  ? "Loading localities..."
                  : !selectedWard
                  ? "Select a ward first"
                  : "— Select locality —"}
              </option>
              {localities.map((loc) => (
                <option key={loc.localityId} value={loc.localityId}>
                  {loc.name}
                </option>
              ))}
            </select>
          </div>

          {/* Street address */}
          <div className="sm:col-span-2">
            <label
              htmlFor="streetAddress"
              className="block text-sm font-medium text-slate-700"
            >
              Street Address / House Number{" "}
              <span className="text-slate-400">(optional)</span>
            </label>
            <input
              id="streetAddress"
              type="text"
              placeholder="e.g. 23, 5th Cross Road"
              className="mt-1 w-full rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-sm text-slate-900 shadow-sm placeholder:text-slate-400 focus:border-sky-500 focus:outline-none focus:ring-1 focus:ring-sky-500"
              value={streetAddress}
              onChange={(e) => setStreetAddress(e.target.value)}
            />
          </div>

          {/* Landmark */}
          <div className="sm:col-span-2">
            <label
              htmlFor="landmark"
              className="block text-sm font-medium text-slate-700"
            >
              Landmark <span className="text-slate-400">(optional)</span>
            </label>
            <input
              id="landmark"
              type="text"
              placeholder="e.g. Near Forum Mall, Opposite State Bank"
              className="mt-1 w-full rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-sm text-slate-900 shadow-sm placeholder:text-slate-400 focus:border-sky-500 focus:outline-none focus:ring-1 focus:ring-sky-500"
              value={landmark}
              onChange={(e) => setLandmark(e.target.value)}
            />
          </div>
        </div>
      </section>

      {/* ── Summary ────────────────────────────────────────────────────── */}
      {selectedCategory && selectedSubcategory && selectedWard && selectedLocality && (
        <section className="rounded-xl border border-sky-200 bg-sky-50 p-4">
          <h4 className="text-sm font-semibold text-sky-900">
            ✅ Selection Summary
          </h4>
          <ul className="mt-2 space-y-1 text-sm text-sky-800">
            <li>
              <strong>Category:</strong> {selectedCategory.name} →{" "}
              {selectedSubcategory.name}
            </li>
            <li>
              <strong>Location:</strong> {selectedLocality.name},{" "}
              {selectedWard.name}
            </li>
            {streetAddress && (
              <li>
                <strong>Address:</strong> {streetAddress}
              </li>
            )}
            {landmark && (
              <li>
                <strong>Landmark:</strong> {landmark}
              </li>
            )}
          </ul>
        </section>
      )}
    </div>
  );
}
