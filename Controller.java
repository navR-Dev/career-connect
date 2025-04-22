import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
        view.setApplyListener(e -> {
            int jobId = view.getSelectedJobId();
            if (jobId != -1 && model.applyForJob(userId, jobId)) {
                view.showMessage("Application submitted!");
            } else {
                view.showMessage("Application failed!");
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
        view.setAddJobListener(e -> {
            String title = view.getJobTitle();
            String description = view.getJobDescription();
            if (!title.isEmpty() && !description.isEmpty() && model.addJob(userId, title, description)) {
                view.showMessage("Job posted!");
                view.updateCompanyJobs(model.getJobsByCompany(userId));
            } else {
                view.showMessage("Failed to post job!");
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
                Model.User user = model.loginUser(username, password);
                if (user != null) {
                    currentStrategy = ControllerFactory.getController(user.role, model, view, user.id);
                    currentStrategy.performAction(Controller.this);
                } else {
                    view.showMessage("Invalid credentials!");
                }
            }
        });

        view.setRegisterListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String role = e.getSource() == view.getJobSeekerRegBtn() ? "jobseeker" : "company";
                String username = view.getUsername();
                String password = view.getPassword();
                String result = model.registerUser(username, password, role);
                if (result.equals("Success")) {
                    view.showMessage("Registration successful!");
                    view.switchPanel("login");
                } else {
                    view.showMessage(result);
                }
            }
        });

        view.setLogoutListener(e -> view.switchPanel("login"));
    }
}