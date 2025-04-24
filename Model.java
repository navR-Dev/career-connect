import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

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

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    private void notifyObservers() {
        for (Observer observer : observers) {
            observer.update();
        }
    }

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

    public Object[] loginUser(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return new Object[] { "Username and password cannot be empty", null };
        }
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User(rs.getInt("id"), rs.getString("username"), rs.getString("role"));
                return new Object[] { "Success", user };
            } else {
                return new Object[] { "Invalid username or password", null };
            }
        } catch (SQLException e) {
            return new Object[] { "Login failed: " + e.getMessage(), null };
        }
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

    public String addJob(int companyId, String title, String description) {
        if (title == null || title.trim().isEmpty() || description == null || description.trim().isEmpty()) {
            return "Job title and description cannot be empty";
        }
        String sql = "INSERT INTO jobs (title, description, company_id, status) VALUES (?, ?, ?, 'Open')";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setInt(3, companyId);
            stmt.executeUpdate();
            notifyObservers();
            return "Success";
        } catch (SQLException e) {
            return "Failed to add job: " + e.getMessage();
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

    public boolean closeJob(int jobId) {
        String sql = "UPDATE jobs SET status = 'Closed' WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, jobId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                notifyObservers();
                return true;
            }
            return false;
        } catch (SQLException e) {
            return false;
        }
    }

    public String applyForJob(int jobseekerId, int jobId, File resumeFile) {
        if (resumeFile == null || !resumeFile.exists()) {
            return "Resume file is required";
        }
        try (FileInputStream fis = new FileInputStream(resumeFile)) {
            String sql = "INSERT INTO applications (job_id, jobseeker_id, resume, status) VALUES (?, ?, ?, 'Pending')";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, jobId);
                stmt.setInt(2, jobseekerId);
                stmt.setBinaryStream(3, fis, (int) resumeFile.length());
                stmt.executeUpdate();
                notifyObservers();
                return "Success";
            }
        } catch (SQLException | IOException e) {
            return "Application failed: " + e.getMessage();
        }
    }

    public List<Application> getApplicationsByJob(int jobId) {
        List<Application> applications = new ArrayList<>();
        String sql = "SELECT a.id, a.job_id, a.jobseeker_id, u.username, a.status, a.resume " +
                "FROM applications a JOIN users u ON a.jobseeker_id = u.id WHERE a.job_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, jobId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                byte[] resumeData = rs.getBytes("resume");
                applications.add(new Application(
                        rs.getInt("id"),
                        rs.getInt("job_id"),
                        rs.getInt("jobseeker_id"),
                        rs.getString("username"),
                        rs.getString("status"),
                        resumeData));
            }
        } catch (SQLException e) {
            // Handle error
        }
        return applications;
    }

    public boolean updateApplicationStatus(int applicationId, String status) {
        if (!status.equals("Selected") && !status.equals("Rejected")) {
            return false;
        }
        String sql = "UPDATE applications SET status = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, applicationId);
            stmt.executeUpdate();
            notifyObservers();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public List<Application> getApplicationsByJobseeker(int jobseekerId) {
        List<Application> applications = new ArrayList<>();
        String sql = "SELECT a.id, a.job_id, j.title, u.username AS company, a.status " +
                "FROM applications a " +
                "JOIN jobs j ON a.job_id = j.id " +
                "JOIN users u ON j.company_id = u.id " +
                "WHERE a.jobseeker_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, jobseekerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                applications.add(new Application(
                        rs.getInt("id"),
                        rs.getInt("job_id"),
                        rs.getString("title"),
                        rs.getString("company"),
                        rs.getString("status")));
            }
        } catch (SQLException e) {
            // Handle error
        }
        return applications;
    }

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

    public static class Application {
        int id;
        int jobId;
        int jobseekerId;
        String jobseekerUsername;
        String jobTitle;
        String company;
        String status;
        byte[] resumeData;

        Application(int id, int jobId, int jobseekerId, String jobseekerUsername, String status, byte[] resumeData) {
            this.id = id;
            this.jobId = jobId;
            this.jobseekerId = jobseekerId;
            this.jobseekerUsername = jobseekerUsername;
            this.status = status;
            this.resumeData = resumeData;
        }

        Application(int id, int jobId, String jobTitle, String company, String status) {
            this.id = id;
            this.jobId = jobId;
            this.jobTitle = jobTitle;
            this.company = company;
            this.status = status;
        }
    }
}

interface Observer {
    void update();
}