import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import javax.swing.*;
import java.util.List;
import java.util.ArrayList;
import java.awt.Desktop;

// Strategy Pattern: Role-specific behavior
interface UserStrategy {
    void performAction(Controller controller);
}

// Factory Pattern: Create role-specific controllers
class ControllerFactory {
    public static UserStrategy getController(String role, Model model, View view, int userId) {
        switch (role) {
            case "jobseeker":
                return new JobSeekerController(model, view, userId);
            case "company":
                return new CompanyController(model, view, userId);
            case "admin":
                return new AdminController(model, view, userId);
            default:
                throw new IllegalArgumentException("Invalid role");
        }
    }
}

class JobSeekerController implements UserStrategy {
    private Model model;
    private View view;
    private int userId;

    public JobSeekerController(Model model, View view, int userId) {
        this.model = model;
        this.view = view;
        this.userId = userId;
    }

    @Override
    public void performAction(Controller controller) {
        view.switchPanel("jobseeker");
        view.update(); // Update job list with open jobs on login
        view.updateJobSeekerApplications(model.getApplicationsByJobseeker(userId));
        view.setApplyListener(e -> {
            int jobId = view.getSelectedJobId();
            File resumeFile = view.getSelectedResume();
            if (jobId != -1 && resumeFile != null) {
                String result = model.applyForJob(userId, jobId, resumeFile);
                view.showMessage(result);
                if (result.equals("Success")) {
                    view.updateJobSeekerApplications(model.getApplicationsByJobseeker(userId));
                }
            } else {
                view.showMessage("Please select a job and upload a resume");
            }
        });
    }
}

class CompanyController implements UserStrategy {
    private Model model;
    private View view;
    private int userId;

    public CompanyController(Model model, View view, int userId) {
        this.model = model;
        this.view = view;
        this.userId = userId;
    }

    @Override
    public void performAction(Controller controller) {
        view.switchPanel("company");
        view.updateCompanyJobs(model.getJobsByCompany(userId));
        List<Model.Application> applications = new ArrayList<>();
        for (Model.Job job : model.getJobsByCompany(userId)) {
            applications.addAll(model.getApplicationsByJob(job.id));
        }
        view.updateCompanyApplications(applications);
        view.setAddJobListener(e -> {
            String title = view.getJobTitle();
            String description = view.getJobDescription();
            System.out.println("Adding job: Title=" + title + ", Description=" + description + ", CompanyID=" + userId);
            String result = model.addJob(userId, title, description);
            System.out.println("Add job result: " + result);
            if (result.equals("Success")) {
                view.showMessage("Job posted!");
                view.updateCompanyJobs(model.getJobsByCompany(userId));
                List<Model.Application> updatedApplications = new ArrayList<>();
                for (Model.Job job : model.getJobsByCompany(userId)) {
                    updatedApplications.addAll(model.getApplicationsByJob(job.id));
                }
                view.updateCompanyApplications(updatedApplications);
            } else {
                view.showMessage(result);
            }
        });
        view.setSelectListener(e -> {
            int applicationId = view.getSelectedApplicationId();
            if (applicationId != -1 && model.updateApplicationStatus(applicationId, "Selected")) {
                view.showMessage("Applicant selected for interview!");
                List<Model.Application> updatedApplications = new ArrayList<>();
                for (Model.Job job : model.getJobsByCompany(userId)) {
                    updatedApplications.addAll(model.getApplicationsByJob(job.id));
                }
                view.updateCompanyApplications(updatedApplications);
            } else {
                view.showMessage("Failed to select applicant!");
            }
        });
        view.setRejectListener(e -> {
            int applicationId = view.getSelectedApplicationId();
            if (applicationId != -1 && model.updateApplicationStatus(applicationId, "Rejected")) {
                view.showMessage("Applicant rejected!");
                List<Model.Application> updatedApplications = new ArrayList<>();
                for (Model.Job job : model.getJobsByCompany(userId)) {
                    updatedApplications.addAll(model.getApplicationsByJob(job.id));
                }
                view.updateCompanyApplications(updatedApplications);
            } else {
                view.showMessage("Failed to reject applicant!");
            }
        });
        view.setViewResumeListener(e -> {
            int applicationId = view.getSelectedApplicationId();
            if (applicationId != -1) {
                Model.Application selectedApp = null;
                for (Model.Application app : applications) {
                    if (app.id == applicationId) {
                        selectedApp = app;
                        break;
                    }
                }
                if (selectedApp != null && selectedApp.resumeData != null) {
                    try {
                        File tempFile = File.createTempFile("resume_" + applicationId, ".pdf");
                        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                            fos.write(selectedApp.resumeData);
                        }
                        Desktop.getDesktop().open(tempFile);
                    } catch (IOException ex) {
                        view.showMessage("Failed to open resume: " + ex.getMessage());
                    }
                } else {
                    view.showMessage("No resume available for this application!");
                }
            } else {
                view.showMessage("Please select an application!");
            }
        });
        // Listener for closing jobs
        view.setCloseJobListener(e -> {
            int jobId = view.getSelectedCompanyJobId();
            if (jobId != -1 && model.closeJob(jobId)) {
                view.showMessage("Job closed successfully!");
                view.updateCompanyJobs(model.getJobsByCompany(userId));
                List<Model.Application> updatedApplications = new ArrayList<>();
                for (Model.Job job : model.getJobsByCompany(userId)) {
                    updatedApplications.addAll(model.getApplicationsByJob(job.id));
                }
                view.updateCompanyApplications(updatedApplications);
            } else {
                view.showMessage("Failed to close job!");
            }
        });
    }
}

