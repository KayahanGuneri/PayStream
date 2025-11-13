// Türkçe Özet: Sayfa üstünde görsel başlık (hero) gösteren sade bileşen.
// Görsel + başlık + açıklama içerir. JSX namespace'e bağımlı değildir.

import React from 'react';

type HeroProps = {
  imageSrc: string;
  imageAlt: string;
  title: string;
  subtitle?: string;
};

export const Hero: React.FC<HeroProps> = ({ imageSrc, imageAlt, title, subtitle }) => {
  return (
    <div className="overflow-hidden rounded-2xl border bg-white shadow-sm">
      <img src={imageSrc} alt={imageAlt} className="h-48 w-full object-cover sm:h-64" />
      <div className="p-6">
        <h1 className="text-2xl font-bold">{title}</h1>
        {subtitle && <p className="mt-2 text-gray-600">{subtitle}</p>}
      </div>
    </div>
  );
};
