'use client';

import React from 'react';
import { CertificateStatus } from '@/lib/types';
import { getStatusMeta } from '@/lib/bangla';

export const StatusBadge: React.FC<{ status: CertificateStatus | string; className?: string }> = ({
  status,
  className = '',
}) => {
  const meta = getStatusMeta(status);
  return (
    <span
      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold border ${meta.bg} ${className}`}
    >
      <span className="w-1.5 h-1.5 rounded-full bg-current mr-1.5 opacity-80" />
      {meta.label}
    </span>
  );
};
