package com.mvc.tdd.gradebook;

import static org.hamcrest.CoreMatchers.*; // is()
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;  // not work
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*; // work
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.ModelAndView;

import com.mvc.tdd.dao.MathGradesDao;
import com.mvc.tdd.dao.ScienceGradesDao;
import com.mvc.tdd.dao.StudentDao;
import com.mvc.tdd.entity.CollegeStudent;
import com.mvc.tdd.entity.Gradebook;
import com.mvc.tdd.entity.GradebookCollegeStudent;
import com.mvc.tdd.service.StudentGradeService;

@TestPropertySource("/application-test.properties")
@AutoConfigureMockMvc
@SpringBootTest
public class StudentGradeControllerTest {

    private static MockHttpServletRequest httpRequest;
    
    @Autowired
    private JdbcTemplate jdbc;

    @PersistenceContext
	private EntityManager entityMng;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentGradeService studentGradeService;

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private CollegeStudent student;
    
    @Autowired
    private Gradebook gradebook;

    @Autowired
    private MathGradesDao mathGradesDao;

    @Autowired
    private ScienceGradesDao scienceGradesDao;

    @Mock
    private StudentGradeService studentGradeServiceMock;

    @Value("${sql.script.create.student}") 
    private String sqlInsertStudentData;

    @Value("${sql.script.create.math.grade}") 
    private String sqlInsertMathGradeData;

    @Value("${sql.script.create.science.grade}") 
    private String sqlInsertScienceGradeData;

    @Value("${sql.script.delete.student}") 
    private String sqlDeleteStudentData;

    @Value("${sql.script.delete.math.grade}") 
    private String sqlDeleteMathGradeData;

    @Value("${sql.script.delete.science.grade}") 
    private String sqlDeleteScienceGradeData;

    @BeforeAll 
    public static void setup() {
        httpRequest = new MockHttpServletRequest();

        httpRequest.setParameter("firstName", "kane");
        httpRequest.setParameter("lastName", "nguyen");
        httpRequest.setParameter("email", "cudayanh1@test.com");
    }

    @BeforeEach
    public void beforeEach() {
        jdbc.execute(sqlInsertStudentData);
        jdbc.execute(sqlInsertMathGradeData);
        jdbc.execute(sqlInsertScienceGradeData);
    }

    @Test
    void getStudentsHttpRequest() throws Exception {
        CollegeStudent studentA = new GradebookCollegeStudent("tom", "nguyen", "mail1@test.com");
        CollegeStudent studentB = new GradebookCollegeStudent("cat", "nguyen", "mail2@test.com");

        List<CollegeStudent> students = new ArrayList<>(Arrays.asList(studentA, studentB));

        when(studentGradeServiceMock.getStudentGrades()).thenReturn(students);

        assertIterableEquals(students, studentGradeServiceMock.getStudentGrades());

        MvcResult mvcResult = mockMvc.perform(get("/"))
                                .andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        ModelAndViewAssert.assertViewName(mav, "index"); // "index" is returned page from corresponding controller
    }

    @Test
    void createStudentHttpRequest() throws Exception {
        // test updating for UI (add/get students)
        // CollegeStudent studentA = new CollegeStudent("tom", "nguyen", "tom@test.com");
        // List<CollegeStudent> students = new ArrayList<>(Arrays.asList(studentA));
        // when(studentServiceMock.getGradebook()).thenReturn(students);
        // assertIterableEquals(students, studentServiceMock.getGradebook());

        MvcResult mvcResult = mockMvc.perform(post("/")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("firstName", httpRequest.getParameterValues("firstName"))
                                .param("lastName", httpRequest.getParameterValues("lastName"))
                                .param("email", httpRequest.getParameterValues("email")))
                                .andExpect(status().isOk())
                                .andReturn();

        // check view
        ModelAndView mav = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(mav, "index");

        CollegeStudent studentToVerify = studentDao.findByEmail("cudayanh1@test.com");
        
        assertNotNull(studentToVerify, "Student should not be null");
    }

    @Test
    void deleteStudentHttpRequest() throws Exception {
        int studentId = 2;
        assertTrue(studentDao.findById(studentId).isPresent(), "Find student by Id (should return true)");

        MvcResult mvcResult = mockMvc.perform(get("/student/delete/{id}", studentId))
                                    .andExpect(status().isOk())
                                    .andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        ModelAndViewAssert.assertViewName(mav, "index");

        assertFalse(studentDao.findById(studentId).isPresent());
    }

    @Test
    void deleteStudentHttpRequestWithErrorPage() throws Exception {
        int nonExistingStudentId = 111;
        MvcResult mvcResult = mockMvc.perform(get("/student/delete/{id}", nonExistingStudentId))
                                    .andExpect(status().isOk())
                                    .andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        ModelAndViewAssert.assertViewName(mav, "error");
    }