class AdminController implements UserStrategy {
    private Model model;
    private View view;
    private int userId;

    public AdminController(Model model, View view, int userId) {
        this.model = model;
        this.view = view;
        this.userId = userId;
    }

    @Override
    public void performAction(Controller controller) {
        view.switchPanel("admin");
        view.updateAdminView(model.getAllUsers(), model.getJobs());
        view.setDeleteUserListener(e -> {
            int selectedUserId = view.getSelectedUserId();
            if (selectedUserId != -1 && model.deleteUser(selectedUserId)) {
                view.showMessage("User deleted!");
                view.updateAdminView(model.getAllUsers(), model.getJobs());
            } else {
                view.showMessage("Failed to delete user!");
            }
        });
        view.setDeleteJobListener(e -> {
            int selectedJobId = view.getSelectedJobIdAdmin();
            if (selectedJobId != -1 && model.deleteJob(selectedJobId)) {
                view.showMessage("Job deleted!");
                view.updateAdminView(model.getAllUsers(), model.getJobs());
            } else {
                view.showMessage("Failed to delete job!");
            }
        });
    }
}

public class Controller {
    private Model model;
    private View view;
    private UserStrategy currentStrategy;

    public Controller(Model model, View view) {
        this.model = model;
        this.view = view;
        this.view.setModel(model);

        // Set up login and register listeners
        view.setLoginListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = view.getUsername();
                String password = view.getPassword();
                System.out.println("Login attempt: Username=" + username + ", Password=" + password);
                Object[] result = model.loginUser(username, password);
                String message = (String) result[0];
                Model.User user = (Model.User) result[1];
                System.out.println("Login result: " + message);
                view.showMessage(message);
                if (message.equals("Success")) {
                    currentStrategy = ControllerFactory.getController(user.role, model, view, user.id);
                    currentStrategy.performAction(Controller.this);
                }
            }
        });

        view.setRegisterListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String role = e.getSource() == view.getJobSeekerRegBtn() ? "jobseeker" : "company";
                String username = view.getRegUsername();
                String password = view.getRegPassword();
                System.out.println(
                        "Register attempt: Username=" + username + ", Password=" + password + ", Role=" + role);
                String result = model.registerUser(username, password, role);
                System.out.println("Register result: " + result);
                view.showMessage(result);
                if (result.equals("Success")) {
                    view.switchPanel("login");
                }
            }
        });

        view.setLogoutListener(e -> view.switchPanel("login"));
    }
}