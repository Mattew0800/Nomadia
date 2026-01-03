export interface TripCreate{
    name: string;
    startDate: string;
    endDate: string;
    description: string | null;
    type: string;
}
