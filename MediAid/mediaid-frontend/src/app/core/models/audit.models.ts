export interface AuditLog { logId: number; userId: number; action: string; resource: string; timestamp: string; }
export interface AuditManagementLog { logId: number; userId: number; action: string; resource: string; details: string; timestamp: string; }
export interface FormalAuditRequest { officerId: number; scope: string; scopeEntityId: number; findings?: string; }
export interface FormalAuditResponse { auditId: number; officerId: number; scope: string; scopeEntityId: number; findings: string; createdAt: string; status: string; }
export interface FormalAuditUpdate { status: string; findings?: string; }
export interface UserResponse { userId: number; name: string; email: string; role: string; }
