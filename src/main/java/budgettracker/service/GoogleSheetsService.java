package budgettracker.service;

import budgettracker.model.Transaction;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;

import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class GoogleSheetsService {

    // Inject spreadsheet ID from application.properties
    @Value("${google.sheets.spreadsheetId}")
    private String spreadsheetId;

    // Inject path to credentials file from application.properties
    @Value("${google.sheets.credentialsPath}")
    private String credentialsPath;

    private Sheets sheetsService;
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String APPLICATION_NAME = "BudgetTrackerApp";
    private static final String SHEET_NAME = "Sheet1"; // Assuming your data is on 'Sheet1'

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    /**
     * Initializes the Google Sheets service client after properties are injected.
     */
    @PostConstruct
    public void init() throws IOException, GeneralSecurityException {
        // Authenticate using the service account credentials
        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(credentialsPath))
                .createScoped(Collections.singletonList(SheetsScopes.SPREADSHEETS));

        // Build the Sheets service client
        sheetsService = new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
        System.out.println("Google Sheets Service initialized successfully.");
    }

    /**
     * Fetches all transactions from the Google Sheet.
     * Assumes columns: Date, Description, Amount, Type
     */
    public List<Transaction> getAllTransactions() throws IOException {
        String range = SHEET_NAME + "!A2:D"; // Start from A2 to skip headers, D is the last column
        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();

        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        List<Transaction> transactions = new ArrayList<>();

        // Map sheet rows to Transaction objects
        if (values != null && !values.isEmpty()) {
            for (List<Object> row : values) {
                // Filter out null rows (equivalent to first filter(Objects::nonNull))
                if (row == null) {
                    continue;
                }

                try {
                    // Ensure row has enough elements to avoid IndexOutOfBoundsException
                    if (row.size() < 4) {
                        System.err.println("Skipping malformed row: " + row);
                        continue; // Skip this row if it's incomplete
                    }
                    LocalDate date = LocalDate.parse(row.get(0).toString(), DATE_FORMATTER);
                    String description = row.get(1).toString();
                    double amount = Double.parseDouble(row.get(2).toString());
                    String type = row.get(3).toString();
                    transactions.add(new Transaction(date, description, amount, type));
                } catch (Exception e) {
                    System.err.println("Error parsing row: " + row + ". Error: " + e.getMessage());
                    // In a traditional loop, we just skip the problematic row
                    throw e;
                }
            }
        }
        return transactions;
    }

    /**
     * Adds a new transaction to the Google Sheet.
     */
    public void addTransaction(Transaction transaction) throws IOException {
        String range = SHEET_NAME +"!A1"; // Append to the first empty row
        ValueRange body = new ValueRange()
                .setValues(Collections.singletonList(transaction.toRowData())); // One row to append

        AppendValuesResponse result = sheetsService.spreadsheets().values()
                .append(spreadsheetId, range, body)
                .setValueInputOption("RAW") // "RAW" means exact values, "USER_ENTERED" means treated as if typed by user
                .execute();
        System.out.println("Transaction added. Updates: " + result.getUpdates().getUpdatedRows() + " rows.");
    }
}