export type UserRole = 'super_admin' | 'union_admin' | 'operator';

export type CertificateStatus = 'draft' | 'generated' | 'printed' | 'signed' | 'rejected';

export interface Profile {
  id: string;
  role: UserRole;
  union_id: string | null;
  full_name: string;
  phone: string | null;
  is_active: boolean;
  created_at: string;
  unions?: Union | null;
}

export interface Union {
  id: string;
  name_bn: string;
  upazila: string;
  district: string;
  chairman_name: string;
  phone: string | null;
  email: string | null;
  logo_url: string | null;
  created_at?: string;
}

export interface CertificateField {
  id: string;
  label: string;
  type: 'text' | 'number' | 'date' | 'select' | 'textarea';
  required?: boolean;
  options?: string[];
  placeholder?: string;
}

export interface CertificateType {
  id: string;
  title_bn: string;
  english_name: string | null;
  category: string;
  fields: CertificateField[];
  template_bn: string;
  is_active: boolean;
}

export interface Certificate {
  id: string;
  union_id: string;
  type_id: string;
  created_by: string;
  data: Record<string, any>;
  serial_no: string | null;
  status: CertificateStatus;
  created_at: string;
  updated_at: string;
  deleted_at: string | null;
  certificate_types?: CertificateType | null;
  profiles?: Profile | null;
  unions?: Union | null;
}

export interface AuditLog {
  id: string;
  user_id: string;
  action: string;
  entity: string;
  entity_id: string | null;
  details: Record<string, any> | null;
  created_at: string;
  profiles?: Profile | null;
}
