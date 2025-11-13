// src/components/Button.tsx
// Türkçe Özet: Erişilebilir (focus-visible) ve sade varyantlara sahip buton bileşeni.
// JSX namespace'ine bağımlı tip kullanmaz; React tipleriyle güvenli çalışır.

import React from 'react';

type ButtonProps = React.ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: 'primary' | 'ghost' | 'outline';
  isLoading?: boolean; // shows a generic "Please wait…" and disables the button
};

// Use forwardRef so parent components can focus() the button if needed
export const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(function Button(
  { variant = 'primary', isLoading = false, className, children, disabled, ...rest },
  ref
) {
  // Base visual style shared across variants
  const base =
    'inline-flex items-center justify-center rounded-xl px-4 py-2 text-sm font-medium transition ' +
    'focus:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed';

  // Variant-specific styles (keep colors gentle; minimal contrast changes)
  const tone =
    variant === 'primary'
      ? 'bg-blue-600 text-white hover:bg-blue-700 focus-visible:ring-blue-600'
      : variant === 'outline'
      ? 'border border-gray-300 bg-white text-gray-900 hover:bg-gray-50 focus-visible:ring-gray-400'
      : // ghost
        'bg-transparent text-gray-800 hover:bg-gray-100 focus-visible:ring-gray-400';

  return (
    <button
      ref={ref}
      className={[base, tone, className].filter(Boolean).join(' ')} // merge classes safely
      aria-busy={isLoading || undefined} // assistive tech hint
      disabled={disabled || isLoading}   // prevent clicks while loading
      {...rest}
    >
      {isLoading ? 'Please wait…' : children}
    </button>
  );
});
