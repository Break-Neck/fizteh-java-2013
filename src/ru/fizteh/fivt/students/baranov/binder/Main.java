package ru.fizteh.fivt.students.baranov.binder;

import ru.fizteh.fivt.binder.DoNotBind;
import ru.fizteh.fivt.binder.Name;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) {

        MyBinderFactory factory = new MyBinderFactory();
        try {
            MyBinder binder = factory.create(User.class);
            String s = "<User><id>1</id><userType>USER</userType><name><UserName><firstName>name</firstName><lastName>last</lastName></UserName></name><perm><Permissions><root>false</root><quota>10</quota></Permissions></perm></User>";
            ByteArrayInputStream input = new ByteArrayInputStream(s.getBytes());

            User user = (User) binder.deserialize(input);
            System.out.println(user.id);
            System.out.println(user.userType);
            System.out.println(user.name.firstName);
            System.out.println(user.name.lastName);
            System.out.println(user.perm.root);
            System.out.println(user.perm.quota);

        } catch (Exception e) {
            System.err.println(e.getMessage());
            return;
        }
    }
}

class User {
    public User() {
    }

    int id;
    type userType;
    UserName name;
    Permissions perm;

}

enum type {
    USER, ADMIN
}

class UserName {
    public UserName() {
    }

    String firstName;
    String lastName;
}

class Permissions {
    public Permissions() {
    }

    boolean root;
    int quota;
}