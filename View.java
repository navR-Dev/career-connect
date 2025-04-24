import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.List;
import java.io.*;

public class View extends JFrame implements Observer {
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JTextArea jobListArea, userListArea;
    private JTextField usernameField, passwordField, jobTitleField, regUsernameField, regPasswordField;
    private JTextArea jobDescArea;
    private JButton loginBtn, registerBtn, applyBtn, addJobBtn, logoutBtn;
    private JButton jobSeekerRegBtn, companyRegBtn, deleteUserBtn, deleteJobBtn;
    private JComboBox<String> jobSelection, userSelection, jobSelectionAdmin;
    private JComboBox<String> applicationSelection;
    private JButton selectBtn, rejectBtn, viewResumeBtn;
    private JTextArea applicationsArea;
    private Model model;
    private File selectedResume;
    private JComboBox<String> companyJobSelection;
    private JButton closeJobBtn;

    public View() {
        setTitle("Job Portal");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Login Panel
        JPanel loginPanel = new JPanel(new BorderLayout());
        JLabel loginTitle = new JLabel("Login", SwingConstants.CENTER);
        loginTitle.setFont(new Font("Arial", Font.BOLD, 16));
        loginPanel.add(loginTitle, BorderLayout.NORTH);
        JPanel loginFields = new JPanel(new GridLayout(3, 2, 10, 10));
        loginFields.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);
        loginBtn = new JButton("Login");
        registerBtn = new JButton("Go to Register");
        loginFields.add(new JLabel("Username:"));
        loginFields.add(usernameField);
        loginFields.add(new JLabel("Password:"));
        loginFields.add(passwordField);
        loginFields.add(loginBtn);
        loginFields.add(registerBtn);
        loginPanel.add(loginFields, BorderLayout.CENTER);

