export interface PaymentRequest { disbursementId: number; method: string; amount: number; citizenId: number; }
export interface PaymentResponse { paymentId: number; method: string; date: string; status: string; amount: number; disbursementId: number; citizenId: number; }
