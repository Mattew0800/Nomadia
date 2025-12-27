export interface ErrorResponse {
  status: number;
  message: string;
  timestamp: string;
  errors?: {
    [key: string]: string;
  };
}
