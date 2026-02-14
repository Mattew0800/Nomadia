export interface CreateExpenseDTO {
  activityId?: number | null;
  tripId: number;
  name: string;
  note: string;
  totalAmount: number;
  payers: { userId: number; amountPaid: number }[];
  splits: { userId: number; amountOwed: number }[] | null;
  customSplit: boolean;
}
