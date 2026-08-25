export type Role = 'ADMIN' | 'USER'

export interface User {
  loginId: string
  role: Role
}

export interface LoginRequest {
  loginId: string
  password: string
}

export interface LoginResponse {
  accessToken: string
  user: User
}

export interface ApiFieldError {
  field: string
  reason: string
}

export interface ApiErrorResponse {
  code: string
  message: string
  timestamp: string
  fieldErrors?: ApiFieldError[] | null
}
