export interface ActivityCreateDTO {
  name: string;
  date: string;          // 'YYYY-MM-DD'
  description: string;
  cost: number;
  startTime: string;     // 'HH:mm'
  endTime: string;       // 'HH:mm'
  tripStartDate?: string; // opcional (YYYY-MM-DD) para validación
  tripEndDate?: string;   // opcional (YYYY-MM-DD) para validación
}
