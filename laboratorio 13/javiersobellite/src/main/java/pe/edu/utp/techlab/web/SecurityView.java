package pe.edu.utp.techlab.web;

public record SecurityView(boolean authenticated, String username) {
    public static SecurityView anonymous() {
        return new SecurityView(false, "");
    }
}
