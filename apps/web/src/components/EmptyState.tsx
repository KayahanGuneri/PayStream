// Türkçe Özet: Liste boşken gösterilecek yalın boş durum kartı.
// Opsiyonel ikon/görsel, başlık ve açıklama alır.

import React from 'react';
import { Button } from './Button';

type EmptyStateProps = {
  title?: string;
  description?: string;
  imageSrc?: string;
  imageAlt?: string;
  onAction?: () => void;
  actionLabel?: string;
};

export const EmptyState: React.FC<EmptyStateProps> = ({
  title = 'Nothing here yet',
  description,
  imageSrc,
  imageAlt = 'Empty',
  onAction,
  actionLabel = 'Refresh',
}) => {
  return (
    <div className="flex items-center justify-between rounded-2xl border bg-white p-4 shadow-sm">
      <div className="flex items-center gap-3">
        {imageSrc && (
          <img
            src={imageSrc}
            alt={imageAlt}
            className="h-10 w-10 object-contain opacity-80"
          />
        )}
        <div>
          <div className="text-sm font-medium text-gray-800">{title}</div>
          {description && <div className="text-sm text-gray-600">{description}</div>}
        </div>
      </div>
      {onAction && <Button variant="ghost" onClick={onAction}>{actionLabel}</Button>}
    </div>
  );
};
