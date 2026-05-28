export interface LoginRequest { email: string; password: string; }
export interface RegisterRequest { name: string; email: string; password: string; role: string; }
export interface AuthResponse { token: string; role: string; }
export interface ForgotPasswordRequest { email: string; }
export interface ResetPasswordRequest { email: string; otp: string; newPassword: string; }
export interface ApiResponse<T> { status: string; message: string; data: T; }
