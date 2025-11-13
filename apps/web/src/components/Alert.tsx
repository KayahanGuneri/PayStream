// Türkçe Özet: Bilgilendirme/uyarı/hata mesajları için sade Alert bileşeni.
// Varyantlara göre arkaplan, border ve yazı rengi değişir. JSX namespace'e bağımlı değildir.

import React from 'react';

type AlertProps = React.PropsWithChildren<{
  className?: string;
  title?: string;
  variant?: 'info' | 'warning' | 'error' | 'success';
}>;

export const Alert: React.FC<AlertProps> = ({ className, title, variant = 'info', children }) => {
  // Choose palette per variant (keep gentle colors)
  const palette =
    variant === 'warning'
      ? 'bg-amber-50 border-amber-200 text-amber-900'
      : variant === 'error'
      ? 'bg-red-50 border-red-200 text-red-900'
      : variant === 'success'
      ? 'bg-emerald-50 border-emerald-200 text-emerald-900'
      : 'bg-blue-50 border-blue-200 text-blue-900';

  return (
    <div
      role={variant === 'error' ? 'alert' : 'status'}
      className={[
        'rounded-xl border px-4 py-3 text-sm',
        palette,
        className,
      ]
        .filter(Boolean)
        .join(' ')}
    >
      {title && <div className="mb-1 font-semibold">{title}</div>}
      <div>{children}</div>
    </div>
  );
};
