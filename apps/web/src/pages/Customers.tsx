/* Türkçe Özet:
   Customer oluşturma formu (yalnızca UI). Kontrollü input'lar kullanır,
   submit işini dışarıdaki onSubmit callback'ine devreder. 422 ValidationError
   alan mesajlarını ilgili input altında gösterir; genel hata varsa üstte banner
   olarak yüzeye çıkarır. Her şey strict TypeScript ile uyumludur.
*/

import React, { useMemo, useState } from "react";

/** Formun dışarıya ilettiği değerler */
export type CreateCustomerValues = {
  name: string;
  email: string;
  password: string;
};

/** Bileşenin props'ları (tamamı tipli) */
export type CreateCustomerFormProps = {
  /** Submit sırasında butonu kilitlemek için (React Query mutation.isPending gibi) */
  pending?: boolean;
  /** Submit işlemini dışarıya devrederiz */
  onSubmit: (values: CreateCustomerValues) => void | Promise<void>;
  /**
   * Alan bazlı hata mesajları (422 için)
   * Örn: { name: "Name is required", email: "Invalid email" }
   */
  fieldErrors?: Record<string, string>;
  /** Alan dışı genel hata (ör. 500 / ağ hatası) */
  generalErrorMessage?: string;
  /** Başlangıç değerleri istersen opsiyonel olarak geçebilirsin */
  initialValues?: Partial<CreateCustomerValues>;
};

export const CreateCustomerForm: React.FC<CreateCustomerFormProps> = ({
  pending = false,
  onSubmit,
  fieldErrors,
  generalErrorMessage,
  initialValues,
}) => {
  // Controlled fields
  const [name, setName] = useState<string>(initialValues?.name ?? "");
  const [email, setEmail] = useState<string>(initialValues?.email ?? "");
  const [password, setPassword] = useState<string>(initialValues?.password ?? "");

  // Şifre görünür/gizli toggle
  const [showPassword, setShowPassword] = useState<boolean>(false);

  // Alan hatasını güvenli okumak için küçük yardımcı
  const f = useMemo(() => {
    const map = fieldErrors ?? {};
    return (key: keyof CreateCustomerValues): string | undefined => map[key as string];
  }, [fieldErrors]);

  // Native submit -> dışarıdaki onSubmit'e delege
  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    await onSubmit({ name, email, password });
  };

  return (
    <div className="rounded-xl border bg-white p-6 shadow-sm">
      <h2 className="mb-4 text-lg font-semibold">Create Customer</h2>

      {/* Genel hata banner'ı (örn. 500/ApiError) */}
      {generalErrorMessage ? (
        <div
          role="alert"
          className="mb-4 rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-800"
        >
          {generalErrorMessage}
        </div>
      ) : null}

      <form onSubmit={handleSubmit} className="space-y-4" noValidate>
        {/* Name */}
        <div>
          <label htmlFor="c-name" className="block text-sm font-medium text-gray-700">
            Name
          </label>
          <input
            id="c-name"
            name="name"
            type="text"
            autoComplete="name"
            value={name}
            onChange={(e) => setName(e.currentTarget.value)}
            placeholder="Jane Doe"
            className={`mt-1 w-full rounded-md border px-3 py-2 text-sm outline-none ${
              f("name") ? "border-red-300" : "border-gray-300"
            }`}
            aria-invalid={Boolean(f("name"))}
            aria-describedby={f("name") ? "c-name-err" : undefined}
          />
          {f("name") ? (
            <p id="c-name-err" className="mt-1 text-xs text-red-700">
              {f("name")}
            </p>
          ) : null}
        </div>

        {/* Email */}
        <div>
          <label htmlFor="c-email" className="block text-sm font-medium text-gray-700">
            Email
          </label>
          <input
            id="c-email"
            name="email"
            type="email"
            autoComplete="email"
            value={email}
            onChange={(e) => setEmail(e.currentTarget.value)}
            placeholder="jane@example.com"
            className={`mt-1 w-full rounded-md border px-3 py-2 text-sm outline-none ${
              f("email") ? "border-red-300" : "border-gray-300"
            }`}
            aria-invalid={Boolean(f("email"))}
            aria-describedby={f("email") ? "c-email-err" : undefined}
          />
          {f("email") ? (
            <p id="c-email-err" className="mt-1 text-xs text-red-700">
              {f("email")}
            </p>
          ) : null}
        </div>

        {/* Password */}
        <div>
          <label htmlFor="c-password" className="block text-sm font-medium text-gray-700">
            Password
          </label>

          <div className="mt-1 flex gap-2">
            <input
              id="c-password"
              name="password"
              type={showPassword ? "text" : "password"}
              autoComplete="new-password"
              value={password}
              onChange={(e) => setPassword(e.currentTarget.value)}
              placeholder="••••••••"
              className={`w-full rounded-md border px-3 py-2 text-sm outline-none ${
                f("password") ? "border-red-300" : "border-gray-300"
              }`}
              aria-invalid={Boolean(f("password"))}
              aria-describedby={f("password") ? "c-password-err" : undefined}
            />
            <button
              type="button"
              onClick={() => setShowPassword((s) => !s)}
              className="shrink-0 rounded-md border px-3 text-xs text-gray-700 hover:bg-gray-50"
              aria-pressed={showPassword}
              title={showPassword ? "Hide password" : "Show password"}
            >
              {showPassword ? "Hide" : "Show"}
            </button>
          </div>

          {f("password") ? (
            <p id="c-password-err" className="mt-1 text-xs text-red-700">
              {f("password")}
            </p>
          ) : null}

          {/* Küçük ipucu (şart değil, FE doğrulaması yapmıyoruz) */}
          <p className="mt-1 text-xs text-gray-500">
            At least 8 characters. Raw password is hashed on the server.
          </p>
        </div>

        {/* Submit */}
        <div className="pt-2">
          <button
            type="submit"
            disabled={pending}
            className="inline-flex items-center rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-blue-700 disabled:opacity-50"
          >
            {pending ? "Creating…" : "Create Customer"}
          </button>
        </div>
      </form>
    </div>
  );
};

export default CreateCustomerForm;
