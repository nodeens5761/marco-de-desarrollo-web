package pe.edu.utp.techlab.controller;

import jakarta.validation.Valid;
import pe.edu.utp.techlab.dto.CourseDto;
import pe.edu.utp.techlab.dto.CourseInputDto;
import pe.edu.utp.techlab.exception.CategoryNotFound;
import pe.edu.utp.techlab.exception.CourseNotFound;
import pe.edu.utp.techlab.exception.DuplicateCourseTitle;
import pe.edu.utp.techlab.service.CourseService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cursos")
public class CourseViewController {
    private final CourseService courseService;

    public CourseViewController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("cursos", courseService.getCourses());
        return "cursos/lista";
    }

    @GetMapping("/nuevo")
    public String newForm(Model model) {
        model.addAttribute("curso", new CourseInputDto("", null, null));
        model.addAttribute("cursoId", null);
        addCategories(model);
        return "cursos/formulario";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("curso") CourseInputDto input,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            addCategories(model);
            model.addAttribute("cursoId", null);
            return "cursos/formulario";
        }
        try {
            CourseDto created = courseService.createCourse(input);
            redirect.addFlashAttribute("mensaje", "Curso creado correctamente.");
            return "redirect:/cursos/" + created.id();
        } catch (DuplicateCourseTitle ex) {
            result.rejectValue("titulo", "duplicate", ex.getMessage());
        } catch (CategoryNotFound ex) {
            result.rejectValue("categoriaId", "invalid", ex.getMessage());
        }
        addCategories(model);
        model.addAttribute("cursoId", null);
        return "cursos/formulario";
    }

    @GetMapping("/resumen")
    public String summary(Model model) {
        model.addAttribute("resumen", courseService.getSummary());
        return "cursos/resumen";
    }

    @GetMapping("/{id}/editar")
    public String editForm(@PathVariable Long id, Model model) {
        CourseDto course = courseService.getCourse(id);
        model.addAttribute("curso", new CourseInputDto(course.titulo(), course.horas(), course.categoriaId()));
        model.addAttribute("cursoId", id);
        addCategories(model);
        return "cursos/formulario";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("curso") CourseInputDto input,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            addCategories(model);
            model.addAttribute("cursoId", id);
            return "cursos/formulario";
        }
        try {
            courseService.updateCourse(id, input);
            redirect.addFlashAttribute("mensaje", "Curso actualizado correctamente.");
            return "redirect:/cursos/" + id;
        } catch (DuplicateCourseTitle ex) {
            result.rejectValue("titulo", "duplicate", ex.getMessage());
        } catch (CategoryNotFound ex) {
            result.rejectValue("categoriaId", "invalid", ex.getMessage());
        }
        addCategories(model);
        model.addAttribute("cursoId", id);
        return "cursos/formulario";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("curso", courseService.getCourse(id));
        return "cursos/detalle";
    }

    @PostMapping("/{id}/eliminar")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        courseService.deleteCourse(id);
        redirect.addFlashAttribute("mensaje", "Curso eliminado correctamente.");
        return "redirect:/cursos";
    }

    @ExceptionHandler(CourseNotFound.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView courseNotFound() {
        return new ModelAndView("error/404");
    }

    private void addCategories(Model model) {
        model.addAttribute("categorias", courseService.getCategories());
    }
}
