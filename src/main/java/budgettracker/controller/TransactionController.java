package budgettracker.controller;

import budgettracker.model.Transaction;
import budgettracker.service.GoogleSheetsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/budgettracker")
public class TransactionController {

    @Autowired
    private GoogleSheetsService googleSheetsService;

    /**
     * Retrieves all transactions from the Google Sheet.
     * GET /api/transactions
     */
    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        try {
            List<Transaction> transactions = googleSheetsService.getAllTransactions();
            return ResponseEntity.ok(transactions);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null); // Or a custom error response
        }
    }

    /**
     * Adds a new transaction to the Google Sheet.
     * POST /api/transactions
     */
    @PostMapping("/transactions")
    public ResponseEntity<Transaction> addTransaction(@RequestBody Transaction transaction) {
        try {
            googleSheetsService.addTransaction(transaction);
            return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null); // Or a custom error response
        }
    }

    /**
     * Calculates and returns the current balance and summary by type (Income/Expense).
     * GET /api/summary
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        try {
            List<Transaction> transactions = googleSheetsService.getAllTransactions();
            double totalIncome = transactions.stream()
                    .filter(t -> "Income".equalsIgnoreCase(t.getType()))
                    .mapToDouble(Transaction::getAmount)
                    .sum();
            double totalExpense = transactions.stream()
                    .filter(t -> "Expense".equalsIgnoreCase(t.getType()))
                    .mapToDouble(Transaction::getAmount)
                    .sum();
            double balance = totalIncome - totalExpense;

            Map<String, Object> summary = new HashMap<>();
            summary.put("totalIncome", totalIncome);
            summary.put("totalExpense", totalExpense);
            summary.put("balance", balance);

            return ResponseEntity.ok(summary);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
}
