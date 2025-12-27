export interface putResponse {
  name?: string;
  email?: string;
  phone?: string;
  photoUrl?: string | null;
  nick?: string;
  about?: string;
  birth?: Date | null;
  age?: number;

  oldPassword?: string | null;
  newPassword?: string | null;
  newNewPassword?: string | null;

  role?: string; // opcional, si tu backend lo usa
}
