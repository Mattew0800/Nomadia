import {ErrorResponse} from './ErrorResponse';

export interface AuthErrorResponse extends ErrorResponse {
  newToken?: string; // Propiedad opcional para el nuevo token
}
