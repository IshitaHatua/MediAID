export interface DisbursementRequest { claimId: number; amount: number; citizenId: number; schemeId: number; }
export interface DisbursementResponse { disbursementId: number; amount: number; date: string; status: string; claimId: number; citizenId: number; schemeId: number; }
