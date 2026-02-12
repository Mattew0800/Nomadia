export interface ExpenseResponseDTO {
  id: number;
  name: string;
  note: string;
  totalAmount: number;
  activityId?: number;
  tripId: number;
  participants: {
    userId: number;
    amountPaid: number;
    amountOwned: number;
  }[];
}
