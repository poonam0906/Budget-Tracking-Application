package budgettracker.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @JsonFormat(pattern = "yyyy/MM/dd")
    private LocalDate date;
    private String description;
    private double amount;
    private String type; // "Income", "Expense"

    private static final DateTimeFormatter SHEET_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");


    // Helper method to convert to List<Object> for Google Sheets API
    public List<Object> toRowData() {
        return Arrays.asList(
                this.date.format(SHEET_DATE_FORMATTER),
                this.description,
                this.amount,
                this.type
        );
    }
}