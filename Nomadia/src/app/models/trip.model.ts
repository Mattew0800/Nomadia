export interface TripCreateDTO {
    name: string;
    startDate: string;
    endDate: string;   
    description: string;
    type: string;
}

export interface TripResponseDTO {
    id: number;
    name: string;
}