import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Singleton Pattern: Database connection
public class Model {
    private static Model instance;
    private Connection conn;
    private List<Observer> observers = new ArrayList<>();

    private Model() {
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/careerconnect",
                    "jobportal",
                    "pass");
        } catch (Exception e) {
            throw new RuntimeException("Database connection failed: " + e.getMessage());
        }
    }

    public static Model getInstance() {
        if (instance == null) {
            instance = new Model();
        }
        return instance;
    }

    // Observer Pattern: Notify observers of changes
    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    private void notifyObservers() {
        for (Observer observer : observers) {
            observer.update();
        }
    }

    // User Management
    public String registerUser(String username, String password, String role) {
        if (!role.equals("jobseeker") && !role.equals("company")) {
            return "Invalid role: Only 'jobseeker' or 'company' allowed";
        }
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return "Username and password cannot be empty";
        }
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);
            stmt.executeUpdate();
            return "Success";
        } catch (SQLException e) {
            return "Registration failed: " + e.getMessage();
        }
    }

    public User loginUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt("id"), rs.getString("username"), rs.getString("role"));
            }
        } catch (SQLException e) {
            // Handle error
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(new User(rs.getInt("id"), rs.getString("username"), rs.getString("role")));
            }
        } catch (SQLException e) {
            // Handle error
        }
        return users;
    }

    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    // Job Management
    public List<Job> getJobs() {
        List<Job> jobs = new ArrayList<>();
        String sql = "SELECT j.id, j.title, j.description, u.username AS company, j.status " +
                "FROM jobs j JOIN users u ON j.company_id = u.id WHERE j.status = 'Open'";
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                jobs.add(new Job(rs.getInt("id"), rs.getString("title"),
                        rs.getString("description"), rs.getString("company"),
                        rs.getString("status")));
            }
        } catch (SQLException e) {
            // Handle error
        }
        return jobs;
    }

    public List<Job> getJobsByCompany(int companyId) {
        List<Job> jobs = new ArrayList<>();
        String sql = "SELECT j.id, j.title, j.description, u.username AS company, j.status " +
                "FROM jobs j JOIN users u ON j.company_id = u.id WHERE j.company_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, companyId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                jobs.add(new Job(rs.getInt("id"), rs.getString("title"),
                        rs.getString("description"), rs.getString("company"),
                        rs.getString("status")));
            }
        } catch (SQLException e) {
            // Handle error
        }
        return jobs;
    }

    public boolean addJob(int companyId, String title, String description) {
        String sql = "INSERT INTO jobs (title, description, company_id, status) VALUES (?, ?, ?, 'Open')";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setInt(3, companyId);
            stmt.executeUpdate();
            notifyObservers();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean deleteJob(int jobId) {
        String sql = "DELETE FROM jobs WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, jobId);
            stmt.executeUpdate();
            notifyObservers();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean applyForJob(int jobseekerId, int jobId) {
        String sql = "INSERT INTO applications (job_id, jobseeker_id) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, jobId);
            stmt.setInt(2, jobseekerId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    // Data Classes
    public static class User {
        int id;
        String username;
        String role;

        User(int id, String username, String role) {
            this.id = id;
            this.username = username;
            this.role = role;
        }
    }

    public static class Job {
        int id;
        String title;
        String description;
        String company;
        String status;

        Job(int id, String title, String description, String company, String status) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.company = company;
            this.status = status;
        }
    }
}

// Observer Interface
interface Observer {
    void update();
}