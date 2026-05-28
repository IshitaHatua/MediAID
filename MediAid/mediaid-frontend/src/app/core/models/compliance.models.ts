export interface ComplianceRecordRequest { entityId: number; entityType: string; result: string; notes: string; }
export interface ComplianceRecordResponse { complianceId: number; entityId: number; entityType: string; result: string; notes: string; requestedBy: string; evaluatedAt: string; }
export interface ComplianceEvaluationRequest { entityId: number; entityType: string; requestedBy?: string; }
