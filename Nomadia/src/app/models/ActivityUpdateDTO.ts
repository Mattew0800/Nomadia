export interface ActivityUpdateDTO{
  id: string;        // activityId
  tripId: string;    // tripId al que pertenece
  name: string;
  date: string;      // 'YYYY-MM-DD'
  description: string;
  cost: number;
  startTime: string; // 'HH:mm'
  endTime: string;
}
