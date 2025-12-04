package test;

import dao.MembershipTypeDAO;
import entity.MembershipType;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class MembershipTypeDAOTest {
    private MembershipTypeDAO typeDAO;

    @Before
    public void setUp() {
        typeDAO = new MembershipTypeDAO();
    }

    @Test
    public void testGetAllTypes() {
        List<MembershipType> types = typeDAO.getAllTypes();
        assertNotNull(types);
        assertEquals(2, types.size());  // 数据库有2种类型
    }

    @Test
    public void testGetTypeById() {
        // 测试月卡类型
        MembershipType monthly = typeDAO.getTypeById(1);
        assertNotNull(monthly);
        assertEquals(1, monthly.getTypeId());
        assertEquals("Monthly", monthly.getTypeName());
        assertEquals(30, monthly.getDurationDays());
        assertEquals(200.0, monthly.getPrice(), 0.01);

        // 测试年卡类型
        MembershipType yearly = typeDAO.getTypeById(2);
        assertNotNull(yearly);
        assertEquals(2, yearly.getTypeId());
        assertEquals("Yearly", yearly.getTypeName());
        assertEquals(365, yearly.getDurationDays());
        assertEquals(1200.0, yearly.getPrice(), 0.01);
    }

    @Test
    public void testGetTypeByIdNotFound() {
        MembershipType type = typeDAO.getTypeById(999);
        assertNull(type);
    }

    @Test
    public void testGetTypeByName() {
        MembershipType monthly = typeDAO.getTypeByName("Monthly");
        assertNotNull(monthly);
        assertEquals(1, monthly.getTypeId());

        MembershipType yearly = typeDAO.getTypeByName("Yearly");
        assertNotNull(yearly);
        assertEquals(2, yearly.getTypeId());
    }

    @Test
    public void testGetTypeByNameNotFound() {
        MembershipType type = typeDAO.getTypeByName("NotExist");
        assertNull(type);
    }

    @Test
    public void testGetMonthlyType() {
        MembershipType monthly = typeDAO.getMonthlyType();
        assertNotNull(monthly);
        assertEquals("Monthly", monthly.getTypeName());
        assertEquals(30, monthly.getDurationDays());
        assertTrue(monthly.isMonthly());
        assertFalse(monthly.isYearly());
    }

    @Test
    public void testGetYearlyType() {
        MembershipType yearly = typeDAO.getYearlyType();
        assertNotNull(yearly);
        assertEquals("Yearly", yearly.getTypeName());
        assertEquals(365, yearly.getDurationDays());
        assertTrue(yearly.isYearly());
        assertFalse(yearly.isMonthly());
    }

    @Test
    public void testGetPriceByTypeId() {
        double monthlyPrice = typeDAO.getPriceByTypeId(1);
        assertEquals(200.0, monthlyPrice, 0.01);

        double yearlyPrice = typeDAO.getPriceByTypeId(2);
        assertEquals(1200.0, yearlyPrice, 0.01);

        // 无效ID返回0
        double invalidPrice = typeDAO.getPriceByTypeId(999);
        assertEquals(0.0, invalidPrice, 0.01);
    }

    @Test
    public void testGetDurationDaysByTypeId() {
        int monthlyDays = typeDAO.getDurationDaysByTypeId(1);
        assertEquals(30, monthlyDays);

        int yearlyDays = typeDAO.getDurationDaysByTypeId(2);
        assertEquals(365, yearlyDays);

        // 无效ID返回0
        int invalidDays = typeDAO.getDurationDaysByTypeId(999);
        assertEquals(0, invalidDays);
    }

    @Test
    public void testAddAndDeleteType() {
        // 添加新类型
        MembershipType newType = new MembershipType();
        newType.setTypeId(99);
        newType.setTypeName("TestType");
        newType.setDurationDays(90);
        newType.setPrice(500.0);
        newType.setDescription("测试类型，有效期90天");

        assertTrue(typeDAO.addType(newType));

        // 验证添加成功
        MembershipType added = typeDAO.getTypeById(99);
        assertNotNull(added);
        assertEquals("TestType", added.getTypeName());
        assertEquals(90, added.getDurationDays());
        assertEquals(500.0, added.getPrice(), 0.01);

        // 删除测试类型
        assertTrue(typeDAO.deleteType(99));
        assertNull(typeDAO.getTypeById(99));
    }

    @Test
    public void testUpdateType() {
        // 先添加一个测试类型
        MembershipType testType = new MembershipType();
        testType.setTypeId(98);
        testType.setTypeName("UpdateTest");
        testType.setDurationDays(60);
        testType.setPrice(300.0);
        testType.setDescription("更新测试");

        assertTrue(typeDAO.addType(testType));

        // 更新类型
        testType.setTypeName("UpdatedType");
        testType.setDurationDays(75);
        testType.setPrice(350.0);
        testType.setDescription("已更新的类型");

        assertTrue(typeDAO.updateType(testType));

        // 验证更新成功
        MembershipType updated = typeDAO.getTypeById(98);
        assertNotNull(updated);
        assertEquals("UpdatedType", updated.getTypeName());
        assertEquals(75, updated.getDurationDays());
        assertEquals(350.0, updated.getPrice(), 0.01);

        // 清理测试数据
        assertTrue(typeDAO.deleteType(98));
    }

    @Test
    public void testTypeDescription() {
        MembershipType monthly = typeDAO.getTypeById(1);
        assertNotNull(monthly.getDescription());
        assertTrue(monthly.getDescription().contains("月卡"));

        MembershipType yearly = typeDAO.getTypeById(2);
        assertNotNull(yearly.getDescription());
        assertTrue(yearly.getDescription().contains("年卡"));
    }

    @Test
    public void testIsMonthlyAndIsYearly() {
        MembershipType monthly = typeDAO.getTypeById(1);
        assertTrue(monthly.isMonthly());
        assertFalse(monthly.isYearly());

        MembershipType yearly = typeDAO.getTypeById(2);
        assertFalse(yearly.isMonthly());
        assertTrue(yearly.isYearly());
    }
}

