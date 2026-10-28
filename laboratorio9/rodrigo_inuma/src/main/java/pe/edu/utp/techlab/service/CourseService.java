package pe.edu.utp.techlab.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.techlab.dto.CourseDto;
import pe.edu.utp.techlab.entity.Course;
import pe.edu.utp.techlab.mapper.CourseMapper;
import pe.edu.utp.techlab.repository.CourseRepository;

@Service
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository repository;
    private final CourseMapper mapper;

    public CourseService(
            CourseRepository repository,
            CourseMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<CourseDto> buscar(String texto) {
        String criterio = Optional.ofNullable(texto)
            .orElse("")
            .strip();

        List<Course> entidades = criterio.isBlank()
            ? repository.findAllByOrderByIdAsc()
            : repository
                .findByTituloContainingIgnoreCaseOrderByIdAsc(criterio);

        return entidades.stream()
            .map(mapper::toDto)
            .toList();
    }

    public Optional<CourseDto> buscarPorId(long id) {
        return repository.findById(id)
            .map(mapper::toDto);
    }

    public long cantidad() {
        return repository.count();
    }

    public int totalHoras() {
        Long total = repository.sumarHoras();
        return Math.toIntExact(total == null ? 0L : total);
    }
}
