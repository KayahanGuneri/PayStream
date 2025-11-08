/* Türkçe Özet:
   Basit route hata bileşeni. errorElement olarak kullanılır.
*/
import React from 'react';

export const RouteError: React.FC = () => {
  // Keep it minimal to avoid dev overlay UX
  return (
    <div className="rounded-md border border-red-200 bg-red-50 p-4 text-red-800">
      Unexpected Application Error! Please navigate back or refresh.
    </div>
  );
};
