import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

public class View extends JFrame implements Observer {
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JTextArea jobListArea, userListArea;
    private JTextField usernameField, passwordField, jobTitleField;
    private JTextArea jobDescArea;
    private JButton loginBtn, registerBtn, applyBtn, addJobBtn, logoutBtn;
    private JButton jobSeekerRegBtn, companyRegBtn, deleteUserBtn, deleteJobBtn;
    private JComboBox<String> jobSelection, userSelection, jobSelectionAdmin;
    private Model model;

    public View() {
        setTitle("Job Portal");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Login Panel
        JPanel loginPanel = new JPanel(new GridLayout(3, 2));
        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);
        loginBtn = new JButton("Login");
        registerBtn = new JButton("Register");
        loginPanel.add(new JLabel("Username:"));
        loginPanel.add(usernameField);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(passwordField);
        loginPanel.add(loginBtn);
        loginPanel.add(registerBtn);

        // Register Panel
        JPanel registerPanel = new JPanel(new GridLayout(4, 2));
        jobSeekerRegBtn = new JButton("Register as Job Seeker");
        companyRegBtn = new JButton("Register as Company");
        registerPanel.add(new JLabel("Username:"));
        registerPanel.add(usernameField); // Reusing usernameField
        registerPanel.add(new JLabel("Password:"));
        registerPanel.add(passwordField); // Reusing passwordField
        registerPanel.add(jobSeekerRegBtn);
        registerPanel.add(companyRegBtn);
        JButton backBtn = new JButton("Back");
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "login"));
        registerPanel.add(backBtn);

        // Job Seeker Panel
        JPanel jobSeekerPanel = new JPanel(new BorderLayout());
        jobListArea = new JTextArea(10, 40);
        jobListArea.setEditable(false);
        jobSelection = new JComboBox<>();
        applyBtn = new JButton("Apply for Selected Job");
        logoutBtn = new JButton("Logout");
        jobSeekerPanel.add(new JScrollPane(jobListArea), BorderLayout.CENTER);
        JPanel jobSeekerButtons = new JPanel();
        jobSeekerButtons.add(jobSelection);
        jobSeekerButtons.add(applyBtn);
        jobSeekerButtons.add(logoutBtn);
        jobSeekerPanel.add(jobSeekerButtons, BorderLayout.SOUTH);

        // Company Panel
        JPanel companyPanel = new JPanel(new BorderLayout());
        JTextArea companyJobList = new JTextArea(10, 40);
        companyJobList.setEditable(false);
        jobTitleField = new JTextField(20);
        jobDescArea = new JTextArea(5, 20);
        addJobBtn = new JButton("Add Job");
        JPanel companyInput = new JPanel(new GridLayout(3, 2));
        companyInput.add(new JLabel("Job Title:"));
        companyInput.add(jobTitleField);
        companyInput.add(new JLabel("Description:"));
        companyInput.add(new JScrollPane(jobDescArea));
        companyInput.add(addJobBtn);
        JButton companyLogoutBtn = new JButton("Logout");
        companyLogoutBtn.addActionListener(e -> cardLayout.show(mainPanel, "login"));
        companyInput.add(companyLogoutBtn);
        companyPanel.add(new JScrollPane(companyJobList), BorderLayout.CENTER);
        companyPanel.add(companyInput, BorderLayout.SOUTH);

        // Admin Panel
        JPanel adminPanel = new JPanel(new BorderLayout());
        userListArea = new JTextArea(5, 40);
        userListArea.setEditable(false);
        JTextArea adminJobList = new JTextArea(5, 40);
        adminJobList.setEditable(false);
        deleteUserBtn = new JButton("Delete Selected User");
        deleteJobBtn = new JButton("Delete Selected Job");
        userSelection = new JComboBox<>();
        jobSelectionAdmin = new JComboBox<>();
        JPanel adminButtons = new JPanel();
        adminButtons.add(userSelection);
        adminButtons.add(deleteUserBtn);
        adminButtons.add(jobSelectionAdmin);
        adminButtons.add(deleteJobBtn);
        JButton adminLogoutBtn = new JButton("Logout");
        adminLogoutBtn.addActionListener(e -> cardLayout.show(mainPanel, "login"));
        adminButtons.add(adminLogoutBtn);
        adminPanel.add(new JScrollPane(userListArea), BorderLayout.NORTH);
        adminPanel.add(new JScrollPane(adminJobList), BorderLayout.CENTER);
        adminPanel.add(adminButtons, BorderLayout.SOUTH);

        // Add panels to main panel
        mainPanel.add(loginPanel, "login");
        mainPanel.add(registerPanel, "register");
        mainPanel.add(jobSeekerPanel, "jobseeker");
        mainPanel.add(companyPanel, "company");
        mainPanel.add(adminPanel, "admin");

        add(mainPanel);
        cardLayout.show(mainPanel, "login");
    }

    // Observer Pattern: Update job list
    @Override
    public void update() {
        List<Model.Job> jobs = model.getJobs();
        jobListArea.setText("");
        jobSelection.removeAllItems();
        for (Model.Job job : jobs) {
            jobListArea.append(job.title + " - " + job.company + "\n" + job.description + "\n\n");
            jobSelection.addItem(job.id + ": " + job.title);
        }
    }

    // Setters for model
    public void setModel(Model model) {
        this.model = model;
    }

    // Getters for buttons
    public JButton getJobSeekerRegBtn() {
        return jobSeekerRegBtn;
    }

    public JButton getCompanyRegBtn() {
        return companyRegBtn;
    }

    // Methods to bind controller actions
    public void setLoginListener(ActionListener listener) {
        loginBtn.addActionListener(listener);
    }

    public void setRegisterListener(ActionListener listener) {
        registerBtn.addActionListener(e -> cardLayout.show(mainPanel, "register"));
        jobSeekerRegBtn.addActionListener(listener);
        companyRegBtn.addActionListener(listener);
    }

    public void setApplyListener(ActionListener listener) {
        applyBtn.addActionListener(listener);
    }

    public void setAddJobListener(ActionListener listener) {
        addJobBtn.addActionListener(listener);
    }

    public void setLogoutListener(ActionListener listener) {
        logoutBtn.addActionListener(listener);
    }

    public void setDeleteUserListener(ActionListener listener) {
        deleteUserBtn.addActionListener(listener);
    }

    public void setDeleteJobListener(ActionListener listener) {
        deleteJobBtn.addActionListener(listener);
    }

    // Getters for input fields
    public String getUsername() {
        return usernameField.getText();
    }

    public String getPassword() {
        return passwordField.getText();
    }

    public String getJobTitle() {
        return jobTitleField.getText();
    }

    public String getJobDescription() {
        return jobDescArea.getText();
    }

    public int getSelectedJobId() {
        String selected = (String) jobSelection.getSelectedItem();
        return selected != null ? Integer.parseInt(selected.split(":")[0]) : -1;
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    public void switchPanel(String panel) {
        cardLayout.show(mainPanel, panel);
    }

    public void updateCompanyJobs(List<Model.Job> jobs) {
        JTextArea companyJobList = (JTextArea) ((JScrollPane) ((JPanel) mainPanel.getComponent(3)).getComponent(0))
                .getViewport().getView();
        companyJobList.setText("");
        for (Model.Job job : jobs) {
            companyJobList.append(job.title + " - " + job.status + "\n" + job.description + "\n\n");
        }
    }

    public void updateAdminView(List<Model.User> users, List<Model.Job> jobs) {
        userListArea.setText("");
        userSelection.removeAllItems();
        for (Model.User user : users) {
            userListArea.append(user.username + " (" + user.role + ")\n");
            userSelection.addItem(user.id + ": " + user.username);
        }
        JTextArea adminJobList = (JTextArea) ((JScrollPane) ((JPanel) mainPanel.getComponent(4)).getComponent(1))
                .getViewport().getView();
        adminJobList.setText("");
        jobSelectionAdmin.removeAllItems();
        for (Model.Job job : jobs) {
            adminJobList.append(job.title + " - " + job.company + " (" + job.status + ")\n");
            jobSelectionAdmin.addItem(job.id + ": " + job.title);
        }
    }

    public int getSelectedUserId() {
        String selected = (String) userSelection.getSelectedItem();
        return selected != null ? Integer.parseInt(selected.split(":")[0]) : -1;
    }

    public int getSelectedJobIdAdmin() {
        String selected = (String) jobSelectionAdmin.getSelectedItem();
        return selected != null ? Integer.parseInt(selected.split(":")[0]) : -1;
    }
}