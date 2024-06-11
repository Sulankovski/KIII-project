package mk.ukim.finki.wp.kol2022.g1.service.impl;

import mk.ukim.finki.wp.kol2022.g1.model.Employee;
import mk.ukim.finki.wp.kol2022.g1.model.EmployeeType;
import mk.ukim.finki.wp.kol2022.g1.model.Skill;
import mk.ukim.finki.wp.kol2022.g1.model.exceptions.InvalidEmployeeIdException;
import mk.ukim.finki.wp.kol2022.g1.repository.EmployeeRepository;
import mk.ukim.finki.wp.kol2022.g1.repository.SkillRepository;
import mk.ukim.finki.wp.kol2022.g1.service.EmployeeService;
import mk.ukim.finki.wp.kol2022.g1.service.SkillService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final SkillRepository skillRepository;
    private final SkillService skillService;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, SkillRepository skillRepository, SkillService skillService) {
        this.employeeRepository = employeeRepository;
        this.skillRepository = skillRepository;
        this.skillService = skillService;
    }

    @Override
    public List<Employee> listAll() {
        return employeeRepository.findAll();
    }

    @Override
    public Employee findById(Long id) {
        return employeeRepository.findById(id).orElseThrow(InvalidEmployeeIdException::new);
    }

    @Override
    public Employee create(String name, String email, String password, EmployeeType type, List<Long> skillId, LocalDate employmentDate) {
        List<Skill> skills = skillRepository.findAllById(skillId);
        Employee employee = new Employee(
                name,
                email,
                password,
                type,
                skills,
                employmentDate
        );
        return employeeRepository.save(employee);
    }

    @Override
    public Employee update(Long id, String name, String email, String password, EmployeeType type, List<Long> skillId, LocalDate employmentDate) {
        List<Skill> skills = skillRepository.findAllById(skillId);
        Employee employee = findById(id);
        employee.setName(name);
        employee.setEmail(email);
        employee.setPassword(password);
        employee.setType(type);
        employee.setSkills(skills);
        employee.setEmploymentDate(employmentDate);
        return employeeRepository.save(employee);
    }

    @Override
    public Employee delete(Long id) {
        Employee employee = findById(id);
        employeeRepository.deleteById(id);
        return employee;
    }

    @Override
    public List<Employee> filter(Long skillId, Integer yearsOfService) {
        if (skillId != null && yearsOfService != null)
        {
            Skill skill = skillService.findById(skillId);
            LocalDate employmentBefore = LocalDate.now().minusYears(yearsOfService);
            return employeeRepository.findBySkillsContainingAndEmploymentDateBefore(skill, employmentBefore);
        }
        if (skillId != null){
            Skill skill = skillService.findById(skillId);
            return employeeRepository.findBySkillsContaining(skill);
        }
        if (yearsOfService != null){
            LocalDate employmentBefore = LocalDate.now().minusYears(yearsOfService);
            return employeeRepository.findByEmploymentDateBefore(employmentBefore);
        }
        return this.listAll();
    }
}
