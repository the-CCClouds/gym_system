package test;

import dao.MemberDAO;
import entity.Member;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MemberDAOTest {
    MemberDAO memberDAO = new MemberDAO();

    @Test
    public void testAddMember() {
        Member member = new Member();
        member.setName("测试用户");
        member.setPhone("13912345678");
        member.setEmail("test@email.com");
        member.setGender("male");
        member.setBirthDate(new Date(System.currentTimeMillis() - 25L * 365 * 24 * 60 * 60 * 1000)); // 25岁
        member.setRegisterDate(new Date());
        member.setStatus("active");

        assertTrue(memberDAO.addMember(member));
        assertNotNull(member.getId());
    }

    @Test
    public void testGetById() {
        Member member = memberDAO.getMemberById(1);
        assertNotNull(member);
        assertEquals("张三", member.getName());
        assertEquals("13900001111", member.getPhone());
    }

    @Test
    public void testGetAll() {
        List<Member> members = memberDAO.getAllMembers();
        assertNotNull(members);
        assertTrue(members.size() > 0);
    }

    @Test
    public void testUpdateMember() {
        Member member = memberDAO.getMemberById(1);
        assertNotNull(member);

        String originalName = member.getName();
        member.setName("张三updated");

        assertTrue(memberDAO.updateMember(member));

        Member updated = memberDAO.getMemberById(1);
        assertEquals("张三updated", updated.getName());

        // 恢复原始数据
        member.setName(originalName);
        memberDAO.updateMember(member);
    }

    @Test
    public void testGetByStatus() {
        List<Member> activeMembers = memberDAO.getMemberByStatus("active");
        assertNotNull(activeMembers);
        assertTrue(activeMembers.size() > 0);

        for (Member member : activeMembers) {
            assertEquals("active", member.getStatus());
        }
    }

    @Test
    public void testSearchByPhone() {
        List<Member> members = memberDAO.searchByPhone("13900001111");
        assertNotNull(members);
        assertTrue(members.size() > 0);
        assertEquals("张三", members.get(0).getName());
    }

    @Test
    public void testInvalidEmail() {
        Member member = new Member();
        member.setName("测试用户2");
        member.setPhone("13987654321");
        member.setEmail("invalid-email"); // 无效邮箱格式
        member.setGender("female");
        member.setRegisterDate(new Date());
        member.setStatus("active");

        assertFalse(memberDAO.addMember(member));
    }

    @Test
    public void testInvalidPhone() {
        Member member = new Member();
        member.setName("测试用户3");
        member.setPhone("123"); // 无效手机号
        member.setEmail("test3@email.com");
        member.setGender("male");
        member.setRegisterDate(new Date());
        member.setStatus("active");

        assertFalse(memberDAO.addMember(member));
    }

    @Test
    public void testInvalidStatus() {
        Member member = new Member();
        member.setName("测试用户4");
        member.setPhone("13900012345");
        member.setEmail("test4@email.com");
        member.setGender("female");
        member.setRegisterDate(new Date());
        member.setStatus("invalid_status"); // 无效状态

        assertFalse(memberDAO.addMember(member));
    }

    @Test
    public void testDeleteMember() {
        // 先添加一个测试会员
        Member member = new Member();
        member.setName("待删除用户");
        member.setPhone("13900099999");
        member.setEmail("delete@email.com");
        member.setGender("other");
        member.setRegisterDate(new Date());
        member.setStatus("inactive");
        member.setBirthDate(new Date());
        assertTrue(memberDAO.addMember(member));
        int memberId = member.getId();

        // 删除该会员
        assertTrue(memberDAO.deleteMember(member));

        // 验证已删除
        assertNull(memberDAO.getMemberById(memberId));
    }
}