    @Test 
    void getStudentDetailsHttpRequest() throws Exception {
        int studentId = 2;
        assertTrue(studentDao.findById(studentId).isPresent());

        MvcResult mvcResult = mockMvc.perform(get("/studentDetails/{id}", studentId))
                                    .andExpect(status().isOk())
                                    .andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        ModelAndViewAssert.assertViewName(mav, "studentDetails");
    }

    @Test 
    void getStudentDetailsHttpRequestWithErrorPage() throws Exception {
        int studentId = 222;
        assertFalse(studentDao.findById(studentId).isPresent());

        MvcResult mvcResult = mockMvc.perform(get("/studentDetails/{id}", studentId))
                                    .andExpect(status().isOk())
                                    .andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        ModelAndViewAssert.assertViewName(mav, "error");
    }

    // TODO
    // @Test
    // void createMathGradesHttpRequest() throws Exception {
    //     int studentId = 2;

    //     assertTrue(studentDao.findById(studentId).isPresent());

    //     Optional<GradebookCollegeStudent> student = studentGradeService.getStudentDetails(studentId);

    //     assertTrue(student.isPresent());

    //     assertEquals(1, student.get().getStudentGrades().getMathGradeResults().size());

    //     MvcResult mvcResult = mockMvc.perform(post("/grades")
    //         .contentType(MediaType.APPLICATION_JSON)
    //         .param("grade", "80.05")
    //         .param("subject", "math")
    //         .param("studentId", "2"))
    //         .andExpect(status().isCreated())
    //         .andReturn();

    //     ModelAndView mav = mvcResult.getModelAndView();

    //     ModelAndViewAssert.assertViewName(mav, "studentDetails");

    //     student = studentGradeService.getStudentDetails(studentId);

    //     assertEquals(2, student.get().getStudentGrades().getMathGradeResults().size());
    // }

    // TODO
    // @Test
    // void createScienceGradesHttpRequest() throws Exception {
    //     int studentId = 2;

    //     assertTrue(studentDao.findById(studentId).isPresent());

    //     Optional<GradebookCollegeStudent> student = studentGradeService.getStudentDetails(studentId);

    //     assertTrue(student.isPresent());

    //     assertEquals(1, student.get().getStudentGrades().getScienceGradeResults().size());

    //     MvcResult mvcResult = mockMvc.perform(post("/grades")
    //         .contentType(MediaType.APPLICATION_JSON)
    //         .param("grade", "90.05")
    //         .param("subject", "science")
    //         .param("studentId", "2"))
    //         .andExpect(status().isOk())
    //         .andReturn();

    //     ModelAndView mav = mvcResult.getModelAndView();

    //     ModelAndViewAssert.assertViewName(mav, "studentDetails");

    //     student = studentGradeService.getStudentDetails(studentId);

    //     assertEquals(2, student.get().getStudentGrades().getScienceGradeResults().size());
    // }

    // TODO
    // @Test
    // void createInvalidGradeWithNonExistedStudentIdHttpRequest() throws Exception {
    //     MvcResult mvcResult = mockMvc.perform(post("/grades")
    //         .contentType(MediaType.APPLICATION_JSON)
    //         .param("grade", "90.05")
    //         .param("subject", "science")
    //         .param("studentId", "222"))
    //         .andExpect(status().isOk()).andReturn();       

    //     ModelAndView mav = mvcResult.getModelAndView();

    //     ModelAndViewAssert.assertViewName(mav, "error");
    // }
  
  // TODO
  //  @Test
  //   void createInvalidGradeWithNonAvailableSubjectHttpRequest() throws Exception {
  //       MvcResult mvcResult = mockMvc.perform(post("/grades")
  //           .contentType(MediaType.APPLICATION_JSON)
  //           .param("grade", "90.05")
  //           .param("subject", "basic coding")
  //           .param("studentId", "2"))
  //           .andExpect(status().isOk()).andReturn();       

  //       ModelAndView mav = mvcResult.getModelAndView();

  //       ModelAndViewAssert.assertViewName(mav, "error");
  //   }
    
  // TODO
    // @Test
    // void deleteMathGradeHttpRequest() throws Exception {
    //     int id = 2;
    //     Optional<MathGrade> mathGrade = mathGradesDao.findById(2);

    //     assertTrue(mathGrade.isPresent());

    //     MvcResult mvcResult = mockMvc.perform(get("/grades/delete/{id}", id))
    //         .andExpect(status().isOk()).andReturn();       

    //     ModelAndView mav = mvcResult.getModelAndView();

    //     ModelAndViewAssert.assertViewName(mav, "index");

    //     mathGrade = mathGradesDao.findById(id);

    //     assertFalse(mathGrade.isPresent());
    // }

    @Test
    void deleteScienceGradeHttpRequest() throws Exception {

    }

    @Test
    void deleteNonExistedGradeHttpRequest() throws Exception {

    }

    @AfterEach
    public void afterEach() {
        jdbc.execute(sqlDeleteStudentData);
        jdbc.execute(sqlDeleteMathGradeData);
        jdbc.execute(sqlDeleteScienceGradeData);
    }
}
