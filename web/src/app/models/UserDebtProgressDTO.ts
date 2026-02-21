import { DebtDTO } from './DebtDTO';

export interface UserDebtProgressDTO {
  debts: DebtDTO[];
  totalDebts: number;
  settledDebts: number;
  pendingDebts: number;
  percentage: number;
}

