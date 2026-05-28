export interface SchemeRequest { name: string; description: string; eligibilityCriteria: string; benefits: string; maxCoverageAmount: number; validityYears: number; }
export interface SchemeResponse { schemeId: number; name: string; description: string; eligibilityCriteria: string; benefits: string; maxCoverageAmount: number; validityYears: number; status: string; }
export interface SchemeStatusUpdate { status: string; }
