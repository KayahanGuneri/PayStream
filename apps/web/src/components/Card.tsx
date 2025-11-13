// Türkçe Özet: Tekrarlanan kart görünümlerini sadeleştiren görsel bileşen.
// JSX global namespace'e bağlı tiplemeler yerine React.ElementType kullanır.

import React from 'react';

type CardProps = React.PropsWithChildren<{
  className?: string;
  as?: React.ElementType;         // ✅ JSX.IntrinsicElements yerine
  title?: string;
  subtitle?: string;
}>;

export const Card: React.FC<CardProps> = ({
  as: Comp = 'div',
  className,
  title,
  subtitle,
  children,
}) => {
  return (
    <Comp className={['rounded-2xl border bg-white p-4 shadow-sm', className].filter(Boolean).join(' ')}>
      {(title || subtitle) && (
        <div className="mb-3">
          {title && <h3 className="text-base font-semibold">{title}</h3>}
          {subtitle && <p className="text-sm text-gray-600">{subtitle}</p>}
        </div>
      )}
      {children}
    </Comp>
  );
};
