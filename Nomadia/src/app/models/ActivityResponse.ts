export interface ActivityResponseDTO {
  id: number;
  name: string;
  date: string;          // ISO o YYYY-MM-DD según tu back
  description: string;
  cost: number;
  startTime: string;     // 'HH:mm:ss' o 'HH:mm' (según tu back)
  endTime: string;
  tripId: number;
}
