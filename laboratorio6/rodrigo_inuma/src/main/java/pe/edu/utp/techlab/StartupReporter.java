package pe.edu.utp.techlab;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class StartupReporter {
    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        System.out.println("TechLab Web listo");
    }
}
