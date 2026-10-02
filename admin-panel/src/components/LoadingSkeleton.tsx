'use client';

import React from 'react';

export const LoadingSkeleton: React.FC<{ rows?: number }> = ({ rows = 4 }) => {
  return (
    <div className="space-y-4 animate-pulse p-4">
      <div className="h-8 bg-gray-200 rounded-lg w-1/3"></div>
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        {[1, 2, 3, 4].map((i) => (
          <div key={i} className="h-28 bg-gray-100 rounded-xl border border-gray-200"></div>
        ))}
      </div>
      <div className="h-64 bg-gray-100 rounded-xl border border-gray-200"></div>
      <div className="space-y-2">
        {Array.from({ length: rows }).map((_, i) => (
          <div key={i} className="h-12 bg-gray-100 rounded-lg border border-gray-200"></div>
        ))}
      </div>
    </div>
  );
};
