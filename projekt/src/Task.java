import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

// Klasa koja predstavlja jedan zadatak
public class Task {
    // Zadatak
    private String title;
    // Kolegij
    private String subject;
    // Datum do kojeg je zadatak potrebno izvršiti
    private String dueDate;
    // Status zadatka
    private String status;

    // Konstruktor za stvaranje novog zadatka
    public Task(String title, String subject, String dueDate, String status) {
        this.title = title;
        this.subject = subject;
        this.dueDate = dueDate;
        this.status = status;
    }

    // Getter metode za pristup varijablama
    public String getTitle() { return title; }
    public String getSubject() { return subject; }
    public String getDueDate() { return dueDate; }
    public String getStatus() { return status; }

    // Setter metode za postavljanje vrijednosti varijabli
    public void setTitle(String title) { this.title = title; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public void setStatus(String status) { this.status = status; }

    // Vraćanje datuma
    public Date getDueDateAsDate() {
        try {
            // Pokušaj parsiranja datuma u formatu "dd.MM.yyyy"
            return new SimpleDateFormat("dd.MM.yyyy").parse(dueDate);
        } catch (ParseException e) {
            return new Date();
        }
    }

    // Formatirani prikaz zadatka za ispis
    @Override
    public String toString() {
        return String.format("%-20s | %-15s | %-10s | %s", title, subject, dueDate, status);
    }

    // Vraćanje zadatka za spremanje u datoteku
    public String toFileString() {
        return title + ";" + subject + ";" + dueDate + ";" + status;
    }

    // Stvaranje novog objekta Task iz linije teksta iz datoteke
    public static Task fromFileString(String line) {
        String[] parts = line.split(";");
        if (parts.length != 4) return null;
        return new Task(parts[0], parts[1], parts[2], parts[3]);
    }
}
