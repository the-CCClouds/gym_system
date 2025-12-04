package test;

import dao.MembershipCardDAO;
import dao.MembershipTypeDAO;
import entity.MembershipCard;
import entity.MembershipType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class MembershipCardDAOTest {
    private MembershipCardDAO cardDAO;
    private MembershipTypeDAO typeDAO;
    private int testCardId;

    @Before
    public void setUp() {
        cardDAO = new MembershipCardDAO();
        typeDAO = new MembershipTypeDAO();
    }

    @After
    public void tearDown() {
        // 清理测试数据
        if (testCardId > 0) {
            cardDAO.deleteMembershipCard(testCardId);
            testCardId = 0;
        }
    }

    @Test
    public void testAddMonthlyCard() {
        MembershipCard card = new MembershipCard();
        card.setMemberId(1);
        card.setTypeId(1);  // 月卡
        card.setStartDate(new Date());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);
        card.setEndDate(cal.getTime());
        card.setCardStatus("active");

        assertTrue(cardDAO.addMembershipCard(card));
        assertNotNull(card.getCardId());
        assertTrue(card.getCardId() > 0);

        testCardId = card.getCardId();

        // 验证类型
        MembershipCard retrieved = cardDAO.getById(testCardId);
        assertEquals(1, retrieved.getTypeId());
        assertEquals("Monthly", retrieved.getCardType());
        assertTrue(retrieved.isMonthly());
    }

    @Test
    public void testAddYearlyCard() {
        MembershipCard card = new MembershipCard();
        card.setMemberId(1);
        card.setTypeId(2);  // 年卡
        card.setStartDate(new Date());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 1);
        card.setEndDate(cal.getTime());
        card.setCardStatus("active");

        assertTrue(cardDAO.addMembershipCard(card));
        testCardId = card.getCardId();

        MembershipCard retrieved = cardDAO.getById(testCardId);
        assertEquals(2, retrieved.getTypeId());
        assertEquals("Yearly", retrieved.getCardType());
        assertTrue(retrieved.isYearly());
    }

    @Test
    public void testAddCardWithSetCardType() {
        // 测试兼容旧代码的 setCardType 方法
        MembershipCard card = new MembershipCard();
        card.setMemberId(1);
        card.setCardType("monthly");  // 使用字符串方式设置
        card.setStartDate(new Date());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);
        card.setEndDate(cal.getTime());
        card.setCardStatus("active");

        assertTrue(cardDAO.addMembershipCard(card));
        testCardId = card.getCardId();

        // 验证 typeId 被正确设置
        assertEquals(1, card.getTypeId());
    }

    @Test
    public void testGetById() {
        MembershipCard card = cardDAO.getById(1);
        assertNotNull(card);
        assertEquals(1, card.getCardId());

        // 验证关联的类型对象
        MembershipType type = card.getMembershipType();
        assertNotNull(type);
        assertTrue(type.getTypeId() > 0);
    }

    @Test
    public void testGetByIdNotFound() {
        MembershipCard card = cardDAO.getById(99999);
        assertNull(card);
    }

    @Test
    public void testGetByMemberId() {
        List<MembershipCard> cards = cardDAO.getByMemberId(1);
        assertNotNull(cards);
        assertTrue(cards.size() > 0);
        assertEquals(1, cards.get(0).getMemberId());
    }

    @Test
    public void testGetByStatus() {
        List<MembershipCard> cards = cardDAO.getByStatus("active");
        assertNotNull(cards);
        for (MembershipCard card : cards) {
            assertEquals("active", card.getCardStatus());
        }
    }

    @Test
    public void testGetByTypeId() {
        // 测试获取所有月卡
        List<MembershipCard> monthlyCards = cardDAO.getByTypeId(1);
        assertNotNull(monthlyCards);
        for (MembershipCard card : monthlyCards) {
            assertEquals(1, card.getTypeId());
            assertTrue(card.isMonthly());
        }

        // 测试获取所有年卡
        List<MembershipCard> yearlyCards = cardDAO.getByTypeId(2);
        assertNotNull(yearlyCards);
        for (MembershipCard card : yearlyCards) {
            assertEquals(2, card.getTypeId());
            assertTrue(card.isYearly());
        }
    }

    @Test
    public void testGetMonthlyCards() {
        List<MembershipCard> cards = cardDAO.getMonthlyCards();
        assertNotNull(cards);
        for (MembershipCard card : cards) {
            assertTrue(card.isMonthly());
        }
    }

    @Test
    public void testGetYearlyCards() {
        List<MembershipCard> cards = cardDAO.getYearlyCards();
        assertNotNull(cards);
        for (MembershipCard card : cards) {
            assertTrue(card.isYearly());
        }
    }

    @Test
    public void testGetAll() {
        List<MembershipCard> cards = cardDAO.getAll();
        assertNotNull(cards);
        assertTrue(cards.size() >= 10);  // 数据库有10条数据

        // 验证每张卡都有关联的类型对象
        for (MembershipCard card : cards) {
            assertNotNull(card.getMembershipType());
        }
    }

    @Test
    public void testUpdateMembershipCard() {
        MembershipCard card = cardDAO.getById(1);
        assertNotNull(card);

        String oldStatus = card.getCardStatus();
        card.setCardStatus("inactive");

        assertTrue(cardDAO.updateMembershipCard(card));

        MembershipCard updated = cardDAO.getById(1);
        assertEquals("inactive", updated.getCardStatus());

        // 恢复原状态
        card.setCardStatus(oldStatus);
        cardDAO.updateMembershipCard(card);
    }

    @Test
    public void testGetRemainingDays() {
        int days = cardDAO.getRemainingDays(1);
        // 允许任何整数(包括负数表示已过期天数)
        assertTrue(days >= -1000 && days <= 1000);
    }

    @Test
    public void testUpdateExpiredCards() {
        int count = cardDAO.updateExpiredCards();
        assertTrue(count >= 0);
    }

    @Test
    public void testInvalidMemberId() {
        MembershipCard card = new MembershipCard();
        card.setMemberId(99999);  // 不存在的会员ID
        card.setTypeId(1);
        card.setStartDate(new Date());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);
        card.setEndDate(cal.getTime());
        card.setCardStatus("active");

        assertFalse(cardDAO.addMembershipCard(card));
    }

    @Test
    public void testInvalidTypeId() {
        MembershipCard card = new MembershipCard();
        card.setMemberId(1);
        card.setTypeId(99);  // 无效的类型ID
        card.setStartDate(new Date());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);
        card.setEndDate(cal.getTime());
        card.setCardStatus("active");

        assertFalse(cardDAO.addMembershipCard(card));
    }

    @Test
    public void testInvalidCardStatus() {
        MembershipCard card = new MembershipCard();
        card.setMemberId(1);
        card.setTypeId(1);
        card.setStartDate(new Date());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);
        card.setEndDate(cal.getTime());
        card.setCardStatus("invalid_status");  // 无效状态

        assertFalse(cardDAO.addMembershipCard(card));
    }

    @Test
    public void testInvalidDateRange() {
        MembershipCard card = new MembershipCard();
        card.setMemberId(1);
        card.setTypeId(1);

        Calendar cal = Calendar.getInstance();
        card.setStartDate(cal.getTime());

        cal.add(Calendar.MONTH, -1);  // 结束日期早于开始日期
        card.setEndDate(cal.getTime());
        card.setCardStatus("active");

        assertFalse(cardDAO.addMembershipCard(card));
    }

    @Test
    public void testDeleteMembershipCard() {
        // 先添加测试数据
        MembershipCard card = new MembershipCard();
        card.setMemberId(1);
        card.setTypeId(1);
        card.setStartDate(new Date());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);
        card.setEndDate(cal.getTime());
        card.setCardStatus("active");

        assertTrue(cardDAO.addMembershipCard(card));
        int cardId = card.getCardId();

        // 删除
        assertTrue(cardDAO.deleteMembershipCard(cardId));
        assertNull(cardDAO.getById(cardId));

        testCardId = 0;  // 已删除，不需要 tearDown 再删
    }

    @Test
    public void testMembershipTypeRelation() {
        MembershipCard card = cardDAO.getById(1);
        assertNotNull(card);

        MembershipType type = card.getMembershipType();
        assertNotNull(type);

        // 验证类型信息
        assertTrue(type.getDurationDays() > 0);
        assertTrue(type.getPrice() > 0);
        assertNotNull(type.getTypeName());

        // 验证卡的便捷方法
        assertEquals(type.getPrice(), card.getPrice(), 0.01);
        assertEquals(type.getDurationDays(), card.getDurationDays());
    }

    @Test
    public void testCardTypeConsistency() {
        // 验证数据库中所有卡的类型一致性
        List<MembershipCard> allCards = cardDAO.getAll();
        for (MembershipCard card : allCards) {
            if (card.getTypeId() == 1) {
                assertEquals("Monthly", card.getCardType());
                assertTrue(card.isMonthly());
                assertFalse(card.isYearly());
            } else if (card.getTypeId() == 2) {
                assertEquals("Yearly", card.getCardType());
                assertTrue(card.isYearly());
                assertFalse(card.isMonthly());
            }
        }
    }
}
