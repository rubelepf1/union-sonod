'use client';

import React from 'react';
import { UserRole } from '@/lib/types';
import { getRoleMeta } from '@/lib/bangla';

export const RoleBadge: React.FC<{ role: UserRole | string; className?: string }> = ({
  role,
  className = '',
}) => {
  const meta = getRoleMeta(role);
  return (
    <span
      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${meta.bg} ${className}`}
    >
      {meta.label}
    </span>
  );
};
