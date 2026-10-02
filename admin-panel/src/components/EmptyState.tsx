'use client';

import React from 'react';
import { FileQuestion } from 'lucide-react';

export const EmptyState: React.FC<{
  title: string;
  description?: string;
  actionText?: string;
  onAction?: () => void;
}> = ({ title, description, actionText, onAction }) => {
  return (
    <div className="flex flex-col items-center justify-center p-8 md:p-12 text-center bg-white rounded-2xl border border-dashed border-gray-300">
      <div className="w-14 h-14 rounded-full bg-emerald-50 text-emerald-700 flex items-center justify-center mb-4">
        <FileQuestion className="w-7 h-7" />
      </div>
      <h3 className="text-lg font-bold text-gray-800 mb-1">{title}</h3>
      {description && <p className="text-sm text-gray-500 max-w-sm mb-4">{description}</p>}
      {actionText && onAction && (
        <button
          onClick={onAction}
          className="px-4 py-2 bg-bdGreen-600 hover:bg-bdGreen-700 text-white rounded-lg text-sm font-medium shadow-sm transition"
        >
          {actionText}
        </button>
      )}
    </div>
  );
};
