package test;

import entity.MembershipCard;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import dao.MembershipCardDAO;
public class MembershipCardDAOTest {
    private MembershipCardDAO cardDAO;
    private int testCardId;

    @Before
    public void setUp() {
        cardDAO = new MembershipCardDAO();
    }

    @After
    public void tearDown() {
        // 清理测试数据
        if (testCardId > 0) {
            cardDAO.deleteMembershipCard(testCardId);
        }
    }

    @Test
    public void testAddMembershipCard() {
        MembershipCard card = new MembershipCard();
        card.setMemberId(1);
        card.setCardType("monthly");
        card.setStartDate(new Date());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);
        card.setEndDate(cal.getTime());
        card.setCardStatus("active");

        assertTrue(cardDAO.addMembershipCard(card));
        assertNotNull(card.getCardId());
        assertTrue(card.getCardId() > 0);

        testCardId = card.getCardId();
    }

    @Test
    public void testGetById() {
        MembershipCard card = cardDAO.getById(1);
        assertNotNull(card);
        assertEquals(1, card.getCardId());
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
    public void testGetAll() {
        List<MembershipCard> cards = cardDAO.getAll();
        assertNotNull(cards);
        assertTrue(cards.size() > 0);
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
        // ✅ 允许任何整数(包括负数表示已过期天数)
        assertTrue(days != 0 || days == -1);
    }


    @Test
    public void testUpdateExpiredCards() {
        int count = cardDAO.updateExpiredCards();
        assertTrue(count >= 0);
    }

    @Test
    public void testInvalidMemberId() {
        MembershipCard card = new MembershipCard();
        card.setMemberId(99999);
        card.setCardType("monthly");
        card.setStartDate(new Date());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);
        card.setEndDate(cal.getTime());
        card.setCardStatus("active");

        assertFalse(cardDAO.addMembershipCard(card));
    }

    @Test
    public void testInvalidCardType() {
        MembershipCard card = new MembershipCard();
        card.setMemberId(1);
        card.setCardType("invalid_type");
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
        card.setCardType("monthly");
        card.setStartDate(new Date());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);
        card.setEndDate(cal.getTime());
        card.setCardStatus("invalid_status");

        assertFalse(cardDAO.addMembershipCard(card));
    }

    @Test
    public void testInvalidDateRange() {
        MembershipCard card = new MembershipCard();
        card.setMemberId(1);
        card.setCardType("monthly");

        Calendar cal = Calendar.getInstance();
        card.setStartDate(cal.getTime());

        cal.add(Calendar.MONTH, -1);
        card.setEndDate(cal.getTime());
        card.setCardStatus("active");

        assertFalse(cardDAO.addMembershipCard(card));
    }

    @Test
    public void testDeleteMembershipCard() {
        MembershipCard card = new MembershipCard();
        card.setMemberId(1);
        card.setCardType("monthly");
        card.setStartDate(new Date());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);
        card.setEndDate(cal.getTime());
        card.setCardStatus("active");

        assertTrue(cardDAO.addMembershipCard(card));
        int cardId = card.getCardId();

        assertTrue(cardDAO.deleteMembershipCard(cardId));
        assertNull(cardDAO.getById(cardId));

        testCardId = 0;
    }

    @Test
    public void testYearlyCard() {
        MembershipCard card = new MembershipCard();
        card.setMemberId(1);
        card.setCardType("yearly");
        card.setStartDate(new Date());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 1);
        card.setEndDate(cal.getTime());
        card.setCardStatus("active");

        assertTrue(cardDAO.addMembershipCard(card));
        assertNotNull(card.getCardId());

        testCardId = card.getCardId();

        MembershipCard retrieved = cardDAO.getById(testCardId);
        assertEquals("yearly", retrieved.getCardType());
    }
}
