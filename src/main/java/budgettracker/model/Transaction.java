package budgettracker.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor // Generates a no-argument constructor
@AllArgsConstructor // Generates a constructor with all fields
public class Transaction {
    // Using String for date to match Google Sheet simple format, can convert to LocalDate internally
    @JsonFormat(pattern = "yyyy/MM/dd")
    private LocalDate date;
    private String description;
    private double amount; // Amount can be positive for income, negative for expense in calculation, or use a 'type' field
    private String type; // e.g., "Income", "Expense"

    private static final DateTimeFormatter SHEET_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");


    // Helper method to convert to List<Object> for Google Sheets API
    public List<Object> toRowData() {
        return Arrays.asList(
                this.date.format(SHEET_DATE_FORMATTER), // Format date as String "YYYY-MM-DD"
                this.description,
                this.amount,
                this.type
        );
    }
}