        // Register Panel
        JPanel registerPanel = new JPanel(new BorderLayout());
        JLabel registerTitle = new JLabel("Register", SwingConstants.CENTER);
        registerTitle.setFont(new Font("Arial", Font.BOLD, 16));
        registerPanel.add(registerTitle, BorderLayout.NORTH);
        JPanel registerFields = new JPanel(new GridLayout(4, 2, 10, 10));
        registerFields.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        regUsernameField = new JTextField(15);
        regPasswordField = new JPasswordField(15);
        jobSeekerRegBtn = new JButton("Register as Job Seeker");
        companyRegBtn = new JButton("Register as Company");
        registerFields.add(new JLabel("New Username:"));
        registerFields.add(regUsernameField);
        registerFields.add(new JLabel("New Password:"));
        registerFields.add(regPasswordField);
        registerFields.add(jobSeekerRegBtn);
        registerFields.add(companyRegBtn);
        JButton backBtn = new JButton("Back to Login");
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "login"));
        registerFields.add(backBtn);
        registerPanel.add(registerFields, BorderLayout.CENTER);

        // Job Seeker Panel
        JPanel jobSeekerPanel = new JPanel(new BorderLayout());
        jobListArea = new JTextArea(10, 40);
        jobListArea.setEditable(false);
        jobSelection = new JComboBox<>();
        applyBtn = new JButton("Apply for Selected Job");
        logoutBtn = new JButton("Logout");
        applicationsArea = new JTextArea(5, 40);
        applicationsArea.setEditable(false);
        jobSeekerPanel.add(new JScrollPane(jobListArea), BorderLayout.CENTER);
        JPanel jobSeekerButtons = new JPanel();
        jobSeekerButtons.add(jobSelection);
        jobSeekerButtons.add(applyBtn);
        jobSeekerButtons.add(logoutBtn);
        jobSeekerPanel.add(jobSeekerButtons, BorderLayout.SOUTH);
        jobSeekerPanel.add(new JScrollPane(applicationsArea), BorderLayout.NORTH);

        // Company Panel
        JPanel companyPanel = new JPanel(new BorderLayout());
        JTextArea companyJobList = new JTextArea(10, 40);
        companyJobList.setEditable(false);
        jobTitleField = new JTextField(20);
        jobDescArea = new JTextArea(5, 20);
        addJobBtn = new JButton("Add Job");
        applicationSelection = new JComboBox<>();
        selectBtn = new JButton("Select for Interview");
        rejectBtn = new JButton("Reject Application");
        viewResumeBtn = new JButton("View Resume");
        companyJobSelection = new JComboBox<>();
        closeJobBtn = new JButton("Close Job");
        JPanel companyInput = new JPanel(new GridLayout(6, 2));
        companyInput.add(new JLabel("Job Title:"));
        companyInput.add(jobTitleField);
        companyInput.add(new JLabel("Description:"));
        companyInput.add(new JScrollPane(jobDescArea));
        companyInput.add(addJobBtn);
        companyInput.add(new JLabel("Applications:"));
        companyInput.add(applicationSelection);
        companyInput.add(selectBtn);
        companyInput.add(rejectBtn);
        companyInput.add(viewResumeBtn);
        companyInput.add(new JLabel("Select Job to Close:"));
        companyInput.add(companyJobSelection);
        companyInput.add(closeJobBtn);
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

    public void setModel(Model model) {
        this.model = model;
    }

    public JButton getJobSeekerRegBtn() {
        return jobSeekerRegBtn;
    }

    public JButton getCompanyRegBtn() {
        return companyRegBtn;
    }

    public void setLoginListener(ActionListener listener) {
        loginBtn.addActionListener(listener);
    }

    public void setRegisterListener(ActionListener listener) {
        registerBtn.addActionListener(e -> {
            regUsernameField.setText("");
            regPasswordField.setText("");
            cardLayout.show(mainPanel, "register");
        });
        jobSeekerRegBtn.addActionListener(listener);
        companyRegBtn.addActionListener(listener);
    }

    public void setApplyListener(ActionListener listener) {
        applyBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files", "pdf"));
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedResume = fileChooser.getSelectedFile();
                listener.actionPerformed(new ActionEvent(applyBtn, ActionEvent.ACTION_PERFORMED, null));
            } else {
                showMessage("Resume upload cancelled");
            }
        });
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

    public void setSelectListener(ActionListener listener) {
        selectBtn.addActionListener(listener);
    }

    public void setRejectListener(ActionListener listener) {
        rejectBtn.addActionListener(listener);
    }

    public void setViewResumeListener(ActionListener listener) {
        viewResumeBtn.addActionListener(listener);
    }

    public void setCloseJobListener(ActionListener listener) {
        closeJobBtn.addActionListener(listener);
    }

    public String getUsername() {
        return usernameField.getText();
    }

    public String getPassword() {
        return passwordField.getText();
    }

    public String getRegUsername() {
        return regUsernameField.getText();
    }

    public String getRegPassword() {
        return regPasswordField.getText();
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

    public int getSelectedApplicationId() {
        String selected = (String) applicationSelection.getSelectedItem();
        return selected != null ? Integer.parseInt(selected.split(":")[0]) : -1;
    }

    public File getSelectedResume() {
        return selectedResume;
    }

    public int getSelectedCompanyJobId() {
        String selected = (String) companyJobSelection.getSelectedItem();
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
        companyJobSelection.removeAllItems();
        for (Model.Job job : jobs) {
            companyJobList.append(job.title + " - " + job.status + "\n" + job.description + "\n\n");
            companyJobSelection.addItem(job.id + ": " + job.title);
        }
    }

    public void updateCompanyApplications(List<Model.Application> applications) {
        applicationSelection.removeAllItems();
        for (Model.Application app : applications) {
            applicationSelection.addItem(app.id + ": " + app.jobseekerUsername + " (" + app.status + ")");
        }
    }

    public void updateJobSeekerApplications(List<Model.Application> applications) {
        applicationsArea.setText("");
        for (Model.Application app : applications) {
            applicationsArea
                    .append("Job: " + app.jobTitle + " - Company: " + app.company + " - Status: " + app.status + "\n");
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