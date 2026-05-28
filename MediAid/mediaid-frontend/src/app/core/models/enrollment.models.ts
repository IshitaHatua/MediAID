export interface EnrollmentRequest { schemeId: number; }
export interface EnrollmentResponse { enrollmentId: number; citizenId: number; schemeId: number; enrollmentDate: string; expiryDate: string; status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'ACTIVE'; }
