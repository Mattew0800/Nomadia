export interface DebtDTO {
  debtorId: number;
  debtorEmail: string;
  creditorId: number;
  creditorEmail: string;
  amount: number;
  settled: boolean;
}

