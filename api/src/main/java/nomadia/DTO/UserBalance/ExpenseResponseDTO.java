package nomadia.DTO.UserBalance;

import lombok.Data;
import nomadia.Model.Expense;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ExpenseResponseDTO {

    private Long id;
    private String name;
    private String note;
    private BigDecimal totalAmount;
    private Long activityId;
    private List<ExpenseParticipantDTO> participants;

    public static ExpenseResponseDTO fromEntity(Expense expense) {
        ExpenseResponseDTO dto = new ExpenseResponseDTO();
        dto.setId(expense.getId());
        dto.setName(expense.getName());
        dto.setNote(expense.getNote());
        dto.setTotalAmount(expense.getTotalAmount());
        dto.setActivityId(expense.getActivity().getId());
        dto.setParticipants(
                expense.getParticipants().stream().map(p -> {
                    ExpenseParticipantDTO pDto = new ExpenseParticipantDTO();
                    pDto.setUserId(p.getUser().getId());
                    pDto.setAmountPaid(p.getAmountPaid());
                    return pDto;
                }).toList()
        );
        return dto;
    }
}
