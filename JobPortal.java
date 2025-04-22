public class JobPortal {
    public static void main(String[] args) {
        // Initialize MVC components
        Model model = Model.getInstance();
        View view = new View();
        Controller controller = new Controller(model, view);

        // Register view as observer of model
        model.addObserver(view);

        // Start the application
        view.setVisible(true);
    }
}