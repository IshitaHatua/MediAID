export interface CitizenRequest { name: string; dob: string; gender: string; address: string; contactInfo: string; }
export interface CitizenResponse { citizenId: number; name: string; dob: string; gender: string; address: string; contactInfo: string; status: 'PENDING' | 'VERIFIED' | 'REJECTED' | 'SUSPENDED'; }
export interface CitizenDocumentResponse { documentId: number; docType: string; fileUri: string; uploadedDate: string; verificationStatus: 'PENDING' | 'VERIFIED' | 'REJECTED'; }
