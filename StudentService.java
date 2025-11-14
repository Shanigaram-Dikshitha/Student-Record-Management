package src;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * StudentService: manages list and persistent CSV storage.
 * Saves students.csv to user's home directory by default so data persists.
 */
public class StudentService {
    private final List<Student> students = new ArrayList<>();
    private final File storageFile;

    /** Default constructor: stores students.csv in the user's home folder. */
    public StudentService() {
        this("students.csv");
    }

    /** Constructor that accepts a filename (relative names are stored in user home). */
    public StudentService(String filename) {
        // Put persistence file in a stable location: user home folder
        File home = new File(System.getProperty("user.home"));
        this.storageFile = new File(home, filename);
        loadFromFile();
    }

    public List<Student> getAll() {
        return new ArrayList<>(students);
    }

    public void add(Student s) {
        students.add(s);
        saveToFile();
    }

    public void update(Student updated) {
        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).getId() == updated.getId()) {
                students.set(i, updated);
                saveToFile();
                return;
            }
        }
    }

    public void delete(int id) {
        students.removeIf(s -> s.getId() == id);
        saveToFile();
    }

    public boolean existsId(int id) {
        return students.stream().anyMatch(s -> s.getId() == id);
    }

    // Save the entire list to the stable storageFile (UTF-8)
    private void saveToFile() {
        // Ensure parent exists (user home always exists, but safe)
        File parent = storageFile.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();

        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(storageFile, false), StandardCharsets.UTF_8))) {
            for (Student s : students) {
                pw.printf("%d,%s,%d,%s%n",
                        s.getId(),
                        escapeCsv(s.getName()),
                        s.getAge(),
                        escapeCsv(s.getCourse()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load existing data from the stable storageFile
    private void loadFromFile() {
        students.clear();
        if (!storageFile.exists()) return;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(storageFile), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = splitCsvLine(line);
                if (parts.length >= 4) {
                    try {
                        int id = Integer.parseInt(parts[0]);
                        String name = parts[1];
                        int age = Integer.parseInt(parts[2]);
                        String course = parts[3];
                        students.add(new Student(id, name, age, course));
                    } catch (NumberFormatException ignored) { }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String escapeCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            s = s.replace("\"", "\"\"");
            return "\"" + s + "\"";
        }
        return s;
    }

    private String[] splitCsvLine(String line) {
        List<String> cols = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"'); // escaped quote
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    cur.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    cols.add(cur.toString());
                    cur.setLength(0);
                } else {
                    cur.append(c);
                }
            }
        }
        cols.add(cur.toString());
        return cols.toArray(new String[0]);
    }

    /**
     * Import CSV.
     * @param csvFile file to import from
     * @param merge   if true, merge entries: new IDs added, existing IDs updated; if false, overwrite entire data
     * @throws IOException
     */
    public void importCsv(File csvFile, boolean merge) throws IOException {
        List<Student> imported = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(csvFile), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = splitCsvLine(line);
                if (parts.length >= 4) {
                    try {
                        int id = Integer.parseInt(parts[0]);
                        String name = parts[1];
                        int age = Integer.parseInt(parts[2]);
                        String course = parts[3];
                        imported.add(new Student(id, name, age, course));
                    } catch (NumberFormatException ignored) { }
                }
            }
        }

        if (!merge) {
            // Overwrite behavior (old behavior)
            students.clear();
            students.addAll(imported);
        } else {
            // Merge behavior: add new, update existing by ID
            Map<Integer, Student> map = new HashMap<>();
            for (Student s : students) map.put(s.getId(), s);
            for (Student s : imported) {
                map.put(s.getId(), s); // imported replaces or adds
            }
            // Replace list with map values in insertion order of existing then imported:
            List<Student> merged = new ArrayList<>(map.values());
            // If you prefer sorted order by ID uncomment:
            // merged.sort(Comparator.comparingInt(Student::getId));
            students.clear();
            students.addAll(merged);
        }

        saveToFile();
    }

    /**
     * Export current data to chosen CSV file (overwrites target file).
     * @param csvFile destination file
     * @throws IOException
     */
    public void exportCsv(File csvFile) throws IOException {
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(csvFile, false), StandardCharsets.UTF_8))) {
            for (Student s : students) {
                pw.printf("%d,%s,%d,%s%n",
                        s.getId(),
                        escapeCsv(s.getName()),
                        s.getAge(),
                        escapeCsv(s.getCourse()));
            }
        }
    }

    /** Returns the full absolute path of the storage file used by the service (helpful for debugging) */
    public String getStorageFilePath() {
        return storageFile.getAbsolutePath();
    }
}
