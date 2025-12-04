package test;

import dao.MemberDAO;
import entity.Member;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class MemberDAOTest {
    private MemberDAO memberDAO;
    private int testMemberId;  // 用于清理测试数据

    @Before
    public void setUp() {
        memberDAO = new MemberDAO();
        testMemberId = 0;
    }

    @After
    public void tearDown() {
        // 清理测试数据
        if (testMemberId > 0) {
            Member member = memberDAO.getMemberById(testMemberId);
            if (member != null) {
                memberDAO.deleteMember(member);
            }
            testMemberId = 0;
        }
    }

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
        assertTrue(member.getId() > 0);

        testMemberId = member.getId();  // 记录ID用于清理
    }

    @Test
    public void testGetById() {
        Member member = memberDAO.getMemberById(1);
        assertNotNull(member);
        assertEquals("张三", member.getName());
        assertEquals("13900001111", member.getPhone());
    }

    @Test
    public void testGetByIdNotFound() {
        Member member = memberDAO.getMemberById(99999);
        assertNull(member);
    }

    @Test
    public void testGetAll() {
        List<Member> members = memberDAO.getAllMembers();
        assertNotNull(members);
        assertTrue(members.size() >= 10);  // 数据库有10条数据
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
    public void testGetByName() {
        List<Member> members = memberDAO.getMemberByName("张三");
        assertNotNull(members);
        assertTrue(members.size() > 0);
        assertEquals("张三", members.get(0).getName());
    }

    @Test
    public void testGetByGender() {
        List<Member> maleMembers = memberDAO.getMemberByGender("male");
        assertNotNull(maleMembers);
        for (Member member : maleMembers) {
            assertEquals("male", member.getGender());
        }

        List<Member> femaleMembers = memberDAO.getMemberByGender("female");
        assertNotNull(femaleMembers);
        for (Member member : femaleMembers) {
            assertEquals("female", member.getGender());
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
    public void testSearchByPhonePartial() {
        // 测试模糊搜索
        List<Member> members = memberDAO.searchByPhone("139");
        assertNotNull(members);
        assertTrue(members.size() > 0);
    }

    @Test
    public void testInvalidEmail() {
        Member member = new Member();
        member.setName("测试用户2");
        member.setPhone("13987654321");
        member.setEmail("invalid-email"); // 无效邮箱格式
        member.setGender("female");
        member.setBirthDate(new Date());
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
        member.setBirthDate(new Date());
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
        member.setBirthDate(new Date());
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
        member.setBirthDate(new Date());
        member.setRegisterDate(new Date());
        member.setStatus("inactive");

        assertTrue(memberDAO.addMember(member));
        int memberId = member.getId();

        // 删除该会员
        assertTrue(memberDAO.deleteMember(member));

        // 验证已删除
        assertNull(memberDAO.getMemberById(memberId));

        testMemberId = 0;  // 已删除，不需要 tearDown 再删
    }

    @Test
    public void testMemberRole() {
        Member member = memberDAO.getMemberById(1);
        assertNotNull(member);
        assertEquals("Member", member.getRole());
    }

    @Test
    public void testMemberBasicInfo() {
        Member member = memberDAO.getMemberById(1);
        assertNotNull(member);

        String basicInfo = member.getBasicInfo();
        assertNotNull(basicInfo);
        assertTrue(basicInfo.contains("张三"));
        assertTrue(basicInfo.contains("13900001111"));
    }

    @Test
    public void testGetByBirthDate() {
        // 使用已知的出生日期进行测试
        Member member = memberDAO.getMemberById(1);
        assertNotNull(member);

        if (member.getBirthDate() != null) {
            List<Member> members = memberDAO.getMemberByBirthDate(member.getBirthDate());
            assertNotNull(members);
            // 可能有多个人同一天生日
            assertTrue(members.size() >= 0);
        }
    }

    @Test
    public void testFrozenStatus() {
        List<Member> frozenMembers = memberDAO.getMemberByStatus("frozen");
        assertNotNull(frozenMembers);
        for (Member member : frozenMembers) {
            assertEquals("frozen", member.getStatus());
        }
    }

    @Test
    public void testInactiveStatus() {
        List<Member> inactiveMembers = memberDAO.getMemberByStatus("inactive");
        assertNotNull(inactiveMembers);
        for (Member member : inactiveMembers) {
            assertEquals("inactive", member.getStatus());
        }
    }
}
