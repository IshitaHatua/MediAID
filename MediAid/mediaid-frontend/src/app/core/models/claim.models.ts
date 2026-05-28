export interface ClaimRequest { schemeId: number; claimAmount: number; description?: string; }
export interface ClaimResponse { claimId: number; citizenId: number; schemeId: number; claimAmount: number; claimDate: string; description: string; status: 'PENDING' | 'APPROVED' | 'REJECTED'; }
export interface ClaimStatusUpdate { status: string; }
export interface ClaimDocumentResponse { documentId: number; claimId: number; fileName: string; fileType: string; uploadDate: string; }
