package entity;

import java.util.Date;

public abstract class Person {
    protected int id;
    protected String name;
    protected String phone;
    protected String email;
    private String gender;        // 性别
    private Date birthDate;       // 出生日期
    private Date registerDate;    // 注册日期

    public Person() {
    }

    public Person(int id, String name, String phone, String email) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    public Person(int id, String name, String phone, String email, String gender, Date birthDate, Date registerDate) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.gender = gender;
        this.birthDate = birthDate;
        this.registerDate = registerDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public void setRegisterDate(Date registerDate) {
        this.registerDate = registerDate;
    }

    public abstract  String getRole();

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", gender='" +gender+'\''+
                ", birthDate='" +birthDate+'\''+
                ", registerDate='"+registerDate+'\''+
                "}";
    }

    public Date getRegisterDate() {
        return registerDate;
    }
}
