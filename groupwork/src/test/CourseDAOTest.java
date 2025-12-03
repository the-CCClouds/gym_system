package test;

import dao.CourseDAO;
import entity.Course;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CourseDAOTest {
    CourseDAO courseDAO = new CourseDAO();

    @Test
    public void testAddCourse() {
        Course course = new Course();
        course.setName("测试课程");
        course.setType("yoga");
        course.setDuration(60);
        course.setMaxCapacity(20);
        course.setEmployeeId(1);

        assertTrue(courseDAO.addCourse(course));
    }

    @Test
    public void testGetCourseById() {
        Course course = courseDAO.getCourseById(1);
        assertNotNull(course);
        assertEquals("瑜伽基础班", course.getName());
        assertEquals("yoga", course.getType());
        assertEquals(60, course.getDuration());
        assertEquals(15, course.getMaxCapacity());
        assertEquals(1, course.getEmployeeId());
    }

    @Test
    public void testGetAllCourse() {
        List<Course> courses = courseDAO.getAllCourse();
        assertNotNull(courses);
        assertTrue(courses.size() >= 8); // 数据库有8条数据
    }

    @Test
    public void testUpdateCourse() {
        Course course = courseDAO.getCourseById(1);
        assertNotNull(course);

        String originalName = course.getName();
        int originalDuration = course.getDuration();

        course.setName("瑜伽基础班updated");
        course.setDuration(75);

        assertTrue(courseDAO.updateCourse(course));

        Course updated = courseDAO.getCourseById(1);
        assertEquals("瑜伽基础班updated", updated.getName());
        assertEquals(75, updated.getDuration());

        // 恢复原始数据
        course.setName(originalName);
        course.setDuration(originalDuration);
        courseDAO.updateCourse(course);
    }

    @Test
    public void testGetCourseByType() {
        List<Course> yogaCourses = courseDAO.getCourseByType("yoga");
        assertNotNull(yogaCourses);
        assertTrue(yogaCourses.size() > 0);

        for (Course course : yogaCourses) {
            assertEquals("yoga", course.getType());
        }
    }

    @Test
    public void testGetCourseByEmployeeId() {
        List<Course> courses = courseDAO.getCourseByEmployeeId("1");
        assertNotNull(courses);
        assertTrue(courses.size() > 0);

        for (Course course : courses) {
            assertEquals(1, course.getEmployeeId());
        }
    }

    @Test
    public void testSearchCourseByName() {
        List<Course> courses = courseDAO.searchCourseByName("瑜伽");
        assertNotNull(courses);
        assertTrue(courses.size() > 0);
        assertTrue(courses.get(0).getName().contains("瑜伽"));
    }

    @Test
    public void testDeleteCourse() {
        // 先添加测试数据
        Course course = new Course();
        course.setName("待删除课程");
        course.setType("other");
        course.setDuration(45);
        course.setMaxCapacity(15);
        course.setEmployeeId(2);

        assertTrue(courseDAO.addCourse(course));

        // 查找刚添加的课程
        List<Course> allCourses = courseDAO.searchCourseByName("待删除课程");
        assertNotNull(allCourses);
        assertTrue(allCourses.size() > 0);

        int courseId = allCourses.get(0).getCourseId();

        // 删除课程
        assertTrue(courseDAO.deleteCourse(courseId));

        // 验证已删除
        assertNull(courseDAO.getCourseById(courseId));
    }

    @Test
    public void testInvalidType() {
        Course course = new Course();
        course.setName("无效类型课程");
        course.setType("invalid_type");
        course.setDuration(60);
        course.setMaxCapacity(20);
        course.setEmployeeId(1);

        // 数据库约束会阻止插入
        assertFalse(courseDAO.addCourse(course));
    }

    @Test
    public void testEmptySearch() {
        List<Course> courses = courseDAO.searchCourseByName("不存在的课程名999");
        assertNotNull(courses);
        assertEquals(0, courses.size());
    }

    @Test
    public void testGetCourseByInvalidType() {
        List<Course> courses = courseDAO.getCourseByType("invalid");
        assertNotNull(courses);
        assertEquals(0, courses.size());
    }
}

